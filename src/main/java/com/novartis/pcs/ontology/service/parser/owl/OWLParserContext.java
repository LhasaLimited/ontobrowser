package com.novartis.pcs.ontology.service.parser.owl;

import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.entity.VersionedEntity;
import org.semanticweb.owlapi.model.IRI;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;


public class OWLParserContext {
	private Deque<ParserState> state = new LinkedList<>();
	private Deque<Term> termStack = new LinkedList<>();
	private RelationshipType relationshipType;
	private IRI iri;
	private AnnotationType annotationType;

	// system
	private Ontology ontology;
	private Curator curator;
	private Version version;

	private Map<String, Datasource> datasources;
	private Map<String, RelationshipType> relationshipTypes;
	private Map<String, AnnotationType> annotationTypes;
	private Map<String, Term> terms;

	public OWLParserContext(final Curator curator, final Version version, final Collection<Datasource> datasources, final Ontology ontology,
							Collection<RelationshipType> relationshipTypes, final Collection<AnnotationType> annotationTypes, final Collection<Term> terms) {
		this.curator = curator;
		this.version = version;
		this.ontology = ontology;
		this.datasources = datasources.stream().collect(Collectors.toMap(Datasource::getAcronym, identity()));
		this.relationshipTypes = relationshipTypes.stream().collect(Collectors.toMap(RelationshipType::getRelationship, identity()));
		this.annotationTypes = annotationTypes.stream().collect(Collectors.toMap(AnnotationType::getPrefixedXmlType, Function.identity()));
		this.terms = terms.stream().collect(Collectors.toMap(Term::getReferenceId, Function.identity()));

	}

	public ParserState statePop(){
		return state.pop();
	}

	public ParserState statePeek(){
		return state.peek();
	}

	public void statePush(ParserState parserState){
		state.push(parserState);
	}

	public void termPush(Term term){
		termStack.push(term);
	}

	public Term termPeek(){
		return termStack.peek();
	}

	public Term termPop(){
		return termStack.pop();
	}

	public RelationshipType getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(final RelationshipType relationshipType) {
		this.relationshipType = relationshipType;
	}

	public IRI getIri() {
		return iri;
	}

	public void setIri(final IRI iri) {
		this.iri = iri;
	}

	public AnnotationType getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(final AnnotationType annotationType) {
		this.annotationType = annotationType;
	}

	public void setDatasources(final Map<String, Datasource> datasources) {
		this.datasources = datasources;
	}

	public void setCurator(final Curator curator) {
		this.curator = curator;
	}

	public Curator getCurator() {
		return curator;
	}

	public Version getVersion() {
		return version;
	}

	public void setVersion(final Version version) {
		this.version = version;
	}

	public Datasource getDatasource(String acronym) {
		return datasources.computeIfAbsent(acronym.toUpperCase(), k -> new Datasource(acronym, acronym, getCurator()));
	}

	public Collection<Datasource> getDatasources() {
		return datasources.values();
	}

	public Ontology getOntology() {
		return ontology;
	}

	public void setOntology(final Ontology ontology) {
		this.ontology = ontology;
	}

	public boolean hasRelationshipType(String relationshipType){
		return relationshipTypes.containsKey(relationshipType);
	}

	public void addRelationshipType(RelationshipType relationshipType) {
		relationshipTypes.put(relationshipType.getRelationship(),relationshipType );
	}

	public RelationshipType getRelationshipType(String relationshipType){
		return relationshipTypes.get(relationshipType);
	}

	public Collection<RelationshipType> getRelationshipTypes() {
		return relationshipTypes.values();
	}


	public AnnotationType getAnnotationType(final String shortForm) {
		return annotationTypes.get(shortForm);
	}

	public boolean hasAnnotationType(final String propertyFragment) {
		return annotationTypes.containsKey(propertyFragment);
	}


	public void addAnnotationType(final AnnotationType annotationType) {
		annotationTypes.put(annotationType.getPrefixedXmlType(), annotationType);
	}

	public Collection<AnnotationType> getAnnotationTypes() {
		return annotationTypes.values();
	}

	public Map<String, Term> getTerms() {
		return terms;
	}

	Term getTerm(String fragment) {
		Term current;
		if (terms.containsKey(fragment)) {
			current = terms.get(fragment);
		} else {
			current = new Term(getOntology(), fragment, fragment, getCurator(), getVersion());
			approve(current);
			terms.put(fragment, current);
		}
		return current;
	}

	void approve(VersionedEntity versionedEntity) {
		versionedEntity.setStatus(VersionedEntity.Status.APPROVED);
		versionedEntity.setApprovedVersion(getVersion());
	}

	public boolean hasTerm(final String referenceId) {
		return terms.containsKey(referenceId);
	}

	void visitPropertyRelationship(String propertyFragment) {
		RelationshipType relationshipType;
		if (hasRelationshipType(propertyFragment)) {
			relationshipType = getRelationshipType(propertyFragment);
		} else {
			// replace with computeIfAbsent
			relationshipType = new RelationshipType(propertyFragment, propertyFragment, propertyFragment, getCurator(),
					getVersion());
			approve(relationshipType);
			addRelationshipType(relationshipType);
		}
		setRelationshipType(relationshipType);
	}

	void visitPropertyAnnotation(String propertyFragment) {
		AnnotationType annotationType;
		if (hasAnnotationType(propertyFragment)) {
			annotationType = getAnnotationType(propertyFragment);
		} else {
			// replace with computeIfAbsent
			annotationType = new AnnotationType(propertyFragment, getCurator(), getVersion());
			approve(annotationType);
			addAnnotationType(annotationType);
		}
		setAnnotationType(annotationType);
	}

	void setStateWithEntity(final String referenceId) {
		if (hasTerm(referenceId)) {
			// if annotation is related to already existing class
			statePush(ParserState.TERM);
			termPush(getTerm(referenceId));
		} else if (hasRelationshipType(referenceId)) {
			setRelationshipType(getRelationshipType(referenceId));
			statePush(ParserState.RELATIONSHIP);
		} else if (hasAnnotationType(referenceId)) {
			setAnnotationType(getAnnotationType(referenceId));
			statePush(ParserState.ANNOTATION_TYPE);
		} else {
			statePush(ParserState.ANNOTATION);
		}
	}
}
