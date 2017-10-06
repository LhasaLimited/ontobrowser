package com.novartis.pcs.ontology.service.importer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.novartis.pcs.ontology.entity.OntologyAlias;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.novartis.pcs.ontology.dao.AnnotationTypeDAOLocal;
import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.DuplicateEntityException;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.entity.VersionedEntity.Status;
import com.novartis.pcs.ontology.service.OntologyService;
import com.novartis.pcs.ontology.service.parser.ParseContext;
import com.novartis.pcs.ontology.service.search.OntologySearchServiceLocal;

public abstract class OntologyImportServiceBase extends OntologyService
        implements OntologyImportServiceRemote, OntologyImportServiceLocal {

    private Logger logger = Logger.getLogger(getClass().getName());

    @EJB
    private OntologySearchServiceLocal searchService;

    @EJB
    private AnnotationTypeDAOLocal annotationTypeDAO;

    @PersistenceContext(unitName = "ontobrowser")
    private EntityManager entityManager;

    public OntologyImportServiceBase() {
        super();
    }

	@Override
	public void importOntology(String ontologyName, InputStream is, Curator curator, final List<String> aliases, final boolean fastImport)
			throws DuplicateEntityException, InvalidEntityException {
		// Lock ontology while we are importing/updating
		// also need to lock ontology because we potentially
		// update the current term reference id value
		Ontology ontology = ontologyDAO.loadByName(ontologyName, true);
		Collection<Term> terms = new ArrayList<>();
		Version version = lastUnpublishedVersion(curator);

		if (ontology == null) {
			ontology = new Ontology(ontologyName, curator, version);
			ontology.setStatus(Status.APPROVED);
			ontology.setApprovedVersion(version);
			ontology.setInternal(false);
			ontology.setIntermediate(false);
			ontology.setAliases(toOntologyAliases(aliases, ontology));
			ontologyDAO.save(ontology);
			ontology = ontologyDAO.loadByName(ontologyName);
		} else {
			terms = termDAO.loadAll(ontology);
		}

		terms.add(termDAO.loadByReferenceId("Thing", ontologyName));
        // According to spec OBO files are UTF-8 encoded
        ParseContext context = parse(is, curator, version, ontology, terms, fastImport);
        saveParsed(ontology, version, context);
    }

	private Set<OntologyAlias> toOntologyAliases(final List<String> aliases, final Ontology ontology) {
		Set<OntologyAlias> set = new HashSet<>();
		for (String alias : aliases) {
			OntologyAlias ontologyAlias = new OntologyAlias(ontology, alias);
			set.add(ontologyAlias);
		}
		return set;
	}

	protected abstract ParseContext parse(InputStream is, Curator curator, Version version, Ontology ontology,
										  Collection<Term> terms, final boolean fastImport);

    private void saveParsed(Ontology ontology, Version version, ParseContext context)
            throws InvalidEntityException, DuplicateEntityException {
        Collection<RelationshipType> relationshipTypes;
        Collection<Datasource> datasources;
        Collection<Term> terms = context.getTerms();

        findRefId(ontology, terms);
//        handleDuplicates(terms);
        markRoot(terms);

        datasources = context.getDatasources();
        for (Datasource datasource : datasources) {
            if (datasource.getId() == 0L) {
                datasourceDAO.save(datasource);
            }
        }

        relationshipTypes = context.getRelationshipTypes();
        for (RelationshipType relationshipType : relationshipTypes) {
            if (relationshipType.getId() == 0L) {
                relationshipTypeDAO.save(relationshipType);
            }
        }
        Collection<AnnotationType> annotationTypes = context.getAnnotationTypes();
        for (AnnotationType annotationType : annotationTypes) {
            if (annotationType.getId() == 0L) {
                annotationTypeDAO.save(annotationType);
            }
        }

        if (ontology.getId() == 0L) {
            ontologyDAO.save(ontology);
        }

        for (Term term : terms) {
            if (term.getId() == 0L) {
                termDAO.save(term);
            } else if (term.getStatus() == Status.OBSOLETE) {
                removePendingDependents(term, version);
            }
            searchService.update(term);
        }
        entityManager.flush();
        entityManager.clear();
    }

    protected abstract void findRefId(Ontology ontology, Collection<Term> terms) throws InvalidEntityException;

    private void handleDuplicates(Collection<Term> terms) throws DuplicateEntityException {
		Map<String, Integer> names = new HashMap<>(terms.size());
        for (Term term : terms) {
			if (term.getName() == null) {
				throw new IllegalArgumentException(ToStringBuilder.reflectionToString(term));
            }
			names.compute(term.getName().toLowerCase(), (key, counter) -> {
				if (counter == null) {
					return 1;
				} else {
					counter++;
					term.setReferenceId(term.getReferenceId() + counter);
					term.setName(term.getName() + counter);
					logger.logp(Level.WARNING, this.getClass().getName(), "handleDuplicates",
							"Duplicated term [name={0}, referenceId={1}]",
							new String[] { term.getName(), term.getReferenceId() });
					return counter;
				}
			});
		}
	}

    private void markRoot(Collection<Term> terms) {
        for (Term term : terms) {
            if (term.getStatus() == Status.APPROVED) {
                boolean found = false;
                for (Relationship relationship : term.getRelationships()) {
                    if (relationship.getStatus() == Status.APPROVED) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    term.setRoot(true);
                }
            }
        }
    }

}