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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AssociationOverride;
import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.QueryHints;

/**
 * Ontology entity
 */
@Entity
@Table(name = "ONTOLOGY", uniqueConstraints = @UniqueConstraint(columnNames = "ONTOLOGY_NAME"))
@AttributeOverride(name = "id", column = @Column(name = "ONTOLOGY_ID", unique = true, nullable = false))
@AssociationOverride(name = "curatorActions", joinColumns = @JoinColumn(name = "ONTOLOGY_ID"))
@NamedEntityGraphs(@NamedEntityGraph(name = Ontology.GRAPH_ONTOLOGY_ALL, attributeNodes = {
		@NamedAttributeNode("importedOntologies"), @NamedAttributeNode("importedBy"),
		@NamedAttributeNode("aliases") }))
@NamedQueries({
		@NamedQuery(name = Ontology.QUERY_BY_NAME, query = "select o from Ontology as o where o.name = :name", hints = {
				@QueryHint(name = "org.hibernate.cacheable", value = "true") }),
		@NamedQuery(name = Ontology.LOAD_ALL_NON_INTERMEDIATE,
				query = "select distinct o from Ontology o",
				hints	= {
				@QueryHint(name = QueryHints.FETCHGRAPH, value = Ontology.GRAPH_ONTOLOGY_ALL),
				}),
		@NamedQuery(name = Ontology.QUERY_BY_ALIAS, query = "SELECT distinct o FROM Ontology AS o LEFT JOIN o.aliases a WHERE"
				+ " o.intermediate = false" + " AND ( a.aliasUrl = :alias " + " OR o.sourceUri = :alias"
				+ " OR o.sourceRelease = :alias)", hints = {
						@QueryHint(name = "org.hibernate.cacheable", value = "true"),
						@QueryHint(name = QueryHints.FETCHGRAPH, value = Ontology.GRAPH_ONTOLOGY_ALL) }),
		@NamedQuery(name = Ontology.QUERY_NON_INTERMEDIATE, query = "FROM Ontology o WHERE"
				+ " o.intermediate = false AND o != :current", hints = {
						@QueryHint(name = "org.hibernate.cacheable", value = "true"),
						@QueryHint(name = QueryHints.LOADGRAPH, value = Ontology.GRAPH_ONTOLOGY_ALL) }) })
@NamedNativeQueries({ @NamedNativeQuery(name = Ontology.QUERY_IMPORT_CLOSURE, query = Relationship.SUBQUERY_IMPORTED_HIERARCHY
		+ " SELECT ont.* FROM ontology ont WHERE ont.ontology_id IN ( select * from imported_hierarchy )", resultClass = Ontology.class) })
public class Ontology extends VersionedEntity implements ReplaceableEntity<Ontology> {
	private static final long serialVersionUID = 1L;

	public static final String QUERY_BY_NAME = "Ontology.loadByName";
	public static final String QUERY_BY_ALIAS = "Ontology.queryByAlias";
	public static final String QUERY_NON_INTERMEDIATE = "Ontology.queryAliased";
	public static final String GRAPH_ONTOLOGY_ALL = "Ontology.graphAll";
	public static final String LOAD_ALL_NON_INTERMEDIATE = "Ontology.loadAllNonIntermediate";
	public static final String QUERY_IMPORT_CLOSURE = "Ontology.queryImportClosure";

