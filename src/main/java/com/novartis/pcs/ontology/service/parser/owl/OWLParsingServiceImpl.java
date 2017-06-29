package com.novartis.pcs.ontology.service.parser.owl;

import java.beans.Beans;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.util.OWLObjectVisitorExAdapter;
import org.semanticweb.owlapi.util.OWLObjectWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.StructureWalker;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.google.common.base.Optional;
import com.novartis.pcs.ontology.entity.AbstractEntity;
import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.entity.VersionedEntity;
import com.novartis.pcs.ontology.entity.VersionedEntity.Status;
import com.novartis.pcs.ontology.service.parser.ParseContext;

@Stateless
@Local(OWLParsingServiceLocal.class)
public class OWLParsingServiceImpl implements OWLParsingServiceLocal {
	private Logger logger = Logger.getLogger(getClass().getName());

	enum ParserState {
		ROOT, ONTOLOGY, CLASS, OBJECT_PROPERTY, ANNOTATION_ASSERTION
	}

	private class MyOWLOntologyWalker extends OWLOntologyWalker {
		MyOWLOntologyWalker(OWLOntology owlOntology) {
			super(Collections.singleton(owlOntology));
			visitor = new OWLObjectVisitorExAdapter<>();
		}
	}

	class ParsingStructureWalker extends StructureWalker<OWLOntology> {

		private OWLOntologyManager owlOntologyManager;
		private OWLDataFactory df;
		private Ontology ontology;
		private Curator curator;
		private Version version;

		// indexes of entities
		private Map<String, Term> terms = new HashMap<>();
		private List<Relationship> relationships = new ArrayList<>();
		private Map<String, RelationshipType> relationshipTypes = new HashMap<>();
		private Map<String, String> danglingLabels = new HashMap<>();
		private Map<String, String> danglingDefinitons = new HashMap<>();
		private Map<Term, Map<Term, RelationshipType>> relationshipMap = new HashMap<>();
		// current state
		private Deque<ParserState> state = new LinkedList<>();
		private Deque<Term> termStack = new LinkedList<>();

		private RelationshipType relationshipType;
		private Relationship relationship;

		public ParsingStructureWalker(OWLObjectWalker<OWLOntology> owlObjectWalker, OWLOntologyManager owlOntologyManager, OWLDataFactory df,
				Collection<RelationshipType> relationshipTypes, Curator curator, Version version, Ontology ontology) {
			super(owlObjectWalker);
			this.owlOntologyManager = owlOntologyManager;
			this.df = df;
			this.curator = curator;
			this.version = version;
			this.ontology = ontology;
			this.relationshipTypes = relationshipTypes.stream().collect(Collectors.toMap(i -> i.getRelationship(), Function.identity()));
		}

		@Override
		public void visit(OWLOntology owlOntology) {
			logger.log(Level.FINE, "OWLOntology" + ":" + owlOntology.toString());
			IRI documentIRI = owlOntologyManager.getOntologyDocumentIRI(owlOntology);
			ontology.setSourceUri(documentIRI.toString());
			state.push(ParserState.ONTOLOGY);
			super.visit(owlOntology);
			state.pop();

			// dangling
			danglingLabels.forEach((k, v) -> matchLabel(k, v, "name"));
			danglingDefinitons.forEach((k, v) -> matchLabel(k, v, "definition"));

			Term thing = terms.get("Thing");
			terms.forEach((termName, term) -> {
				if (term.getRelationships().isEmpty() && !term.equals(thing)) {
					Relationship relationship = new Relationship(term, thing, relationshipTypes.get("is_a"), curator, version);
					initVersion(relationship);
				}
			});
		}

