/*

Copyright 2017 Lhasa Limited

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package com.novartis.pcs.ontology.service.parser.owl;

import static com.novartis.pcs.ontology.service.export.ReferenceIdProvider.getRefId;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag.TAG_IS_A;
import static org.semanticweb.owlapi.vocab.OWLRDFVocabulary.OWL_THING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.ClassExpressionType;
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
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
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
import org.semanticweb.owlapi.model.OWLEntity;
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
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.util.OWLObjectWalker;
import org.semanticweb.owlapi.util.StructureWalker;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.novartis.pcs.ontology.entity.Annotation;
import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.PropertyType;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.TermType;
import com.novartis.pcs.ontology.service.parser.owl.handlers.AnnotationTypeNameHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.OntologyDescriptionHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.RelationshipTypeNameHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.TermAnnotationHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.TermCommentHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.TermCrossReferenceHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.TermDefinitionHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.TermDeprecatedHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.TermLabelHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.TermReplacedByHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.TermSynonymHandler;

/**
 * @author Artur Polit
 * @since 09/08/2017
 */
class ParsingStructureWalker extends StructureWalker<OWLOntology> {
	private final Logger logger = Logger.getLogger(getClass().getName());

	private final OWLParserContext context;
	// indexes of entities

	private final Map<IRI, List<OWLAnnotation>> annotationsBySubject = new HashMap<>();

	private final Set<OWLVisitorHandler> handlers = new LinkedHashSet<>();
	// current state

	public ParsingStructureWalker(final OWLObjectWalker<OWLOntology> owlObjectWalker, final OWLParserContext context) {
		super(owlObjectWalker);
		this.context = context;

		handlers.add(new TermLabelHandler());
		handlers.add(new TermCommentHandler());
		handlers.add(new TermCrossReferenceHandler());
		handlers.add(new TermDefinitionHandler());
		handlers.add(new TermReplacedByHandler());
		handlers.add(new TermSynonymHandler());
		handlers.add(new TermDeprecatedHandler());
		handlers.add(new RelationshipTypeNameHandler());
		handlers.add(new AnnotationTypeNameHandler());
		handlers.add(new OntologyDescriptionHandler());
		// the default one as the last
		handlers.add(new TermAnnotationHandler());
	}

	@Override
	public void visit(OWLOntology owlOntology) {
		logger.log(FINE, "OWLOntology:{0}", owlOntology.toString());

		fillSourceProperties(owlOntology);

		try {
			Set<OWLClass> owlClasses = owlOntology.getClassesInSignature();
			for (OWLClass owlClass : owlClasses) {
				String fragment = getRefId(owlClass);
				if (!context.hasTerm(fragment)) {
					context.putTerm(fragment, createTerm(owlClass));
				}
			}

			Set<OWLNamedIndividual> individuals = owlOntology.getIndividualsInSignature();
			for (OWLNamedIndividual individual : individuals) {
				String fragment = getRefId(individual);
				if (!context.hasTerm(fragment)) {
					context.putTerm(fragment, createTerm(individual));
				}
			}

			Set<OWLAnnotationProperty> annotationProps = owlOntology.getAnnotationPropertiesInSignature();
			for (OWLAnnotationProperty annotationProp : annotationProps) {
				String annotationTypeFragment = getRefId(annotationProp);
				if (!context.hasAnnotationType(annotationTypeFragment)) {
					context.putAnnotationType(annotationTypeFragment, createAnnotationType(annotationProp));
				}
			}

			Set<OWLObjectProperty> objectProperties = owlOntology.getObjectPropertiesInSignature();
			for (OWLObjectProperty objectProperty : objectProperties) {
				String relationshipTypeFragment = getRefId(objectProperty);
				if (!context.hasRelationshipType(relationshipTypeFragment)) {
					context.addRelationshipType(createRelationshipType(objectProperty));
				}
			}

			Set<OWLDataProperty> dataProperties = owlOntology.getDataPropertiesInSignature();
			for (OWLDataProperty owlDataProperty : dataProperties) {
				String annotationTypeFragment = getRefId(owlDataProperty);
				if (!context.hasAnnotationType(annotationTypeFragment)) {
					AnnotationType annotationType = createAnnotationType(owlDataProperty);
					annotationType.setType(PropertyType.DATA_PROPERTY);
					context.putAnnotationType(annotationTypeFragment, annotationType);
				}
			}
		} catch (InvalidEntityException e) {
			throw new ParsingException("Error during visiting signature objects", e);
		}

		context.statePush(ParserState.ONTOLOGY);
		super.visit(owlOntology);
		context.statePop();

	}

