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

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;

import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.entity.VersionedEntity;

/**
 * @author Artur Polit
 * @since 15/09/2017
 */
public abstract class OWLParserContext {
	private final Map<String, Map<String, Set<String>>> relationshipMap = new HashMap<>();
	// system
	protected Ontology ontology;
	protected Curator curator;
	protected Version version;
	private Deque<ParserState> state = new LinkedList<>();
	private Deque<Term> termStack = new LinkedList<>();
	private RelationshipType relationshipType;
	private IRI iri;
	private AnnotationType annotationType;

	public OWLParserContext(final Curator curator, final Ontology ontology, final Version version) {
		this.curator = curator;
		this.ontology = ontology;
		this.version = version;
	}

	public ParserState statePop() {
		return state.pop();
	}

	public ParserState statePeek() {
		return state.peek();
	}

	public void statePush(ParserState parserState) {
		state.push(parserState);
	}

	public void termPush(Term term) {
		termStack.push(term);
	}

	public Term termPeek() {
		return termStack.peek();
	}

	public Term termPop() {
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

	public abstract void setDatasources(Map<String, Datasource> datasources);

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

	public abstract Datasource getDatasource(String acronym);

	public abstract Collection<Datasource> getDatasources();

	public Ontology getOntology() {
		return ontology;
	}

	public void setOntology(final Ontology ontology) {
		this.ontology = ontology;
	}

	public abstract boolean hasRelationshipType(String relationshipType);

	public abstract void addRelationshipType(RelationshipType relationshipType) throws InvalidEntityException;

	public abstract RelationshipType getRelationshipType(String relationshipType);

	public abstract Collection<RelationshipType> getRelationshipTypes();

	public abstract AnnotationType getAnnotationType(String propertyFragment);

	public abstract boolean hasAnnotationType(String propertyFragment);

	public abstract Collection<AnnotationType> getAnnotationTypes();

	public abstract Map<String, Term> getTerms();

	public abstract Term getTerm(String referenceId);

	public abstract void putTerm(String referenceId, Term term) throws InvalidEntityException;

	public void approve(VersionedEntity versionedEntity) {
		versionedEntity.setStatus(VersionedEntity.Status.APPROVED);
		versionedEntity.setApprovedVersion(getVersion());
	}

	public abstract boolean hasTerm(String referenceId);

	public abstract void addTerms(Collection<Term> existingTerms);

	public abstract void putAnnotationType(String annotationTypeFragment, AnnotationType annotationType)
			throws InvalidEntityException;

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
				// group existing relations by the related term referenceId,
				// then create a set of relationship types
				refId -> term.getRelationships().stream()
						.collect(Collectors.groupingBy(r -> r.getRelatedTerm().getReferenceId(),
								Collectors.mapping(r -> r.getType().getRelationship(), Collectors.toSet()))));
		return relatedTerms.computeIfAbsent(relatedTerm.getReferenceId(), r -> new HashSet<>());
	}
}