		@Override
		public void visit(OWLAnnotation owlAnnotation) {
			logger.log(Level.FINE, "OWLAnnotation" + ":" + owlAnnotation.toString());
			OWLAnnotationProperty owlAnnotationProperty = owlAnnotation.getProperty();
			if (ParserState.ONTOLOGY.equals(state.peek())) {
				if (isRDFSLabel(owlAnnotationProperty)) {
					String name = appendNonEmpty(toString(owlAnnotation), ontology.getName());
					ontology.setName(name.substring(0, Math.min(name.length() - 1, 63)));
				} else if (isRDFSComment(owlAnnotationProperty)) {
					String description = appendNonEmpty(toString(owlAnnotation), ontology.getDescription());
					ontology.setDescription(description.substring(0, Math.min(description.length() - 1, 63)));
				}
			} else if (ParserState.CLASS.equals(state.peek())) {
				if (isRDFSLabel(owlAnnotationProperty)) {
					termStack.peek().setName(toString(owlAnnotation));
				} else if (isRDFSComment(owlAnnotationProperty)) {
					termStack.peek().setComments(toString(owlAnnotation));
				}
			} else if (ParserState.OBJECT_PROPERTY.equals(state.peek())) {
				if (isRDFSComment(owlAnnotationProperty)) {
					relationshipType.setRelationship(toString(owlAnnotation));
				}
			}
		}

		private boolean isRDFSLabel(OWLAnnotationProperty owlAnnotationProperty) {
			return df.getRDFSLabel().equals(owlAnnotationProperty);
		}

		private boolean isRDFSComment(OWLAnnotationProperty owlAnnotationProperty) {
			return df.getRDFSComment().equals(owlAnnotationProperty);
		}

		private String appendNonEmpty(String string, String existing) {
			return existing != null && !existing.isEmpty() ? existing + " " + string : string;
		}

		private String toString(OWLAnnotation owlAnnotation) {
			return ((OWLLiteral) owlAnnotation.getValue()).getLiteral();
		}

		@Override
		public void visit(OWLClass owlClass) {
			logger.log(Level.FINE, "OWLClass" + ":" + owlClass.toString());
			String fragment = owlClass.getIRI().getRemainder().orNull();
			Term current = getTerm(fragment);
			termStack.push(current);
			state.push(ParserState.CLASS);
			super.visit(owlClass);
			state.pop();
		}

		private Term getTerm(String fragment) {
			Term current;
			if (terms.containsKey(fragment)) {
				current = terms.get(fragment);
			} else {
				current = new Term(ontology, fragment /* temporary name */, fragment, curator, version);
				initVersion(current);
				terms.put(fragment, current);
			}
			return current;
		}

		private void initVersion(VersionedEntity versionedEntity) {
			versionedEntity.setStatus(Status.APPROVED);
			versionedEntity.setApprovedVersion(version);
		}

		@Override
		public void visit(OWLSubClassOfAxiom subClassAxiom) {
			logger.log(Level.FINE, "OWLSubClassOfAxiom" + ":" + subClassAxiom.toString());
			super.visit(subClassAxiom);
			Term relatedTerm = termStack.pop(); // do not inline, keep in order!
			Term term = termStack.pop();

			Map<Term, RelationshipType> relatedTerms = relationshipMap.computeIfAbsent(term, k -> new HashMap<>());
			RelationshipType oldDirect = relatedTerms.get(relatedTerm);
			RelationshipType inverse = relationshipMap.getOrDefault(relatedTerm, Collections.emptyMap()).get(term);

			if (compareRelationships(oldDirect) || compareRelationships(inverse)) {
				logger.severe("duplicated relationshop");
			} else {
				if (relationshipType == null) {
					logger.severe("no relationship type present");
					relationshipType = relationshipTypes.get("is_a");
				}
				relationship = new Relationship(term, relatedTerm, relationshipType, curator, version);
				initVersion(relationship);
				relatedTerms.put(relatedTerm, relationshipType);
				term.getRelationships().add(relationship);
				relationships.add(relationship);
			}
		}

		private boolean compareRelationships(RelationshipType oldDirect) {
			return oldDirect != null && oldDirect.getRelationship().equals(relationshipType.getRelationship());
		}