	private void fillSourceProperties(final OWLOntology owlOntology) {
		OWLOntologyID ontologyID = owlOntology.getOntologyID();
		Ontology ontology = context.getOntology();
		Optional<IRI> ontologyIRI = ontologyID.getOntologyIRI();
		if (ontologyIRI.isPresent()) {
			String sourceUriStr = ontologyIRI.get().toString();
			ontology.setSourceNamespace(sourceUriStr.endsWith("/") ? sourceUriStr : sourceUriStr + "#");
			ontology.setSourceUri(sourceUriStr);
		}
		Optional<IRI> versionIRI = ontologyID.getVersionIRI().or(ontologyIRI);
		if (versionIRI.isPresent()) {
			ontology.setSourceRelease(versionIRI.get().toString());
		}
	}

	private Term createTerm(OWLClass owlClass) {
		String fragment = getRefId(owlClass);
		Term current = new Term(context.getOntology(), fragment, fragment, context.getCurator(), context.getVersion());
		current.setUrl(owlClass.getIRI().toString());
		context.approve(current);
		return current;
	}

	private Term createTerm(OWLNamedIndividual owlIndividual) {
		String fragment = getRefId(owlIndividual);
		Term current = new Term(context.getOntology(), fragment, fragment, context.getCurator(), context.getVersion());
		current.setUrl(owlIndividual.getIRI().toString());
		current.setType(TermType.INDIVIDUAL);
		context.approve(current);
		return current;
	}

	private AnnotationType createAnnotationType(OWLEntity annotationProp) {
		AnnotationType anAnnotationType = new AnnotationType(getRefId(annotationProp), context.getCurator(),
				context.getVersion());
		anAnnotationType.setOntology(context.getOntology());
		anAnnotationType.setDefinitionUrl(annotationProp.getIRI().toString());
		context.approve(anAnnotationType);
		return anAnnotationType;
	}

	private RelationshipType createRelationshipType(final OWLEntity owlEntity) {
		String refId = getRefId(owlEntity);
		RelationshipType relationshipType = new RelationshipType(refId, refId, refId,
				context.getCurator(), context.getVersion());
		relationshipType.setOntology(context.getOntology());
		relationshipType.setUrl(owlEntity.getIRI().toString());
		context.approve(relationshipType);
		return relationshipType;
	}

	@Override
	public void visit(OWLAnnotation owlAnnotation) {
		for (OWLVisitorHandler handler : handlers) {
			if (handler.match(context, owlAnnotation)) {
				handler.handleAnnotation(context, owlAnnotation);
				break;
			}
		}
		logger.log(FINE, "OWLAnnotation:{0}", owlAnnotation.toString());
	}

	@Override
	public void visit(OWLClass owlClass) {
		logger.log(FINE, "OWLClass:{0}", owlClass.toString());
		Term current = context.getTerm(getRefId(owlClass));
		context.termPush(current);
		context.statePush(ParserState.TERM);
		super.visit(owlClass);
		context.statePop();
	}

	@Override
	public void visit(final OWLNamedIndividual individual) {
		logger.log(FINE, "OWLNamedIndividual:{0}", individual.toString());
		Term current = context.getTerm(getRefId(individual));
		context.termPush(current);
		context.statePush(ParserState.TERM);
		super.visit(individual);
		context.statePop();
	}

	@Override
	public void visit(OWLSubClassOfAxiom subClassAxiom) {
		logger.log(FINE, "OWLSubClassOfAxiom:{0}", subClassAxiom.toString());
		ClassExpressionType classExpressionType = subClassAxiom.getSuperClass().getClassExpressionType();
		if (ClassExpressionType.OWL_CLASS.equals(classExpressionType)) {
			super.visit(subClassAxiom);
			// do not inline, keep in order!
			Term relatedTerm = context.termPop();
			Term term = context.termPop();

			createIsARelationship(relatedTerm, term);
		} else if (ClassExpressionType.OBJECT_SOME_VALUES_FROM.equals(classExpressionType)) {
			super.visit(subClassAxiom);
			// do not inline, keep in order!
			Term relatedTerm = context.termPop();
			Term term = context.termPop();
			createRelationship(relatedTerm, term, context.getRelationshipType(), context.getOntology());
		} else {
			logger.log(INFO, "Only OWLClass is supported [classExpression={0}]", subClassAxiom.getSuperClass().toString());
		}
	}

	private void createIsARelationship(final Term relatedTerm, final Term term) {
		RelationshipType isARelationship = context.getRelationshipType(TAG_IS_A.getTag());
		if (relatedTerm.getReferenceId().equalsIgnoreCase(OWL_THING.getShortForm())) {
			logger.log(INFO, "Subclass to Thing dropped for {0} to {1}", new String[] { term.getReferenceId(), relatedTerm.getReferenceId() });
		} else {
			createRelationship(relatedTerm, term, isARelationship, context.getOntology());
		}
	}

