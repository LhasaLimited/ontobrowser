package com.novartis.pcs.ontology.service.parser.owl;

import static java.util.function.Function.identity;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import com.novartis.pcs.ontology.entity.AbstractEntity;
import com.novartis.pcs.ontology.entity.Annotation;
import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.CrossReference;
import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Synonym;
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
		ROOT, ONTOLOGY, TERM, OBJECT_PROPERTY, RELATIONSHIP, ANNOTATION, ANNOTATION_TYPE
	}

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
		private OWLOntologyManager owlOntologyManager;
		private Ontology ontology;
		private Curator curator;
		private Version version;

		// indexes of entities
		private Map<String, Term> terms = new HashMap<>();
		private List<Relationship> relationships = new ArrayList<>();
		private Map<String, RelationshipType> relationshipTypes = new HashMap<>();
		private Map<String, Datasource> datasources = new HashMap<>();
		private Map<String, AnnotationType> annotationTypes;
		private Map<String, Map<String, Set<String>>> relationshipMap = new HashMap<>();
		private Map<Term, Set<Pair<Term, Synonym.Type>>> synonymsMap = new HashMap<>();

		private Map<IRI, List<OWLAnnotation>> annotationsBySubject = new HashMap<>();

		// current state
		private Deque<ParserState> state = new LinkedList<>();
		private Deque<Term> termStack = new LinkedList<>();

		private RelationshipType relationshipType;
		private Relationship relationship;
		private IRI iri;

		private AnnotationType annotationType;

		public ParsingStructureWalker(OWLObjectWalker<OWLOntology> owlObjectWalker,
				OWLOntologyManager owlOntologyManager, Collection<RelationshipType> relationshipTypes, Curator curator,
				Version version, Ontology ontology, final Collection<Datasource> datasources,
				final Collection<AnnotationType> annotationTypes, final Collection<Term> terms) {
			super(owlObjectWalker);
			this.owlOntologyManager = owlOntologyManager;
			this.curator = curator;
			this.version = version;
			this.ontology = ontology;
			this.relationshipTypes = relationshipTypes.stream()
					.collect(Collectors.toMap(RelationshipType::getRelationship, identity()));
			this.datasources = datasources.stream().collect(Collectors.toMap(Datasource::getAcronym, identity()));
			this.annotationTypes = annotationTypes.stream()
					.collect(Collectors.toMap(AnnotationType::getPrefixedXmlType, identity()));
			this.terms = terms.stream().collect(Collectors.toMap(Term::getReferenceId, identity()));

		}

		@Override
		public void visit(OWLOntology owlOntology) {
			logger.log(FINE, "OWLOntology:{0}", owlOntology.toString());
			IRI documentIRI = owlOntologyManager.getOntologyDocumentIRI(owlOntology);
			ontology.setSourceUri(documentIRI.toString());

			Set<OWLClass> owlClasses = owlOntology.getClassesInSignature();
			for (OWLClass owlClass : owlClasses) {
				Optional<String> remainder = owlClass.getIRI().getRemainder();
				if (remainder.isPresent()) {
					String referenceId = remainder.get();
					getTerm(referenceId);
				}
			}
			Set<OWLAnnotationProperty> annotationProps = owlOntology.getAnnotationPropertiesInSignature();
			for (OWLAnnotationProperty annotationProp : annotationProps) {
				visitPropertyAnnotation(annotationProp.getIRI().getRemainder().orNull());
			}

			Set<OWLObjectProperty> objectProperties = owlOntology.getObjectPropertiesInSignature();
			for (OWLObjectProperty objectProperty : objectProperties) {
				visitPropertyRelationship(objectProperty.getIRI().getRemainder().orNull());
			}

			Set<OWLDataProperty> dataProperties = owlOntology.getDataPropertiesInSignature();
			for (OWLDataProperty owlDataProperty : dataProperties) {
				visitPropertyRelationship(owlDataProperty.getIRI().getRemainder().orNull());
			}

			state.push(ParserState.ONTOLOGY);
			super.visit(owlOntology);
			state.pop();

			Term thing = getTerm("Thing");
			terms.forEach((termName, term) -> {
				if (term.getRelationships().isEmpty() && !term.equals(thing)) {
					// added to term in constructor
					approve(new Relationship(term, thing, relationshipTypes.get("is_a"), curator, version));
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
						logger.log(INFO, "Duplicated relationship {0} {1} {2}", new String[] { term.getName(),
								aRelationship.getRelatedTerm().getName(), aRelationship.getType().getRelationship() });
					} else {
						types.add(relationshipTypeId);
					}
				}
			});

			synonymsMap.forEach((term, synonymTerms) -> {
				for (Pair<Term, Synonym.Type> synonymTerm : synonymTerms) {
					Synonym synonym = new Synonym(term, synonymTerm.getLeft().getName(), synonymTerm.getRight(),
							curator, version);
					approve(synonym);
				}
			});
		}

		@Override
		public void visit(OWLAnnotation owlAnnotation) {
			logger.log(FINE, "OWLAnnotation:{0}", owlAnnotation.toString());
			OWLAnnotationProperty owlAnnotationProperty = owlAnnotation.getProperty();
			ParserState currentState = state.peek();
			if (ParserState.ONTOLOGY.equals(currentState)) {
				if (owlAnnotationProperty.isComment()) {
					String description = appendNonEmpty(ontology.getDescription(), getString(owlAnnotation));
					ontology.setDescription(StringUtils.abbreviate(description, 1024));
				}
			} else if (ParserState.TERM.equals(currentState)) {
				Term term = termStack.peek();
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
							Datasource datasource = getDatasource(splitted[ACRONYM_INDEX]);
							new CrossReference(term, datasource, splitted[REFID_INDEX], curator);
						} else if (string != null) {
							// TS28 from bioontology - without prefix
						}
					} else if (owlAnnotation.getValue() instanceof IRI) {
						new CrossReference(term, iri.toString(), curator);
					}
				} else if (Obo2OWLConstants.Obo2OWLVocabulary.IRI_IAO_0000115.getIRI()
						.equals(owlAnnotationProperty.getIRI())) {
					term.setDefinition(getString(owlAnnotation));
				} else if (Obo2OWLConstants.Obo2OWLVocabulary.IRI_IAO_0100001.getIRI()
						.equals(owlAnnotationProperty.getIRI())) {
					logger.log(INFO, "replaced_by unused in BAO");
				} else if (synonymMap.containsKey(owlAnnotationProperty.getIRI())) {
					if (owlAnnotation.getValue() instanceof IRI) {
						IRI synonymIRI = (IRI) owlAnnotation.getValue();
						Term synonymTerm = getTerm(synonymIRI.getRemainder().orNull());
						Term preferredTerm = termStack.peek();
						Synonym.Type synonymType = synonymMap.get(owlAnnotationProperty.getIRI());
						synonymsMap.computeIfAbsent(preferredTerm, t -> new HashSet<>())
								.add(Pair.of(synonymTerm, synonymType));
					} else if (owlAnnotation.getValue() instanceof OWLLiteral) {
						OWLLiteral literal = (OWLLiteral) owlAnnotation.getValue();
						logger.log(INFO, literal.toString());
					}

				} else {
					AnnotationType annotationType = annotationTypes.get(owlAnnotationProperty.getIRI().getShortForm());

					String value = getString(owlAnnotation);
					Annotation annotation = new Annotation(value, annotationType, term, curator, version);
					approve(annotation);
					term.getAnnotations().add(annotation);
				}
			} else if (ParserState.RELATIONSHIP.equals(currentState)) {
				if (owlAnnotationProperty.isLabel()) {
					relationshipType.setName(getString(owlAnnotation));
				}
			} else if (ParserState.ANNOTATION_TYPE.equals(currentState)){
				if(owlAnnotationProperty.isLabel()) {
					annotationType.setAnnotationType(getString(owlAnnotation));
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
			Term current = getTerm(fragment);
			termStack.push(current);
			state.push(ParserState.TERM);
			super.visit(owlClass);
			state.pop();
		}

		private Term getTerm(String fragment) {
			Term current;
			if (terms.containsKey(fragment)) {
				current = terms.get(fragment);
			} else {
				current = new Term(ontology, fragment /* temporary name */, fragment, curator, version);
				approve(current);
				terms.put(fragment, current);
			}
			return current;
		}

		private void approve(VersionedEntity versionedEntity) {
			versionedEntity.setStatus(Status.APPROVED);
			versionedEntity.setApprovedVersion(version);
		}

		@Override
		public void visit(OWLSubClassOfAxiom subClassAxiom) {
			logger.log(FINE, "OWLSubClassOfAxiom:{0}", subClassAxiom.toString());
			super.visit(subClassAxiom);
			Term relatedTerm = termStack.pop(); // do not inline, keep in order!
			Term term = termStack.pop();

			RelationshipType isARelationship = relationshipTypes.get("is_a");

			Map<String, Set<String>> relatedTerms = relationshipMap.computeIfAbsent(term.getReferenceId(),
					k -> new HashMap<>());
			Set<String> relationshipTypesSet = relatedTerms.computeIfAbsent(relatedTerm.getReferenceId(),
					id -> new HashSet<>());

			if (relationshipTypesSet.contains(isARelationship.getRelationship())) {
				logger.log(INFO, "Duplicated relationship {0} {1} {2}", new String[] { term.getReferenceId(),
						relatedTerm.getReferenceId(), isARelationship.getRelationship() });
			} else {
				relationshipTypesSet.add(isARelationship.getRelationship());
				relationship = new Relationship(term, relatedTerm, isARelationship, curator, version);
				approve(relationship);
				term.getRelationships().add(relationship);
				relationships.add(relationship);
			}
		}

		@Override
		public void visit(OWLObjectProperty property) {
			logger.log(FINE, "OWLObjectProperty:{0}", property.toString());
			state.push(ParserState.OBJECT_PROPERTY);
			super.visit(property);

			String objectPropertyFragment = property.getIRI().getRemainder().orNull();

			visitPropertyRelationship(objectPropertyFragment);
			state.pop();
		}

		private void visitPropertyAnnotation(String propertyFragment) {
			if (annotationTypes.containsKey(propertyFragment)) {
				annotationType = annotationTypes.get(propertyFragment);
			} else {
				// replace with computeIfAbsent
				annotationType = new AnnotationType(propertyFragment, curator, version);
				approve(annotationType);
				annotationTypes.put(propertyFragment, annotationType);
			}
		}
		
		private void visitPropertyRelationship(String propertyFragment) {
			if (relationshipTypes.containsKey(propertyFragment)) {
				relationshipType = relationshipTypes.get(propertyFragment);
			} else {
				// replace with computeIfAbsent
				relationshipType = new RelationshipType(propertyFragment, propertyFragment, propertyFragment, curator,
						version);
				approve(relationshipType);
				relationshipTypes.put(relationshipType.getRelationship(), relationshipType);
			}
		}

		@Override
		public void visit(IRI iri) {
			logger.log(Level.INFO, "IRI:{0}", iri.toQuotedString());
			this.iri = iri;
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

			if (terms.containsKey(referenceId)) {
				// if annotation is related to already existing class
				state.push(ParserState.TERM);
				termStack.push(terms.get(referenceId));
			} else if (relationshipTypes.containsKey(referenceId)) {
				relationshipType = relationshipTypes.get(referenceId);
				state.push(ParserState.RELATIONSHIP);
			} else if (annotationTypes.containsKey(referenceId)) {
				annotationType = annotationTypes.get(referenceId);
				state.push(ParserState.ANNOTATION_TYPE);
			} else {
				state.push(ParserState.ANNOTATION);
			}
			if (axiom.getProperty().isLabel()) {
				String label = getString(axiom.getAnnotation());
				boolean matched = matchLabel(referenceId, label, "name");
				if (!matched) {
					throw new IllegalStateException("There should be no dangling names now");
				}
			}

			if (isIAODefinition(axiom.getProperty())) {
				String definition = getString(axiom.getAnnotation());
				boolean matched = matchLabel(referenceId, definition, "definition");
				if (!matched)
					throw new IllegalStateException("There should be no dangling definition now");
			}

			super.visit(axiom);
			annotationsBySubject.computeIfAbsent(iri, anIri -> new ArrayList<>()).add(axiom.getAnnotation());
			state.pop();
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
			} else if(annotationTypes.containsKey(fragment)) {
				entity = annotationTypes.get(fragment);
			}
			boolean matched = entity != null;

			if (matched) {
				try {
					BeanUtils.setProperty(entity, propertyName, label);
				} catch (IllegalAccessException | InvocationTargetException e) {
					logger.log(Level.SEVERE, "Cannot set [property={},entity={}]",
							new Object[] { propertyName, entity.getId() });
				}
			}
			return matched;
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

		public Ontology getOntology() {
			return ontology;
		}

		public Map<String, Term> getTerms() {
			return terms;
		}

		public Collection<RelationshipType> getRelationshipTypes() {
			return relationshipTypes.values();
		}

		public Datasource getDatasource(String acronym) {
			return datasources.computeIfAbsent(acronym.toUpperCase(), k -> new Datasource(acronym, acronym, curator));
		}

		public Collection<Datasource> getDatasources() {
			return datasources.values();
		}

		public Collection<AnnotationType> getAnnotationTypes() {
			return annotationTypes.values();
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

		ParsingStructureWalker parsingStructureWalker = new ParsingStructureWalker(owlObjectWalker, owlOntologyManager,
				relationshipTypes, curator, version, ontology, datasources, annotationTypes, terms);
		parsingStructureWalker.visit(owlOntology);

		Map<String, Term> terms2 = parsingStructureWalker.getTerms();
		Set<String> termIds = terms2.keySet();
		logger.log(Level.INFO, termIds.toString());

		logger.log(Level.INFO, "The End!");
		return new ParseContextImpl(terms2.values(), parsingStructureWalker.getDatasources(),
				parsingStructureWalker.getRelationshipTypes(), parsingStructureWalker.getAnnotationTypes());
	}

}