		@Override
		public void visit(OWLObjectProperty property) {
			logger.log(Level.FINE, "OWLObjectProperty" + ":" + property.toString());
			state.push(ParserState.OBJECT_PROPERTY);
			super.visit(property);

			String objectPropertyFragment = property.getIRI().getRemainder().orNull();

			visitProperty(objectPropertyFragment);
			state.pop();
		}

		private void visitProperty(String propertyFragment) {
			if (relationshipTypes.containsKey(propertyFragment)) {
				relationshipType = relationshipTypes.get(propertyFragment);
			} else {
				// replace with computeIfAbsent
				relationshipType = new RelationshipType(propertyFragment, propertyFragment, propertyFragment, curator, version);
				initVersion(relationshipType);
				relationshipTypes.put(relationshipType.getRelationship(), relationshipType);
			}
		}

		@Override
		public void visit(IRI iri) {
			logger.log(Level.INFO, "IRI" + ":" + iri.toQuotedString());
			super.visit(iri);
		}

		@Override
		public void visit(OWLObjectSomeValuesFrom desc) {
			logger.log(Level.INFO, "OWLObjectSomeValuesFrom" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLDataSomeValuesFrom desc) {
			logger.log(Level.INFO, "OWLDataSomeValuesFrom" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectPropertyAssertionAxiom objectPropertyAxiom) {
			logger.log(Level.INFO, "OWLObjectPropertyAssertionAxiom" + ":" + objectPropertyAxiom.toString());
			super.visit(objectPropertyAxiom);
		}

		@Override
		public void visit(OWLAnnotationAssertionAxiom axiom) {
			state.push(ParserState.ANNOTATION_ASSERTION);
			logger.log(Level.INFO, "OWLAnnotationAssertionAxiom" + ":" + axiom.toString());
			super.visit(axiom);
			state.pop();

			String referenceId = null;
			if (axiom.getSubject() instanceof IRI) {
				IRI subjectIRI = (IRI) axiom.getSubject();
				Optional<String> fragmentOpt = subjectIRI.getRemainder();
				if (fragmentOpt.isPresent()) {
					referenceId = fragmentOpt.get();
				}
			}

			if (referenceId == null) {
				return;
			}

			if (isRDFSLabel(axiom.getProperty())) {
				String label = toString(axiom.getAnnotation());
				boolean matched = matchLabel(referenceId, label, "name");
				if (!matched) {
					danglingLabels.put(referenceId, label);
				}
			}

			if (isIAODefinition(axiom.getProperty())) {
				String definition = toString(axiom.getAnnotation());
				boolean matched = matchLabel(referenceId, definition, "definition");
				if (!matched)
					danglingDefinitons.put(referenceId, definition);
			}
		}

		private boolean isIAODefinition(OWLAnnotationProperty property) {
			String string = property.getIRI().toString();
			return string.equals("http://purl.obolibrary.org/obo/IAO_0000115");
		}

		private boolean matchLabel(String fragment, String label, String propertyName) {
			AbstractEntity entity = null;
			if (terms.containsKey(fragment)) {
				entity = terms.get(fragment);
			} else if (relationshipTypes.containsKey(fragment)) {
				entity = relationshipTypes.get(fragment);
			}
			boolean matched = entity != null;

			if (matched) {
				try {
					BeanUtils.setProperty(entity, propertyName, label);
				} catch (IllegalAccessException | InvocationTargetException e) {
					logger.log(Level.SEVERE, "Cannot set [property={},entity={}]", new Object[] { propertyName, entity.getId() });
				}
			}
			return matched;
		}

		@Override
		public void visit(OWLAnnotationProperty property) {
			logger.log(Level.INFO, "OWLAnnotationProperty" + ":" + property.toString());
			super.visit(property);
		}

		@Override
		public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
			logger.log(Level.INFO, "OWLAnnotationPropertyDomainAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
			logger.log(Level.INFO, "OWLAnnotationPropertyRangeAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLAnonymousIndividual individual) {
			logger.log(Level.INFO, "OWLAnonymousIndividual" + ":" + individual.toString());
			super.visit(individual);
		}

		@Override
		public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
			logger.log(Level.INFO, "OWLAsymmetricObjectPropertyAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLClassAssertionAxiom axiom) {
			logger.log(Level.INFO, "OWLClassAssertionAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDataAllValuesFrom desc) {
			logger.log(Level.INFO, "OWLDataAllValuesFrom" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLDataComplementOf node) {
			logger.log(Level.INFO, "OWLDataComplementOf" + ":" + node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLDataExactCardinality desc) {
			logger.log(Level.INFO, "OWLDataExactCardinality" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLDataHasValue desc) {
			logger.log(Level.INFO, "OWLDataHasValue" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLDataIntersectionOf node) {
			logger.log(Level.INFO, "OWLDataIntersectionOf" + ":" + node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLDataMaxCardinality desc) {
			logger.log(Level.INFO, "OWLDataMaxCardinality" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLDataMinCardinality desc) {
			logger.log(Level.INFO, "OWLDataMinCardinality" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLDataOneOf node) {
			logger.log(Level.INFO, "OWLDataOneOf" + ":" + node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLDataProperty property) {
			logger.log(Level.INFO, "OWLDataProperty" + ":" + property.toString());
			super.visit(property);
		}

		@Override
		public void visit(OWLDataPropertyAssertionAxiom axiom) {
			logger.log(Level.INFO, "OWLDataPropertyAssertionAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDataPropertyDomainAxiom axiom) {
			logger.log(Level.INFO, "OWLDataPropertyDomainAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDataPropertyRangeAxiom axiom) {
			logger.log(Level.INFO, "OWLDataPropertyRangeAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDatatype node) {
			logger.log(Level.INFO, "OWLDatatype" + ":" + node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLDatatypeDefinitionAxiom axiom) {
			logger.log(Level.INFO, "OWLDatatypeDefinitionAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDatatypeRestriction node) {
			logger.log(Level.INFO, "OWLDatatypeRestriction" + ":" + node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLDataUnionOf node) {
			logger.log(Level.INFO, "OWLDataUnionOf" + ":" + node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLDeclarationAxiom axiom) {
			logger.log(Level.INFO, "OWLDeclarationAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDifferentIndividualsAxiom axiom) {
			logger.log(Level.INFO, "OWLDifferentIndividualsAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDisjointClassesAxiom axiom) {
			logger.log(Level.INFO, "OWLDisjointClassesAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDisjointDataPropertiesAxiom axiom) {
			logger.log(Level.INFO, "OWLDisjointDataPropertiesAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
			logger.log(Level.INFO, "OWLDisjointObjectPropertiesAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDisjointUnionAxiom axiom) {
			logger.log(Level.INFO, "OWLDisjointUnionAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLEquivalentClassesAxiom axiom) {
			logger.log(Level.INFO, "OWLEquivalentClassesAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
			logger.log(Level.INFO, "OWLEquivalentDataPropertiesAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
			logger.log(Level.INFO, "OWLEquivalentObjectPropertiesAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLFacetRestriction node) {
			logger.log(Level.INFO, "OWLFacetRestriction" + ":" + node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLFunctionalDataPropertyAxiom axiom) {
			logger.log(Level.INFO, "axiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
			logger.log(Level.INFO, "OWLFunctionalObjectPropertyAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLHasKeyAxiom axiom) {
			logger.log(Level.INFO, "OWLHasKeyAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
			logger.log(Level.INFO, "OWLInverseFunctionalObjectPropertyAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLInverseObjectPropertiesAxiom axiom) {
			logger.log(Level.INFO, "OWLInverseObjectPropertiesAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
			logger.log(Level.INFO, "OWLIrreflexiveObjectPropertyAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLLiteral literal) {
			logger.log(Level.INFO, "OWLLiteral" + ":" + literal.toString());
			super.visit(literal);
		}

		@Override
		public void visit(OWLNamedIndividual individual) {
			logger.log(Level.INFO, "OWLNamedIndividual" + ":" + individual.toString());
			super.visit(individual);
		}

		@Override
		public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
			logger.log(Level.INFO, "OWLNegativeDataPropertyAssertionAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
			logger.log(Level.INFO, "OWLNegativeObjectPropertyAssertionAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLObjectAllValuesFrom desc) {
			logger.log(Level.INFO, "OWLObjectAllValuesFrom" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectComplementOf desc) {
			logger.log(Level.INFO, "OWLObjectComplementOf" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectExactCardinality desc) {
			logger.log(Level.INFO, "OWLObjectExactCardinality" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectHasSelf desc) {
			logger.log(Level.INFO, "OWLObjectHasSelf" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectHasValue desc) {
			logger.log(Level.INFO, "OWLObjectHasValue" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectIntersectionOf desc) {
			logger.log(Level.INFO, "OWLObjectIntersectionOf" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectInverseOf property) {
			logger.log(Level.INFO, "OWLObjectInverseOf" + ":" + property.toString());
			super.visit(property);
		}

		@Override
		public void visit(OWLObjectMaxCardinality desc) {
			logger.log(Level.INFO, "OWLObjectMaxCardinality" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectMinCardinality desc) {
			logger.log(Level.INFO, "OWLObjectMinCardinality" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectOneOf desc) {
			logger.log(Level.INFO, "OWLObjectOneOf" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectPropertyDomainAxiom axiom) {
			logger.log(Level.INFO, "OWLObjectPropertyDomainAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLObjectPropertyRangeAxiom axiom) {
			logger.log(Level.INFO, "OWLObjectPropertyRangeAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLObjectUnionOf desc) {
			logger.log(Level.INFO, "OWLObjectUnionOf" + ":" + desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
			logger.log(Level.INFO, "OWLReflexiveObjectPropertyAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLSameIndividualAxiom axiom) {
			logger.log(Level.INFO, "OWLSameIndividualAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
			logger.log(Level.INFO, "OWLSubAnnotationPropertyOfAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLSubDataPropertyOfAxiom axiom) {
			logger.log(Level.INFO, "OWLSubDataPropertyOfAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLSubObjectPropertyOfAxiom axiom) {
			logger.log(Level.INFO, "OWLSubObjectPropertyOfAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLSubPropertyChainOfAxiom axiom) {
			logger.log(Level.INFO, "OWLSubPropertyChainOfAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
			logger.log(Level.INFO, "OWLSymmetricObjectPropertyAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
			logger.log(Level.INFO, "OWLTransitiveObjectPropertyAxiom" + ":" + axiom.toString());
			super.visit(axiom);
		}

		public Ontology getOntology() {
			return ontology;
		}

		public Map<String, Term> getTerms() {
			return terms;
		}

		public Map<String, RelationshipType> getRelationshipTypes() {
			return relationshipTypes;
		}

		public List<Relationship> getRelationships() {
			return relationships;
		}
	}

	@Override
	public ParseContext parseOWLontology(InputStream inputStream, Collection<RelationshipType> relationshipTypes, Collection<Datasource> datasources,
			Curator curator, Version version, Ontology ontology) throws OWLOntologyCreationException {

		final OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
		final OWLDataFactory df = owlOntologyManager.getOWLDataFactory();
		OWLOntology owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(inputStream);
		OWLOntologyWalker owlObjectWalker = new MyOWLOntologyWalker(owlOntology);

		ParsingStructureWalker parsingStructureWalker = new ParsingStructureWalker(owlObjectWalker, owlOntologyManager, df, relationshipTypes, curator, version,
				ontology);
		parsingStructureWalker.visit(owlOntology);

		parsingStructureWalker.getOntology();

		Map<String, Term> terms = parsingStructureWalker.getTerms();
		Set<String> termIds = terms.keySet();
		logger.log(Level.INFO, termIds.toString());
		Map<String, RelationshipType> relationshipTypes2 = parsingStructureWalker.getRelationshipTypes();

		List<Relationship> relationships = parsingStructureWalker.getRelationships();

		logger.log(Level.INFO, "The End!");
		return new ParseContextImpl(terms.values(), datasources, relationshipTypes2.values());
	}

}