	private Relationship createRelationship(final Term relatedTerm, final Term term, final RelationshipType relationshipType, final Ontology ontology) {
		// added to term in constructor
		Set<String> relationshipTypesSet = context.getRelationshipTypes(relatedTerm, term);
		if (relationshipTypesSet.contains(relationshipType.getRelationship())) {
			logger.log(INFO, "Duplicated relationship {0} {1} {2}",
					new String[] { term.getReferenceId(), relatedTerm.getReferenceId(), relationshipType.getRelationship() });
			return null;
		}
		relationshipTypesSet.add(relationshipType.getRelationship());
		Relationship relationship = new Relationship(term, relatedTerm, relationshipType, context.getCurator(), context.getVersion());
		relationship.setOntology(ontology);
		context.approve(relationship);
		return relationship;
	}

	@Override
	public void visit(OWLObjectProperty property) {
		logger.log(FINE, "OWLObjectProperty:{0}", property.toString());
		context.setRelationshipType(context.getRelationshipType(getRefId(property.getIRI())));
		super.visit(property);
	}

	@Override
	public void visit(IRI iri) {
		logger.log(Level.FINE, "IRI:{0}", iri.toQuotedString());
		context.setIri(iri);
		super.visit(iri);
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom desc) {
		logger.log(FINE, "OWLObjectSomeValuesFrom:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLDataSomeValuesFrom desc) {
		logger.log(FINE, "OWLDataSomeValuesFrom:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom objectPropertyAxiom) {
		logger.log(FINE, "OWLObjectPropertyAssertionAxiom:{0}", objectPropertyAxiom.toString());
		super.visit(objectPropertyAxiom);
		Term relatedTerm = context.termPop();
		Term term = context.termPop();
		createRelationship(relatedTerm, term, context.getRelationshipType(), context.getOntology());
	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		logger.log(FINE, "OWLAnnotationAssertionAxiom:{0}", axiom.toString());

		String referenceId = null;
		if (axiom.getSubject() instanceof IRI) {
			IRI subjectIRI = (IRI) axiom.getSubject();
			referenceId = getRefId(subjectIRI);
		}

		if (referenceId == null) {
			return;
		}

		context.setStateWithEntity(referenceId);
		super.visit(axiom);
		annotationsBySubject.computeIfAbsent(context.getIri(), anIri -> new ArrayList<>()).add(axiom.getAnnotation());
		context.statePop();
	}

	@Override
	public void visit(OWLAnnotationProperty property) {
		logger.log(FINE, "OWLAnnotationProperty:{0}", property.toString());
		super.visit(property);
	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		logger.log(FINE, "OWLAnnotationPropertyDomainAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
		logger.log(FINE, "OWLAnnotationPropertyRangeAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLAnonymousIndividual individual) {
		logger.log(FINE, "OWLAnonymousIndividual:{0}", individual.toString());
		super.visit(individual);
	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		logger.log(FINE, "OWLAsymmetricObjectPropertyAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		logger.log(FINE, "OWLClassAssertionAxiom:{0}", axiom.toString());

		if (axiom.getIndividual() instanceof OWLNamedIndividual && axiom.getClassExpression() instanceof OWLClass) {
			OWLNamedIndividual namedIndividual = (OWLNamedIndividual) axiom.getIndividual();
			Term individual = context.getTerm(getRefId(namedIndividual));
			Term indClass = context.getTerm(getRefId((OWLClass) axiom.getClassExpression()));
			createIsARelationship(indClass, individual);
		}
	}

	@Override
	public void visit(OWLDataAllValuesFrom desc) {
		logger.log(FINE, "OWLDataAllValuesFrom:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLDataComplementOf node) {
		logger.log(FINE, "OWLDataComplementOf:{0}", node.toString());
		super.visit(node);
	}

	@Override
	public void visit(OWLDataExactCardinality desc) {
		logger.log(FINE, "OWLDataExactCardinality:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLDataHasValue desc) {
		logger.log(FINE, "OWLDataHasValue:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLDataIntersectionOf node) {
		logger.log(FINE, "OWLDataIntersectionOf:{0}", node.toString());
		super.visit(node);
	}

	@Override
	public void visit(OWLDataMaxCardinality desc) {
		logger.log(FINE, "OWLDataMaxCardinality:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLDataMinCardinality desc) {
		logger.log(FINE, "OWLDataMinCardinality:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLDataOneOf node) {
		logger.log(FINE, "OWLDataOneOf:{0}", node.toString());
		super.visit(node);
	}

	@Override
	public void visit(OWLDataProperty property) {
		logger.log(FINE, "OWLDataProperty:{0}", property.toString());
		super.visit(property);
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		logger.log(FINE, "OWLDataPropertyAssertionAxiom:{0}", axiom.toString());
		super.visit(axiom);
		Term term = context.termPeek();
		OWLDataPropertyExpression dataPropertyExpression = axiom.getProperty();
		if (dataPropertyExpression.isAnonymous()) {
			return;
		}
		AnnotationType annotationType = context.getAnnotationType(dataPropertyExpression.asOWLDataProperty().getIRI().getRemainder().get());
		String value = ApiHelper.getString(axiom);
		if (!Strings.isNullOrEmpty(value)) {
			Annotation annotation = new Annotation(value, annotationType, term, context.getCurator(), context.getVersion());
			annotation.setOntology(context.getOntology());
			context.approve(annotation);
			term.getAnnotations().add(annotation);
		} else {
			logger.log(Level.WARNING, "Annotation omitted, value is null [termReferenceId={0}, annotationType={1}]",
					new String[] { term.getReferenceId(), annotationType.getAnnotationType() });
		}
	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		logger.log(FINE, "OWLDataPropertyDomainAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		logger.log(FINE, "OWLDataPropertyRangeAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLDatatype node) {
		logger.log(FINE, "OWLDatatype:{0}", node.toString());
		super.visit(node);
	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
		logger.log(FINE, "OWLDatatypeDefinitionAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLDatatypeRestriction node) {
		logger.log(FINE, "OWLDatatypeRestriction:{0}", node.toString());
		super.visit(node);
	}

	@Override
	public void visit(OWLDataUnionOf node) {
		logger.log(FINE, "OWLDataUnionOf:{0}", node.toString());
		super.visit(node);
	}

	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		logger.log(FINE, "OWLDeclarationAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		logger.log(FINE, "OWLDifferentIndividualsAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		logger.log(FINE, "OWLDisjointClassesAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		logger.log(FINE, "OWLDisjointDataPropertiesAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		logger.log(FINE, "OWLDisjointObjectPropertiesAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		logger.log(FINE, "OWLDisjointUnionAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		logger.log(FINE, "OWLEquivalentClassesAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		logger.log(FINE, "OWLEquivalentDataPropertiesAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		logger.log(FINE, "OWLEquivalentObjectPropertiesAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLFacetRestriction node) {
		logger.log(FINE, "OWLFacetRestriction:{0}", node.toString());
		super.visit(node);
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		logger.log(FINE, "axiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		logger.log(FINE, "OWLFunctionalObjectPropertyAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		logger.log(FINE, "OWLHasKeyAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		logger.log(FINE, "OWLInverseFunctionalObjectPropertyAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		logger.log(FINE, "OWLInverseObjectPropertiesAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		logger.log(FINE, "OWLIrreflexiveObjectPropertyAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLLiteral literal) {
		logger.log(FINE, "OWLLiteral:{0}", literal.toString());
		super.visit(literal);
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		logger.log(FINE, "OWLNegativeDataPropertyAssertionAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		logger.log(FINE, "OWLNegativeObjectPropertyAssertionAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLObjectAllValuesFrom desc) {
		logger.log(FINE, "OWLObjectAllValuesFrom:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLObjectComplementOf desc) {
		logger.log(FINE, "OWLObjectComplementOf:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLObjectExactCardinality desc) {
		logger.log(FINE, "OWLObjectExactCardinality:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLObjectHasSelf desc) {
		logger.log(FINE, "OWLObjectHasSelf:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLObjectHasValue desc) {
		logger.log(FINE, "OWLObjectHasValue:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLObjectIntersectionOf desc) {
		logger.log(FINE, "OWLObjectIntersectionOf:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLObjectInverseOf property) {
		logger.log(FINE, "OWLObjectInverseOf:{0}", property.toString());
		super.visit(property);
	}

	@Override
	public void visit(OWLObjectMaxCardinality desc) {
		logger.log(FINE, "OWLObjectMaxCardinality:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLObjectMinCardinality desc) {
		logger.log(FINE, "OWLObjectMinCardinality:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLObjectOneOf desc) {
		logger.log(FINE, "OWLObjectOneOf:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		logger.log(FINE, "OWLObjectPropertyDomainAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		logger.log(FINE, "OWLObjectPropertyRangeAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLObjectUnionOf desc) {
		logger.log(FINE, "OWLObjectUnionOf:{0}", desc.toString());
		super.visit(desc);
	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		logger.log(FINE, "OWLReflexiveObjectPropertyAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		logger.log(FINE, "OWLSameIndividualAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		logger.log(FINE, "OWLSubAnnotationPropertyOfAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		logger.log(FINE, "OWLSubDataPropertyOfAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		logger.log(FINE, "OWLSubObjectPropertyOfAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		logger.log(FINE, "OWLSubPropertyChainOfAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		logger.log(FINE, "OWLSymmetricObjectPropertyAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		logger.log(FINE, "OWLTransitiveObjectPropertyAxiom:{0}", axiom.toString());
		super.visit(axiom);
	}

}

