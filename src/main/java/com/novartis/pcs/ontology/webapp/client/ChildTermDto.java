package com.novartis.pcs.ontology.webapp.client;

import java.io.Serializable;
import java.util.List;

import com.novartis.pcs.ontology.entity.ControlledVocabularyTerm;
import com.novartis.pcs.ontology.entity.Synonym;

public class ChildTermDto implements Serializable {
	private String ontologyName;
	private String termName;
	private String definition;
	private String url;
	private String comments;
	private String relatedTermRefId;
	private String relationshipType;
	private String datasoureAcronym;
	private String referenceId;
	private List<ControlledVocabularyTerm> synonyms;
	private Synonym.Type synonymType;
	private Boolean value;

	public ChildTermDto() {
	}

	public ChildTermDto(final String ontologyName, final String termName, final String definition, final String url,
			final String comments, final String relatedTermRefId, final String relationshipType,
			final String datasoureAcronym, final String referenceId, final List<ControlledVocabularyTerm> synonyms,
			final Synonym.Type synonymType, final Boolean value) {
		this.ontologyName = ontologyName;
		this.termName = termName;
		this.definition = definition;
		this.url = url;
		this.comments = comments;
		this.relatedTermRefId = relatedTermRefId;
		this.relationshipType = relationshipType;
		this.datasoureAcronym = datasoureAcronym;
		this.referenceId = referenceId;
		this.synonyms = synonyms;
		this.synonymType = synonymType;
		this.value = value;
	}

	public String getOntologyName() {
		return ontologyName;
	}

	public String getTermName() {
		return termName;
	}

	public String getDefinition() {
		return definition;
	}

	public String getUrl() {
		return url;
	}

	public String getComments() {
		return comments;
	}

	public String getRelatedTermRefId() {
		return relatedTermRefId;
	}

	public String getRelationshipType() {
		return relationshipType;
	}

	public String getDatasoureAcronym() {
		return datasoureAcronym;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public List<ControlledVocabularyTerm> getSynonyms() {
		return synonyms;
	}

	public Synonym.Type getSynonymType() {
		return synonymType;
	}

	public Boolean getValue() {
		return value;
	}
}
