package com.novartis.pcs.ontology.service.parser.owl;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLObjectVisitorExAdapter;
import org.semanticweb.owlapi.util.OWLOntologyWalker;

import com.novartis.pcs.ontology.dao.OntologyDAOLocal;
import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.entity.VersionedEntity;
import com.novartis.pcs.ontology.service.parser.ParseContext;

@Stateless
@Local(OWLParsingServiceLocal.class)
public class OWLParsingServiceImpl implements OWLParsingServiceLocal {
	private final Logger logger = Logger.getLogger(getClass().getName());

	@EJB
	private OntologyDAOLocal ontologyDAO;

	private class MyOWLOntologyWalker extends OWLOntologyWalker {
		MyOWLOntologyWalker(OWLOntology owlOntology) {
			super(Collections.singleton(owlOntology));
			visitor = new OWLObjectVisitorExAdapter<>();
		}
	}

	@Override
	public ParseContext parseOWLontology(InputStream inputStream, Collection<RelationshipType> relationshipTypes,
			Collection<Datasource> datasources, Curator curator, Version version, Ontology mainOntology,
			final Collection<AnnotationType> annotationTypes, final Collection<Term> terms)
			throws OWLOntologyCreationException {

		final OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(inputStream);

		final OWLParserContext context = new OWLParserContext(curator, version, datasources, mainOntology,
				relationshipTypes, annotationTypes, terms);
		List<OWLOntology> sortedImportsClosure = owlOntologyManager.getSortedImportsClosure(owlOntology);

		for (OWLOntology importedOwlOntology : Lists.reverse(sortedImportsClosure)) {
			String importedName = importedOwlOntology.getOntologyID().getOntologyIRI().getRemainder().get();
			Ontology ontology = ontologyDAO.loadByName(importedName, true);
			if (ontology == null) {
				ontology = new Ontology(importedName, curator, version);
				ontology.setStatus(VersionedEntity.Status.APPROVED);
				ontology.setApprovedVersion(version);
				ontology.setInternal(false);
			}
			try {
				ontologyDAO.save(ontology);
			} catch (InvalidEntityException e) {
				throw new RuntimeException("Ontology saving exception");
			}
			context.setOntology(ontology);
			OWLOntologyWalker owlObjectWalker = new MyOWLOntologyWalker(importedOwlOntology);
			ParsingStructureWalker parsingStructureWalker = new ParsingStructureWalker(owlObjectWalker, context);
			parsingStructureWalker.visit(importedOwlOntology);
		}

		OWLOntologyFormat ontologyFormat = owlOntologyManager.getOntologyFormat(owlOntology);
		context.getOntology().setSourceFormat(ontologyFormat.getClass().getSimpleName());
		Boolean validated = validateDuplicates(context);
		if (!validated) {
			throw new RuntimeException("Ontology saving exception");
		}
		Map<String, Term> terms2 = context.getTerms();
		logger.log(Level.INFO, () -> "Terms count:" + terms2.values().size());

		logger.log(Level.INFO, "The End!");
		return new ParseContextImpl(terms2.values(), context.getDatasources(), context.getRelationshipTypes(),
				context.getAnnotationTypes());
	}

	private Boolean validateDuplicates(OWLParserContext context) {
		final MutableBoolean validated = new MutableBoolean(true);
		Map<String, Term> terms = context.getTerms();
		Map<String, List<Term>> grouped = terms.values().stream().collect(Collectors.groupingBy(Term::getReferenceId));
		grouped.forEach((referenceId, termList) -> {
			if (termList.size() > 1) {
				logger.log(SEVERE, "Duplicated referenceId for {}", referenceId);
				String termsStr = termList.stream().map(ReflectionToStringBuilder::toString)
						.collect(Collectors.joining("; "));
				logger.log(SEVERE, "Terms [{}]", termsStr);
				validated.setFalse();
			}
		});

		terms.forEach((termName, term) -> {
			Map<String, Set<String>> relationshipsIndex = new HashMap<>();
			for (Relationship aRelationship : term.getRelationships()) {
				Set<String> types = relationshipsIndex
						.computeIfAbsent(aRelationship.getRelatedTerm().getReferenceId(),
								id -> new HashSet<>());
				String relationshipTypeId = aRelationship.getType().getRelationship();
				if (types.contains(relationshipTypeId)) {
					logger.log(INFO, "Duplicated relationship {0} {1} {2}", new String[]{term.getName(),
							aRelationship.getRelatedTerm().getName(), aRelationship.getType().getRelationship()});
				} else {
					types.add(relationshipTypeId);
				}
			}
		});
		return validated.getValue();
	}

}
