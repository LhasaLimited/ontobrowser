package com.novartis.pcs.ontology.service.parser.owl;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.apache.commons.lang3.StringUtils;
import org.obolibrary.obo2owl.Obo2OWLConstants;
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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.novartis.pcs.ontology.entity.Annotation;
import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.CrossReference;
import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Synonym;
import com.novartis.pcs.ontology.entity.Synonym.Type;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.service.parser.ParseContext;

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

	class ParsingStructureWalker extends StructureWalker<OWLOntology> {

		private final Map<IRI, Synonym.Type> synonymMap = ImmutableMap.of(
				Obo2OWLConstants.Obo2OWLVocabulary.IRI_OIO_hasBroadSynonym.getIRI(), Synonym.Type.BROAD,
				Obo2OWLConstants.Obo2OWLVocabulary.IRI_OIO_hasExactSynonym.getIRI(), Synonym.Type.EXACT,
				Obo2OWLConstants.Obo2OWLVocabulary.IRI_OIO_hasRelatedSynonym.getIRI(), Synonym.Type.RELATED,
				Obo2OWLConstants.Obo2OWLVocabulary.IRI_OIO_hasNarrowSynonym.getIRI(), Synonym.Type.NARROW);

		private static final int ACRONYM_INDEX = 0;
		private static final int REFID_INDEX = 1;

		private final OWLOntologyManager owlOntologyManager;
		private final OWLParserContext context;
		// indexes of entities
		private final Map<String, Map<String, Set<String>>> relationshipMap = new HashMap<>();
		private final Map<IRI, List<OWLAnnotation>> annotationsBySubject = new HashMap<>();

		// current state

		public ParsingStructureWalker(OWLObjectWalker<OWLOntology> owlObjectWalker,
				OWLOntologyManager owlOntologyManager, final OWLParserContext context) {
			super(owlObjectWalker);
			this.context = context;
			this.owlOntologyManager = owlOntologyManager;
		}

		@Override
		public void visit(OWLOntology owlOntology) {
			logger.log(FINE, "OWLOntology:{0}", owlOntology.toString());
			IRI documentIRI = owlOntologyManager.getOntologyDocumentIRI(owlOntology);
			context.getOntology().setSourceUri(documentIRI.toString());

			Set<OWLClass> owlClasses = owlOntology.getClassesInSignature();
			for (OWLClass owlClass : owlClasses) {
				Optional<String> remainder = owlClass.getIRI().getRemainder();
				if (remainder.isPresent()) {
					String referenceId = remainder.get();
					context.getTerm(referenceId);
				}
			}
			Set<OWLAnnotationProperty> annotationProps = owlOntology.getAnnotationPropertiesInSignature();
			for (OWLAnnotationProperty annotationProp : annotationProps) {
				context.visitPropertyAnnotation(annotationProp.getIRI().getRemainder().orNull());
			}

			Set<OWLObjectProperty> objectProperties = owlOntology.getObjectPropertiesInSignature();
			for (OWLObjectProperty objectProperty : objectProperties) {
				context.visitPropertyRelationship(objectProperty.getIRI().getRemainder().orNull());
			}

			Set<OWLDataProperty> dataProperties = owlOntology.getDataPropertiesInSignature();
			for (OWLDataProperty owlDataProperty : dataProperties) {
				context.visitPropertyRelationship(owlDataProperty.getIRI().getRemainder().orNull());
			}

			context.statePush(ParserState.ONTOLOGY);
			super.visit(owlOntology);
			context.statePop();

			addRootRelationships();
			validateDuplicates();
		}

		private void validateDuplicates() {
			Map<String, Term> terms = context.getTerms();
			terms.forEach((termName, term) -> {
				Map<String, Set<String>> relationshipsIndex = new HashMap<>();
				for (Relationship aRelationship : term.getRelationships()) {
					Set<String> types = relationshipsIndex
							.computeIfAbsent(aRelationship.getRelatedTerm().getReferenceId(),
							id -> new HashSet<>());
					String relationshipTypeId = aRelationship.getType().getRelationship();
					if (types.contains(relationshipTypeId)) {
						logger.log(INFO, "Duplicated relationship {0} {1} {2}", new String[] { term.getName(),
								aRelationship.getRelatedTerm().getName(), aRelationship.getType().getRelationship() });
					} else {
						types.add(relationshipTypeId);
					}
				}
			});
		}

		private void addRootRelationships() {
			Map<String, Term> terms = context.getTerms();
			Term thing = context.getTerm("Thing");
			terms.forEach((termName, term) -> {
				if (term.getRelationships().isEmpty() && !term.equals(thing)) {
					// added to term in constructor
					context.approve(new Relationship(term, thing, context.getRelationshipType("is_a"),
							context.getCurator(), context.getVersion()));
				}
			});
		}

		@Override
		public void visit(OWLAnnotation owlAnnotation) {
			logger.log(FINE, "OWLAnnotation:{0}", owlAnnotation.toString());
			OWLAnnotationProperty owlAnnotationProperty = owlAnnotation.getProperty();
			ParserState currentState = context.statePeek();
			if (ParserState.ONTOLOGY.equals(currentState)) {
				if (owlAnnotationProperty.isComment()) {
					String description = appendNonEmpty(context.getOntology().getDescription(),
							getString(owlAnnotation));
					context.getOntology().setDescription(StringUtils.abbreviate(description, 1024));
				}
			} else if (ParserState.TERM.equals(currentState)) {
				Term term = context.termPeek();
				if (owlAnnotationProperty.isLabel()) {
					term.setName(getString(owlAnnotation));
				} else if (owlAnnotationProperty.isComment()) {
					term.setComments(getString(owlAnnotation));
				} else if (Obo2OWLConstants.Obo2OWLVocabulary.IRI_OIO_hasDbXref.getIRI()
						.equals(owlAnnotationProperty.getIRI())) {
					if (owlAnnotation.getValue() instanceof OWLLiteral) {
						String string = getString(owlAnnotation);
						if (string != null && string.contains(":")) {
							String[] splitted = string.split(":");
							Datasource datasource = context.getDatasource(splitted[ACRONYM_INDEX]);
							new CrossReference(term, datasource, splitted[REFID_INDEX], context.getCurator());
						} else if (string != null) {
							// TS28 from bioontology - without prefix
						}
					} else if (owlAnnotation.getValue() instanceof IRI) {
						new CrossReference(term, context.getIri().toString(), context.getCurator());
					}
				} else if (Obo2OWLConstants.Obo2OWLVocabulary.IRI_IAO_0000115.getIRI()
						.equals(owlAnnotationProperty.getIRI())) {
					term.setDefinition(getString(owlAnnotation));
				} else if (Obo2OWLConstants.Obo2OWLVocabulary.IRI_IAO_0100001.getIRI()
						.equals(owlAnnotationProperty.getIRI())) {
					logger.log(INFO, "replaced_by unused in BAO");
				} else if (synonymMap.containsKey(owlAnnotationProperty.getIRI())) {
					Type type = synonymMap.get(owlAnnotationProperty.getIRI());
					if (owlAnnotation.getValue() instanceof OWLLiteral) {
						OWLLiteral literal = (OWLLiteral) owlAnnotation.getValue();
						Synonym synonym = new Synonym(term, literal.getLiteral(), type, context.getCurator(),
								context.getVersion());
						context.approve(synonym);
					} else if (owlAnnotation.getValue() instanceof IRI) {
						logger.warning("IRI not supported for Synonyms");
					}

				} else {
					AnnotationType annotationType = context
							.getAnnotationType(owlAnnotationProperty.getIRI().getShortForm());
					String value = getString(owlAnnotation);
					Annotation annotation = new Annotation(value, annotationType, term, context.getCurator(),
							context.getVersion());
					context.approve(annotation);
					term.getAnnotations().add(annotation);
				}
			} else if (ParserState.RELATIONSHIP.equals(currentState)) {
				if (owlAnnotationProperty.isLabel()) {
					context.getRelationshipType().setName(getString(owlAnnotation));
				}
			} else if (ParserState.ANNOTATION_TYPE.equals(currentState)){
				if(owlAnnotationProperty.isLabel()) {
					context.getAnnotationType().setAnnotationType(getString(owlAnnotation));
				}
			}
		}

		private String appendNonEmpty(String existing, String appended) {
			return existing != null && !existing.isEmpty() ? existing + " " + appended : appended;
		}

		private String getString(OWLAnnotation owlAnnotation) {
			String result = null;
			if (owlAnnotation.getValue() instanceof OWLLiteral) {
				OWLLiteral owlLiteral = (OWLLiteral) owlAnnotation.getValue();
				result = owlLiteral.getLiteral();
			} else if (owlAnnotation.getValue() instanceof IRI) {
				result = ((IRI) owlAnnotation.getValue()).getRemainder().orNull();
			}
			return result;
		}

		@Override
		public void visit(OWLClass owlClass) {
			logger.log(FINE, "OWLClass:{0}", owlClass.toString());
			String fragment = owlClass.getIRI().getRemainder().orNull();
			Term current = context.getTerm(fragment);
			context.termPush(current);
			context.statePush(ParserState.TERM);
			super.visit(owlClass);
			context.statePop();
		}

		@Override
		public void visit(OWLSubClassOfAxiom subClassAxiom) {
			logger.log(FINE, "OWLSubClassOfAxiom:{0}", subClassAxiom.toString());
			super.visit(subClassAxiom);
			Term relatedTerm = context.termPop(); // do not inline, keep in
													// order!
			Term term = context.termPop();

			RelationshipType isARelationship = context.getRelationshipType("is_a");

			Map<String, Set<String>> relatedTerms = relationshipMap.computeIfAbsent(term.getReferenceId(),
					k -> new HashMap<>());
			Set<String> relationshipTypesSet = relatedTerms.computeIfAbsent(relatedTerm.getReferenceId(),
					id -> new HashSet<>());

			if (relationshipTypesSet.contains(isARelationship.getRelationship())) {
				logger.log(INFO, "Duplicated relationship {0} {1} {2}", new String[] { term.getReferenceId(),
						relatedTerm.getReferenceId(), isARelationship.getRelationship() });
			} else {
				relationshipTypesSet.add(isARelationship.getRelationship());
				Relationship relationship = new Relationship(term, relatedTerm, isARelationship, context.getCurator(),
						context.getVersion());
				context.approve(relationship);
				term.getRelationships().add(relationship);
			}
		}

		@Override
		public void visit(OWLObjectProperty property) {
			logger.log(FINE, "OWLObjectProperty:{0}", property.toString());
			context.statePush(ParserState.OBJECT_PROPERTY);
			super.visit(property);

			String objectPropertyFragment = property.getIRI().getRemainder().orNull();

			context.visitPropertyRelationship(objectPropertyFragment);
			context.statePop();
		}

		@Override
		public void visit(IRI iri) {
			logger.log(Level.INFO, "IRI:{0}", iri.toQuotedString());
			context.setIri(iri);
			super.visit(iri);
		}

		@Override
		public void visit(OWLObjectSomeValuesFrom desc) {
			logger.log(INFO, "OWLObjectSomeValuesFrom:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLDataSomeValuesFrom desc) {
			logger.log(INFO, "OWLDataSomeValuesFrom:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectPropertyAssertionAxiom objectPropertyAxiom) {
			logger.log(INFO, "OWLObjectPropertyAssertionAxiom:{0}", objectPropertyAxiom.toString());
			super.visit(objectPropertyAxiom);
		}

		@Override
		public void visit(OWLAnnotationAssertionAxiom axiom) {
			logger.log(INFO, "OWLAnnotationAssertionAxiom:{0}", axiom.toString());

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

			context.setStateWithEntity(referenceId);
			super.visit(axiom);
			annotationsBySubject.computeIfAbsent(context.getIri(), anIri -> new ArrayList<>())
					.add(axiom.getAnnotation());
			context.statePop();
		}

		@Override
		public void visit(OWLAnnotationProperty property) {
			logger.log(INFO, "OWLAnnotationProperty:{0}", property.toString());
			super.visit(property);
		}

		@Override
		public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
			logger.log(INFO, "OWLAnnotationPropertyDomainAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
			logger.log(INFO, "OWLAnnotationPropertyRangeAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLAnonymousIndividual individual) {
			logger.log(INFO, "OWLAnonymousIndividual:{0}", individual.toString());
			super.visit(individual);
		}

		@Override
		public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
			logger.log(INFO, "OWLAsymmetricObjectPropertyAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLClassAssertionAxiom axiom) {
			logger.log(INFO, "OWLClassAssertionAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDataAllValuesFrom desc) {
			logger.log(INFO, "OWLDataAllValuesFrom:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLDataComplementOf node) {
			logger.log(INFO, "OWLDataComplementOf:{0}", node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLDataExactCardinality desc) {
			logger.log(INFO, "OWLDataExactCardinality:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLDataHasValue desc) {
			logger.log(INFO, "OWLDataHasValue:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLDataIntersectionOf node) {
			logger.log(INFO, "OWLDataIntersectionOf:{0}", node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLDataMaxCardinality desc) {
			logger.log(INFO, "OWLDataMaxCardinality:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLDataMinCardinality desc) {
			logger.log(INFO, "OWLDataMinCardinality:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLDataOneOf node) {
			logger.log(INFO, "OWLDataOneOf:{0}", node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLDataProperty property) {
			logger.log(INFO, "OWLDataProperty:{0}", property.toString());
			super.visit(property);
		}

		@Override
		public void visit(OWLDataPropertyAssertionAxiom axiom) {
			logger.log(INFO, "OWLDataPropertyAssertionAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDataPropertyDomainAxiom axiom) {
			logger.log(INFO, "OWLDataPropertyDomainAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDataPropertyRangeAxiom axiom) {
			logger.log(INFO, "OWLDataPropertyRangeAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDatatype node) {
			logger.log(INFO, "OWLDatatype:{0}", node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLDatatypeDefinitionAxiom axiom) {
			logger.log(INFO, "OWLDatatypeDefinitionAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDatatypeRestriction node) {
			logger.log(INFO, "OWLDatatypeRestriction:{0}", node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLDataUnionOf node) {
			logger.log(INFO, "OWLDataUnionOf:{0}", node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLDeclarationAxiom axiom) {
			logger.log(INFO, "OWLDeclarationAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDifferentIndividualsAxiom axiom) {
			logger.log(INFO, "OWLDifferentIndividualsAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDisjointClassesAxiom axiom) {
			logger.log(INFO, "OWLDisjointClassesAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDisjointDataPropertiesAxiom axiom) {
			logger.log(INFO, "OWLDisjointDataPropertiesAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
			logger.log(INFO, "OWLDisjointObjectPropertiesAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLDisjointUnionAxiom axiom) {
			logger.log(INFO, "OWLDisjointUnionAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLEquivalentClassesAxiom axiom) {
			logger.log(INFO, "OWLEquivalentClassesAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
			logger.log(INFO, "OWLEquivalentDataPropertiesAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
			logger.log(INFO, "OWLEquivalentObjectPropertiesAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLFacetRestriction node) {
			logger.log(INFO, "OWLFacetRestriction:{0}", node.toString());
			super.visit(node);
		}

		@Override
		public void visit(OWLFunctionalDataPropertyAxiom axiom) {
			logger.log(INFO, "axiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
			logger.log(INFO, "OWLFunctionalObjectPropertyAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLHasKeyAxiom axiom) {
			logger.log(INFO, "OWLHasKeyAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
			logger.log(INFO, "OWLInverseFunctionalObjectPropertyAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLInverseObjectPropertiesAxiom axiom) {
			logger.log(INFO, "OWLInverseObjectPropertiesAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
			logger.log(INFO, "OWLIrreflexiveObjectPropertyAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLLiteral literal) {
			logger.log(INFO, "OWLLiteral:{0}", literal.toString());
			super.visit(literal);
		}

		@Override
		public void visit(OWLNamedIndividual individual) {
			logger.log(INFO, "OWLNamedIndividual:{0}", individual.toString());
			super.visit(individual);
		}

		@Override
		public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
			logger.log(INFO, "OWLNegativeDataPropertyAssertionAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
			logger.log(INFO, "OWLNegativeObjectPropertyAssertionAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLObjectAllValuesFrom desc) {
			logger.log(INFO, "OWLObjectAllValuesFrom:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectComplementOf desc) {
			logger.log(INFO, "OWLObjectComplementOf:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectExactCardinality desc) {
			logger.log(INFO, "OWLObjectExactCardinality:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectHasSelf desc) {
			logger.log(INFO, "OWLObjectHasSelf:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectHasValue desc) {
			logger.log(INFO, "OWLObjectHasValue:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectIntersectionOf desc) {
			logger.log(INFO, "OWLObjectIntersectionOf:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectInverseOf property) {
			logger.log(INFO, "OWLObjectInverseOf:{0}", property.toString());
			super.visit(property);
		}

		@Override
		public void visit(OWLObjectMaxCardinality desc) {
			logger.log(INFO, "OWLObjectMaxCardinality:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectMinCardinality desc) {
			logger.log(INFO, "OWLObjectMinCardinality:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectOneOf desc) {
			logger.log(INFO, "OWLObjectOneOf:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLObjectPropertyDomainAxiom axiom) {
			logger.log(INFO, "OWLObjectPropertyDomainAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLObjectPropertyRangeAxiom axiom) {
			logger.log(INFO, "OWLObjectPropertyRangeAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLObjectUnionOf desc) {
			logger.log(INFO, "OWLObjectUnionOf:{0}", desc.toString());
			super.visit(desc);
		}

		@Override
		public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
			logger.log(INFO, "OWLReflexiveObjectPropertyAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLSameIndividualAxiom axiom) {
			logger.log(INFO, "OWLSameIndividualAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
			logger.log(INFO, "OWLSubAnnotationPropertyOfAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLSubDataPropertyOfAxiom axiom) {
			logger.log(INFO, "OWLSubDataPropertyOfAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLSubObjectPropertyOfAxiom axiom) {
			logger.log(INFO, "OWLSubObjectPropertyOfAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLSubPropertyChainOfAxiom axiom) {
			logger.log(INFO, "OWLSubPropertyChainOfAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
			logger.log(INFO, "OWLSymmetricObjectPropertyAxiom:{0}", axiom.toString());
			super.visit(axiom);
		}

		@Override
		public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
			logger.log(INFO, "OWLTransitiveObjectPropertyAxiom:{0}", axiom.toString());
			super.visit(axiom);
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
		ParsingStructureWalker parsingStructureWalker = new ParsingStructureWalker(owlObjectWalker, owlOntologyManager,
				context);
		parsingStructureWalker.visit(owlOntology);

		Map<String, Term> terms2 = context.getTerms();
		logger.log(Level.INFO, () -> "Terms count:" + terms2.values().size());

		logger.log(Level.INFO, "The End!");
		return new ParseContextImpl(terms2.values(), context.getDatasources(), context.getRelationshipTypes(),
				context.getAnnotationTypes());
	}

}
