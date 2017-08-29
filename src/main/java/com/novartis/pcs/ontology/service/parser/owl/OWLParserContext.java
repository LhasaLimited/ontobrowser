package com.novartis.pcs.ontology.service.parser.owl;

import static java.util.function.UnaryOperator.identity;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;

import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.entity.VersionedEntity;
import org.semanticweb.owlapi.model.OWLClass;


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

	private final Map<String, Map<String, Set<String>>> relationshipMap = new HashMap<>();

	public OWLParserContext(final Curator curator, final Version version, final Collection<Datasource> datasources, final Ontology ontology,
							Collection<RelationshipType> relationshipTypes, final Collection<AnnotationType> annotationTypes, final Collection<Term> terms) {
		this.curator = curator;
		this.version = version;
		this.ontology = ontology;
		this.datasources = new LinkedHashMap<>(datasources.stream().collect(Collectors.toMap(Datasource::getAcronym, identity())));
		this.relationshipTypes = new LinkedHashMap<>(relationshipTypes.stream().collect(Collectors.toMap(RelationshipType::getRelationship, identity())));
		this.annotationTypes = new LinkedHashMap<>(annotationTypes.stream().collect(Collectors.toMap(AnnotationType::getPrefixedXmlType, Function.identity())));
		this.terms = new LinkedHashMap<>(terms.stream().collect(Collectors.toMap(Term::getReferenceId, Function.identity())));

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


	public AnnotationType getAnnotationType(final String propertyFragment) {
		return annotationTypes.get(propertyFragment);
	}

	public boolean hasAnnotationType(final String propertyFragment) {
		return annotationTypes.containsKey(propertyFragment);
	}


	public Collection<AnnotationType> getAnnotationTypes() {
		return annotationTypes.values();
	}

	public Map<String, Term> getTerms() {
		return terms;
	}

	public Term getTerm(String referenceId) {
		return terms.get(referenceId);
	}

	public void putTerm(final String referenceId, final Term term) {
		terms.put(referenceId, term);
	}
	public void approve(VersionedEntity versionedEntity) {
		versionedEntity.setStatus(VersionedEntity.Status.APPROVED);
		versionedEntity.setApprovedVersion(getVersion());
	}

	public boolean hasTerm(final String referenceId) {
		return terms.containsKey(referenceId);
	}

	void visitPropertyRelationship(String propertyFragment, final Function<String,RelationshipType> relationshipTypeFunction) {
		RelationshipType relationshipType = relationshipTypes.computeIfAbsent(propertyFragment, relationshipTypeFunction);
		setRelationshipType(relationshipType);
	}

	void visitPropertyAnnotation(String propertyFragment, final Function<String, AnnotationType> annotationTypeFunction) {
		AnnotationType annotationType = annotationTypes.computeIfAbsent(propertyFragment, annotationTypeFunction);
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


	public Set<String> getRelationshipTypes(final Term relatedTerm, final Term term) {
		Map<String, Set<String>> relatedTerms = relationshipMap.computeIfAbsent(term.getReferenceId(),
				k -> new HashMap<>());
		return relatedTerms.computeIfAbsent(relatedTerm.getReferenceId(),
				id -> new HashSet<>());
	}
}
