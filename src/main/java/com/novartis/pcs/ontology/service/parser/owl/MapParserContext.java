package com.novartis.pcs.ontology.service.parser.owl;

import static java.util.function.UnaryOperator.identity;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;

public class MapParserContext extends OWLParserContext {

	private Map<String, Datasource> datasources;
	private Map<String, RelationshipType> relationshipTypes;
	private Map<String, AnnotationType> annotationTypes;
	private Map<String, Term> terms;

	public MapParserContext(final Curator curator, final Version version, final Collection<Datasource> datasources,
			final Ontology ontology, Collection<RelationshipType> relationshipTypes,
			final Collection<AnnotationType> annotationTypes, final Collection<Term> terms) {
		super(curator, ontology, version);
		this.datasources = new LinkedHashMap<>(
				datasources.stream().collect(Collectors.toMap(acronym-> acronym.getAcronym().toUpperCase(), identity())));
		this.relationshipTypes = new LinkedHashMap<>(
				relationshipTypes.stream().collect(Collectors.toMap(RelationshipType::getRelationship, identity())));
		this.annotationTypes = new LinkedHashMap<>(annotationTypes.stream()
				.collect(Collectors.toMap(AnnotationType::getPrefixedXmlType, Function.identity())));
		this.terms = new LinkedHashMap<>(
				terms.stream().collect(Collectors.toMap(Term::getReferenceId, Function.identity())));

	}

	@Override
	public void setDatasources(final Map<String, Datasource> datasources) {
		this.datasources = datasources;
	}

	@Override
	public Datasource getDatasource(String acronymRaw) {
		String acronym = acronymRaw.toUpperCase();
		return datasources.computeIfAbsent(acronym, k -> new Datasource(acronym, acronym, getCurator()));
	}

	@Override
	public Collection<Datasource> getDatasources() {
		return datasources.values();
	}

	@Override
	public boolean hasRelationshipType(String relationshipType) {
		return relationshipTypes.containsKey(relationshipType);
	}

	@Override
	public void addRelationshipType(RelationshipType relationshipType) {
		relationshipTypes.put(relationshipType.getRelationship(), relationshipType);
	}

	@Override
	public RelationshipType getRelationshipType(String relationshipType) {
		return relationshipTypes.get(relationshipType);
	}

	@Override
	public Collection<RelationshipType> getRelationshipTypes() {
		return relationshipTypes.values();
	}

	@Override
	public AnnotationType getAnnotationType(final String propertyFragment) {
		return annotationTypes.get(propertyFragment);
	}

	@Override
	public boolean hasAnnotationType(final String propertyFragment) {
		return annotationTypes.containsKey(propertyFragment);
	}

	@Override
	public Collection<AnnotationType> getAnnotationTypes() {
		return annotationTypes.values();
	}

	@Override
	public Map<String, Term> getTerms() {
		return terms;
	}

	@Override
	public Term getTerm(String referenceId) {
		return terms.get(referenceId);
	}

	@Override
	public void putTerm(final String referenceId, final Term term) {
		terms.put(referenceId, term);
	}

	@Override
	public boolean hasTerm(final String referenceId) {
		return terms.containsKey(referenceId);
	}

	@Override
	public void addTerms(Collection<Term> existingTerms) {
		terms.putAll(existingTerms.stream().collect(Collectors.toMap(Term::getReferenceId, Function.identity())));

	}

	@Override
	public void putAnnotationType(final String annotationTypeFragment, final AnnotationType annotationType) {
		annotationTypes.put(annotationTypeFragment, annotationType);
	}

}
