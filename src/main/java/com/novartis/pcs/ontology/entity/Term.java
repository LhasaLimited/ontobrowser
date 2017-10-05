/* 

Copyright 2015 Novartis Institutes for Biomedical Research

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
package com.novartis.pcs.ontology.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.AssociationOverride;
import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.novartis.pcs.ontology.entity.util.UrlParser;

/**
 * Term entity
 */
@Entity
@Table(name = "TERM", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "TERM_NAME", "ONTOLOGY_ID" }),
		@UniqueConstraint(columnNames = { "REFERENCE_ID", "ONTOLOGY_ID" }) })
@AttributeOverride(name = "id", 
		column = @Column(name = "TERM_ID", unique = true, nullable = false))
@AssociationOverride(name = "curatorActions", joinColumns = @JoinColumn(name = "TERM_ID"))
@NamedQueries({
		@NamedQuery(name=Term.QUERY_ALL,
			query="select t from Term as t"
				+ " where t.ontology = :ontology",
			hints={ @QueryHint(name="org.hibernate.cacheable", value="true") }),
		@NamedQuery(name=Term.QUERY_BY_NAME,
				query="select t from Term as t"
					+ " where lower(t.name) = :name"
					+ " and t.ontology = :ontology",
				hints={ @QueryHint(name="org.hibernate.cacheable", value="true") })
})
@NamedNativeQueries({
		@NamedNativeQuery(name=Term.QUERY_SUBTERMS,
				query="SELECT * FROM term WHERE term_id IN (SELECT DISTINCT term_id" 
					+ " FROM term_relationship"
					+ " WHERE status IN (:status)"
					+ " START WITH related_term_id = (SELECT term_id"
					+ " FROM term WHERE UPPER(reference_id) = :referenceId)"
					+ " AND status IN (:status)"
					+ " CONNECT BY NOCYCLE PRIOR term_id = related_term_id"
					+ " AND status IN (:status))",
				resultClass = Term.class),
		@NamedNativeQuery(name = Term.QUERY_BY_REF_ID, query = Relationship.SUBQUERY_IMPORTED_HIERARCHY
				+ "select * from TERM t where upper(t.reference_id) = :referenceId and t.ontology_id in "
				+ "(select * from imported_hierarchy)", hints = {
						@QueryHint(name = "org.hibernate.cacheable", value = "true") }, resultClass = Term.class),
		@NamedNativeQuery(name = Term.QUERY_NOT_RELATED, query = Relationship.SUBQUERY_IMPORTED_HIERARCHY + Relationship.SUBQUERY_RELATIONSHIP_TYPE
				+ " select * from term t"
				+ "    where"
				+ "    t.ontology_id in (select * from imported_hierarchy) "
				+ "    AND not exists "
				+ "    	( select Tr.term_relationship_id from term_relationship tr "
				+ "    	 WHERE tr.term_id = t.term_id or tr.related_term_id = t.term_id"
				+ "	   AND tr.relationship_type_id = (select is_a_id from rel_type)"
				+ "	  AND tr.ontology_id in (select * from imported_hierarchy))"
				+ " and t.reference_id != 'Thing'"
				+ " AND t.status IN ('APPROVED','PENDING')", resultClass = Term.class) })
public class Term extends VersionedEntity implements ReplaceableEntity<Term> {
	private static final long serialVersionUID = 1L;
	
	public static final String QUERY_ALL = "Term.loadAll";
	public static final String QUERY_BY_REF_ID = "Term.loadByReferenceId";
	public static final String QUERY_BY_NAME = "Term.loadByName";
	public static final String QUERY_SUBTERMS = "Term.loadSubTermsByReferenceId";

	/** Terms without relationships - virtual children of owl:Thing */
	public static final String QUERY_NOT_RELATED = "Term.loadNonRelated";

	@NotNull
	@Valid
	@ManyToOne(optional=false)
	@JoinColumn(name = "ONTOLOGY_ID", nullable = false)
	private Ontology ontology;
	
	@NotNull
	@Column(name = "TERM_NAME", nullable = false)
	private String name;
	
	@NotNull
	@Column(name = "REFERENCE_ID", nullable = false)
	private String referenceId;
	
	@Column(name = "DEFINITION")
	private String definition;
	
	@Column(name = "DEFINITION_URL")
	private String url;
	
	@Column(name = "COMMENTS")
	private String comments;
	
	@Column(name = "IS_ROOT", nullable = false)
	private boolean root;

	@Column(name = "TERM_TYPE")
	@Convert(converter = TermTypeConverter.class)
	private TermType type;
	
	@Valid
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "term",
			cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL)
	private Set<Synonym> synonyms = new HashSet<Synonym>(0);
	
	@Valid
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "term",
			cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL)
	private Set<Relationship> relationships = new HashSet<Relationship>(0);

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "relatedTerm")
	private Set<Relationship> inverseRelationships = new HashSet<Relationship>(0);
	
	@Valid
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "term",
			cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL)
	private Set<CrossReference> crossReferences = new HashSet<CrossReference>(0);

	@Valid
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "term",
			cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL)
	private List<Annotation> annotations = new ArrayList<>(0);

	@Valid
	@ManyToOne(cascade={CascadeType.PERSIST})
	@JoinColumn(name = "REPLACED_BY")
	private Term replacedBy;

	protected Term() {
	}

	public Term(Ontology ontology, String name, String referenceId, 
			Curator creator, Version version) {
    	super(creator, version);
		setOntology(ontology);
		setName(name);
		setReferenceId(referenceId);
		this.type = TermType.CLASS;
	}
	
	public Ontology getOntology() {
		return this.ontology;
	}

	public void setOntology(Ontology ontology) {
		this.ontology = ontology;
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getReferenceId() {
		return this.referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getDefinition() {
		return this.definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}
		
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		if(url != null && url.trim().length() > 0) {
			if (url.startsWith("mailto")) {
				this.url = url;
			} else {
				try {
					this.url = UrlParser.parse(url.trim());
				} catch (Exception e) {
					throw new IllegalArgumentException("Invalid URL: " + e.getMessage());
				}
			}
		} else {
			this.url = null;
		}
	}
	
	public String getComments() {
		return this.comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public boolean isRoot() {
		return this.root;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}

	public TermType getType() {
		return type;
	}

	public void setType(final TermType type) {
		this.type = type;
	}

	public Set<CrossReference> getCrossReferences() {
		return crossReferences;
	}

	public void setCrossReferences(Set<CrossReference> crossReferences) {
		this.crossReferences = crossReferences;
	}

	public Set<Relationship> getRelationships() {
		return this.relationships;
	}

	public void setRelationships(Set<Relationship> relationships) {
		this.relationships = relationships;
	}

	public Set<Relationship> getInverseRelationships() {
		return inverseRelationships;
	}

	public void setInverseRelationships(final Set<Relationship> inverseRelationships) {
		this.inverseRelationships = inverseRelationships;
	}

	public Set<Synonym> getSynonyms() {
		return this.synonyms;
	}

	public void setSynonyms(Set<Synonym> synonyms) {
		this.synonyms = synonyms;
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(final List<Annotation> annotations) {
		this.annotations = annotations;
	}

	@Override
	public Term getReplacedBy() {
		return replacedBy;
	}

	@Override
	public void setReplacedBy(Term replacedBy) {
		this.replacedBy = replacedBy;
	}

	@Override
	public String toString() {
		return getName();
	}
}
