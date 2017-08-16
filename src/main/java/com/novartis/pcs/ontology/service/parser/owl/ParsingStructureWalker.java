/**
 * Copyright © 2017 Lhasa Limited
 * File created: 09/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.parser.owl;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.service.parser.owl.handlers.AnnotationTypeNameHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.OntologyDescriptionHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.RelationshipTypeNameHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.TermAnnotationHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.TermCommentHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.TermCrossReferenceHandler;
import com.novartis.pcs.ontology.service.parser.owl.handlers.TermDefinitionHandler;
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
	private final Map<String, Map<String, Set<String>>> relationshipMap = new HashMap<>();
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
		handlers.add(new TermAnnotationHandler());

		handlers.add(new RelationshipTypeNameHandler());
		handlers.add(new AnnotationTypeNameHandler());

		handlers.add(new OntologyDescriptionHandler());
	}

	@Override
	public void visit(OWLOntology owlOntology) {
		logger.log(FINE, "OWLOntology:{0}", owlOntology.toString());

		fillSourceProperties(owlOntology);

		Set<OWLClass> owlClasses = owlOntology.getClassesInSignature();
		for (OWLClass owlClass : owlClasses) {
			context.putTerm(owlClass.getIRI().getRemainder().get(), createTerm(owlClass));
		}
		Set<OWLAnnotationProperty> annotationProps = owlOntology.getAnnotationPropertiesInSignature();
		for (OWLAnnotationProperty annotationProp : annotationProps) {
			context.visitPropertyAnnotation(annotationProp.getIRI().getRemainder().orNull(),
					this::createAnnotationType);
		}


		Set<OWLObjectProperty> objectProperties = owlOntology.getObjectPropertiesInSignature();
		for (OWLObjectProperty objectProperty : objectProperties) {
			context.visitPropertyRelationship(objectProperty.getIRI().getRemainder().orNull(),
					this::createRelationshipType);
		}

		Set<OWLDataProperty> dataProperties = owlOntology.getDataPropertiesInSignature();
		for (OWLDataProperty owlDataProperty : dataProperties) {
			context.visitPropertyRelationship(owlDataProperty.getIRI().getRemainder().orNull(),
					this::createRelationshipType);
		}

		context.statePush(ParserState.ONTOLOGY);
		super.visit(owlOntology);
		context.statePop();

		addRootRelationships();
	}

	private void fillSourceProperties(final OWLOntology owlOntology) {
		OWLOntologyID ontologyID = owlOntology.getOntologyID();
		String ontologyIRI = ontologyID.getOntologyIRI().toString();
		context.getOntology().setSourceUri(ontologyIRI);
		context.getOntology().setSourceRelease(ontologyID.getVersionIRI().toString());
		context.getOntology().setSourceNamespace(ontologyIRI.endsWith("/") ? ontologyIRI : ontologyIRI + "#");
	}

	private Term createTerm(OWLClass owlClass) {
		String fragment = owlClass.getIRI().getRemainder().get();
		Term current = new Term(context.getOntology(), fragment, fragment, context.getCurator(), context.getVersion());
		current.setUrl(owlClass.getIRI().toString());
		context.approve(current);
		return current;
	}
	private AnnotationType createAnnotationType(String name) {
		AnnotationType anAnnotationType = new AnnotationType(name, context.getCurator(), context.getVersion());
		context.approve(anAnnotationType);
		return anAnnotationType;
	}

	private RelationshipType createRelationshipType(String propertyFragment) {
		RelationshipType relationshipType = new RelationshipType(propertyFragment, propertyFragment,
				propertyFragment, context.getCurator(), context.getVersion());
		context.approve(relationshipType);
		return relationshipType;
	}



	private void addRootRelationships() {
		Map<String, Term> terms = context.getTerms();
		Term thing = context.getTerm("Thing");
		terms.forEach((termName, term) -> {
			if (term.getRelationships().isEmpty() && !term.equals(thing)) {
				createRelationship(thing, term, context.getRelationshipType("is_a"));
			}
		});
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
			logger.log(INFO, "Duplicated relationship {0} {1} {2}", new String[]{term.getReferenceId(),
					relatedTerm.getReferenceId(), isARelationship.getRelationship()});
		} else {
			relationshipTypesSet.add(isARelationship.getRelationship());
			Relationship relationship = createRelationship(relatedTerm, term, isARelationship);
			term.getRelationships().add(relationship);
		}
	}

	private Relationship createRelationship(final Term relatedTerm, final Term term, final RelationshipType relationshipType) {
		// added to term in constructor
		Relationship relationship = new Relationship(term, relatedTerm, relationshipType, context.getCurator(), context.getVersion());
		context.approve(relationship);
		return relationship;
	}

	@Override
	public void visit(OWLObjectProperty property) {
		logger.log(FINE, "OWLObjectProperty:{0}", property.toString());
		context.statePush(ParserState.OBJECT_PROPERTY);
		super.visit(property);
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
			referenceId = subjectIRI.getRemainder().orNull();
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
/* ---------------------------------------------------------------------*
 * This software is the confidential and proprietary
 * information of Lhasa Limited
 * Granary Wharf House, 2 Canal Wharf, Leeds, LS11 5PY
 * ---
 * No part of this confidential information shall be disclosed
 * and it shall be used only in accordance with the terms of a
 * written license agreement entered into by holder of the information
 * with LHASA Ltd.
 * ---------------------------------------------------------------------*/
 