package com.novartis.pcs.ontology.service.parser.owl;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Stateless;

import com.novartis.pcs.ontology.entity.Relationship;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLObjectVisitorExAdapter;
import org.semanticweb.owlapi.util.OWLOntologyWalker;

import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.service.parser.ParseContext;

import static java.util.logging.Level.INFO;

@Stateless
@Local(OWLParsingServiceLocal.class)
public class OWLParsingServiceImpl implements OWLParsingServiceLocal {
	private final Logger logger = Logger.getLogger(getClass().getName());

	private class MyOWLOntologyWalker extends OWLOntologyWalker {
		MyOWLOntologyWalker(OWLOntology owlOntology) {
			super(Collections.singleton(owlOntology));
			visitor = new OWLObjectVisitorExAdapter<>();
		}
	}

	@Override
	public ParseContext parseOWLontology(InputStream inputStream, Collection<RelationshipType> relationshipTypes,
			Collection<Datasource> datasources, Curator curator, Version version, Ontology ontology,
			final Collection<AnnotationType> annotationTypes, final Collection<Term> terms)
			throws OWLOntologyCreationException {

		final OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(inputStream);
		OWLOntologyWalker owlObjectWalker = new MyOWLOntologyWalker(owlOntology);

		final OWLParserContext context = new OWLParserContext(curator, version, datasources, ontology,
				relationshipTypes, annotationTypes, terms);
		ParsingStructureWalker parsingStructureWalker = new ParsingStructureWalker(owlObjectWalker,
				context);
		parsingStructureWalker.visit(owlOntology);

		validateDuplicates(context);
		Map<String, Term> terms2 = context.getTerms();
		logger.log(Level.INFO, () -> "Terms count:" + terms2.values().size());

		logger.log(Level.INFO, "The End!");
		return new ParseContextImpl(terms2.values(), context.getDatasources(), context.getRelationshipTypes(),
				context.getAnnotationTypes());
	}

	private void validateDuplicates(OWLParserContext context) {
		Map<String, Term> terms = context.getTerms();
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
	}

}