	@NotNull
	@Column(name = "ONTOLOGY_NAME", unique = true, nullable = false)
	private String name;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "IS_INTERNAL", nullable = false)
	private boolean internal;

	@Column(name = "SOURCE_NAMESPACE")
	private String sourceNamespace;

	@Column(name = "SOURCE_URI")
	private String sourceUri;

	@Column(name = "SOURCE_RELEASE")
	private String sourceRelease;

	@Temporal(TemporalType.DATE)
	@Column(name = "SOURCE_DATE")
	private Date sourceDate;

	@Column(name = "SOURCE_FORMAT")
	private String sourceFormat;

	@Column(name = "REFERENCE_ID_PREFIX")
	private String referenceIdPrefix;

	@Column(name = "REFERENCE_ID_VALUE")
	private int referenceIdValue;

	@Column(name = "IS_CODELIST", nullable = false)
	private boolean codelist;

	@Column(name = "IS_INTERMEDIATE", nullable = false)
	private boolean intermediate;

	@Valid
	@ManyToOne(cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "REPLACED_BY")
	private Ontology replacedBy;

	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "ONTOLOGY_IMPORTED",
			joinColumns = @JoinColumn(name = "ONTOLOGY_ID"),
			inverseJoinColumns = @JoinColumn(name = "IMPORTED_ONTOLOGY_ID"))
	@Fetch(FetchMode.SELECT)
	private Set<Ontology> importedOntologies;

	@ManyToMany
	@JoinTable(name = "ONTOLOGY_IMPORTED",
			joinColumns = @JoinColumn(name = "IMPORTED_ONTOLOGY_ID", insertable = false, updatable = false),
			inverseJoinColumns = @JoinColumn(name = "ONTOLOGY_ID", insertable = false, updatable = false))
	private Set<Ontology> importedBy;

	@OneToMany(mappedBy = "ontology", cascade = { CascadeType.ALL })
	private Set<OntologyAlias> aliases = new HashSet<>();

	protected Ontology() {
	}

	public Ontology(String name, Curator creator, Version version) {
    	super(creator, version);
		setName(name);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isInternal() {
		return this.internal;
	}

	public void setInternal(boolean internal) {
		this.internal = internal;
	}

	public String getSourceNamespace() {
		return this.sourceNamespace;
	}

	public void setSourceNamespace(String sourceNamespace) {
		this.sourceNamespace = sourceNamespace;
	}

	public String getSourceUri() {
		return this.sourceUri;
	}

	public void setSourceUri(String sourceUri) {
		this.sourceUri = sourceUri;
	}

	public String getSourceRelease() {
		return this.sourceRelease;
	}

	public void setSourceRelease(String sourceRelease) {
		this.sourceRelease = sourceRelease;
	}

	public Date getSourceDate() {
		return this.sourceDate;
	}

	public void setSourceDate(Date sourceDate) {
		this.sourceDate = sourceDate;
	}

	public String getSourceFormat() {
		return this.sourceFormat;
	}

	public void setSourceFormat(String sourceFormat) {
		this.sourceFormat = sourceFormat;
	}

	public String getReferenceIdPrefix() {
		return referenceIdPrefix;
	}

	public void setReferenceIdPrefix(String referenceIdPrefix) {
		this.referenceIdPrefix = referenceIdPrefix;
	}

	public int getReferenceIdValue() {
		return referenceIdValue;
	}

	public void setReferenceIdValue(int referenceIdValue) {
		this.referenceIdValue = referenceIdValue;
	}

	public boolean isCodelist() {
		return codelist;
	}

	public void setCodelist(boolean codelist) {
		this.codelist = codelist;
	}

	public boolean isIntermediate() {
		return intermediate;
	}

	public void setIntermediate(final boolean intermediate) {
		this.intermediate = intermediate;
	}

	public Set<Ontology> getImportedOntologies() {
		return importedOntologies;
	}

	public void setImportedOntologies(final Set<Ontology> importedOntologies) {
		this.importedOntologies = importedOntologies;
	}

	public Set<Ontology> getImportedBy() {
		return importedBy;
	}

	public void setImportedBy(final Set<Ontology> importedBy) {
		this.importedBy = importedBy;
	}

	public Set<OntologyAlias> getAliases() {
		return aliases;
	}

	public void setAliases(final Set<OntologyAlias> aliases) {
		this.aliases = aliases;
	}

	@Override
	public Ontology getReplacedBy() {
		return replacedBy;
	}

	@Override
	public void setReplacedBy(Ontology replacedBy) {
		this.replacedBy = replacedBy;
	}

	@Override
	public String toString() {
		return getName();
	}
}
