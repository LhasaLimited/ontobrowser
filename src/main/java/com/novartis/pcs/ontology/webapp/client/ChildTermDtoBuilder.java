package com.novartis.pcs.ontology.webapp.client;

import java.util.List;

import com.novartis.pcs.ontology.entity.ControlledVocabularyTerm;
import com.novartis.pcs.ontology.entity.Synonym;

public class ChildTermDtoBuilder {
	private String ontologyName;
	private String termName;
	private String definition;
	private String url;
	private String comments;
	private String relatedTermRefId;
	private String relationshipType;
	private String datasourceAcronym;
	private String referenceId;
	private List<ControlledVocabularyTerm> synonyms;
	private Synonym.Type synonymType;
	private Boolean value;

	public ChildTermDtoBuilder setOntologyName(final String ontologyName) {
		this.ontologyName = ontologyName;
		return this;
	}

	public ChildTermDtoBuilder setTermName(final String termName) {
		this.termName = termName;
		return this;
	}

	public ChildTermDtoBuilder setDefinition(final String definition) {
		this.definition = definition;
		return this;
	}

	public ChildTermDtoBuilder setUrl(final String url) {
		this.url = url;
		return this;
	}

	public ChildTermDtoBuilder setComments(final String comments) {
		this.comments = comments;
		return this;
	}

	public ChildTermDtoBuilder setRelatedTermRefId(final String relatedTermRefId) {
		this.relatedTermRefId = relatedTermRefId;
		return this;
	}

	public ChildTermDtoBuilder setRelationshipType(final String relationshipType) {
		this.relationshipType = relationshipType;
		return this;
	}

	public ChildTermDtoBuilder setDatasourceAcronym(final String datasourceAcronym) {
		this.datasourceAcronym = datasourceAcronym;
		return this;
	}

	public ChildTermDtoBuilder setReferenceId(final String referenceId) {
		this.referenceId = referenceId;
		return this;
	}

	public ChildTermDtoBuilder setSynonyms(final List<ControlledVocabularyTerm> synonyms) {
		this.synonyms = synonyms;
		return this;
	}

	public ChildTermDtoBuilder setSynonymType(final Synonym.Type synonymType) {
		this.synonymType = synonymType;
		return this;
	}

	public ChildTermDtoBuilder setValue(final Boolean value) {
		this.value = value;
		return this;
	}

	public ChildTermDto createChildTermDto() {
		return new ChildTermDto(ontologyName, termName, definition, url, comments, relatedTermRefId, relationshipType,
				datasourceAcronym, referenceId, synonyms, synonymType, value);
	}
}