package com.novartis.pcs.ontology.service.importer;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.EJB;

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

    public OntologyImportServiceBase() {
        super();
    }

    @Override
    public void importOntology(String ontologyName, InputStream is, Curator curator)
            throws DuplicateEntityException, InvalidEntityException {
        // Lock ontology while we are importing/updating
        // also need to lock ontology because we potentially
        // update the current term reference id value
        Ontology ontology = ontologyDAO.loadByName(ontologyName, true);
        Collection<Term> terms = Collections.emptyList();
        Version version = lastUnpublishedVersion(curator);

        if (ontology == null) {
            ontology = new Ontology(ontologyName, curator, version);
            ontology.setStatus(Status.APPROVED);
            ontology.setApprovedVersion(version);
        } else {
            terms = termDAO.loadAll(ontology);
        }

        // According to spec OBO files are UTF-8 encoded
        ParseContext context = parse(is, curator, version, ontology, terms);
        saveParsed(ontology, version, context);
    }

    protected abstract ParseContext parse(InputStream is, Curator curator, Version version, Ontology ontology,
            Collection<Term> terms);

    private void saveParsed(Ontology ontology, Version version, ParseContext context)
            throws InvalidEntityException, DuplicateEntityException {
        Collection<RelationshipType> relationshipTypes;
        Collection<Datasource> datasources;
        Collection<Term> terms = context.getTerms();

        findRefId(ontology, terms);
        validateDuplicates(terms);
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
    }

    protected abstract void findRefId(Ontology ontology, Collection<Term> terms) throws InvalidEntityException;

    private void validateDuplicates(Collection<Term> terms) throws DuplicateEntityException {
        Set<String> names = new HashSet<>(terms.size());
        for (Term term : terms) {
            if (!names.add(term.getName().toLowerCase())) {
                logger.warning("Duplicated term:" + term.getName());
                term.setName(term.getReferenceId()+":"+term.getName());
                // throw new DuplicateEntityException(term, "Duplicate term: " +
                // term.getName());
            }
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