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

import javax.persistence.AssociationOverride;
import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * TermRelationship entity
 */
@Entity
@Table(name = "TERM_RELATIONSHIP", uniqueConstraints = @UniqueConstraint(columnNames = {
		"TERM_ID", "RELATED_TERM_ID", "RELATIONSHIP_TYPE_ID" }))
@AttributeOverride(name = "id", 
		column = @Column(name = "TERM_RELATIONSHIP_ID", unique = true, nullable = false))
@AssociationOverride(name = "curatorActions", joinColumns = @JoinColumn(name = "TERM_RELATIONSHIP_ID"))
@NamedQueries({
		@NamedQuery(name=Relationship.QUERY_BY_RELATED_TERM_ID,
			query="select r from Relationship as r"
					+ " where r.relatedTerm.id = :relatedTermId",
			hints={ @QueryHint(name="org.hibernate.cacheable", value="true") }),
		@NamedQuery(name=Relationship.QUERY_BY_RELATED_TERM_REF_ID,
				query="select r from Relationship as r"
						+ " inner join r.relatedTerm as t"
						+ " where upper(t.referenceId) = :termRefId",
				hints={ @QueryHint(name="org.hibernate.cacheable", value="true") })
})
@SqlResultSetMapping(name="RelationshipHierarchy", 
		entities={@EntityResult(entityClass=Relationship.class)},
		columns={@ColumnResult(name="CONNECT_BY_ISLEAF")})
@NamedNativeQueries({
		@NamedNativeQuery(name=Relationship.QUERY_HIERARCHY,
				query= Relationship.SUBQUERY_IMPORTED_HIERARCHY + Relationship.SUBQUERY_RELATIONSHIP_TYPE +
						" SELECT DISTINCT * FROM (SELECT r.term_relationship_id, CONNECT_BY_ISLEAF"
					+ " FROM term_relationship r"
					+ " WHERE r.status IN ('PENDING','APPROVED')"
					+ " AND r.ontology_id in (select * from imported_hierarchy)"
					+ " START WITH r.term_id = :termId"
					+ " AND r.status IN ('PENDING','APPROVED')"
					+ " AND r.relationship_type_id = (SELECT is_a_id FROM rel_type)"
					+ " CONNECT BY NOCYCLE r.term_id = PRIOR r.related_term_id"
					+ " AND r.status IN ('PENDING','APPROVED')"
					+ " UNION ALL"
					+ " SELECT r.term_relationship_id, CONNECT_BY_ISLEAF"
					+ " FROM term_relationship r"
					+ " WHERE (LEVEL = 1 OR 1 = :deep)"
					+ " AND r.ontology_id in (select * from imported_hierarchy)"
					+ " AND r.status IN ('PENDING','APPROVED')"
					+ " START WITH r.related_term_id = :termId"
					+ " AND r.status IN ('PENDING','APPROVED')"
					+ " CONNECT BY NOCYCLE PRIOR r.term_id = r.related_term_id"
					+ " AND r.status IN ('PENDING','APPROVED')"
					+ " AND r.relationship_type_id = (SELECT is_a_id FROM rel_type)"
					+ " AND ( LEVEL <= 2 OR 1 = :deep))"/*,
				resultSetMapping="RelationshipHierarchy"*/),
		@NamedNativeQuery(name=Relationship.QUERY_HIERARCHY_ONTOLOGY,
				query= Relationship.SUBQUERY_IMPORTED_HIERARCHY +  Relationship.SUBQUERY_RELATIONSHIP_TYPE +
						// @formatter:off
						" SELECT DISTINCT *" +
						" FROM" +
						"  (SELECT r.term_relationship_id," +
						"    CONNECT_BY_ISLEAF" +
						"  FROM term_relationship r" +
						"  WHERE " +
						"  r.status                   IN ('PENDING','APPROVED')" +
						"    START WITH r.related_term_id IN" +  // start with terms not present in term_relationship.term_id
						"    (SELECT t.term_id" +
						"    FROM term t" +
						"    LEFT OUTER JOIN" +
						"      (SELECT *" +
						"      FROM term_relationship tr" +
						"      WHERE tr.status    IN ('PENDING','APPROVED')" +
						"	   AND tr.relationship_type_id = (SELECT is_a_id FROM rel_type)" +
						"      AND tr.ontology_id IN" +
						"        ( SELECT * FROM imported_hierarchy" +
						"        )" +
						"      ) subquery" +
						"    ON t.term_id       = subquery.term_id" +
						"    WHERE t.status    IN ('PENDING','APPROVED')" +
						"    AND t.ontology_id IN" +
						"      ( SELECT * FROM imported_hierarchy" +
						"      )" +
						"    AND subquery.related_term_id IS NULL" +
						"    )" +
						"  AND r.status                        IN ('PENDING','APPROVED')" +
						"    CONNECT BY NOCYCLE PRIOR r.term_id = r.related_term_id" +
						"  AND r.ontology_id                   IN" +
						"    (SELECT * FROM imported_hierarchy" +
						"    )" +
						"  AND r.status IN ('PENDING','APPROVED')" +
						"  AND r.relationship_type_id = (SELECT is_a_id FROM rel_type)" +
						"  AND ( LEVEL <= 0 OR 1 = :deep)" +
						"  )"
						// @formatter:on
				/*, resultSetMapping="RelationshipHierarchy"*/)
})
public class Relationship extends VersionedEntity implements ReplaceableEntity<Relationship> {
	private static final long serialVersionUID = 1L;
	
	public static final String QUERY_BY_RELATED_TERM_ID = "Realationship.loadByRelatedTermId";
	public static final String QUERY_BY_RELATED_TERM_REF_ID = "Realationship.loadByRelatedTermRefId";
	public static final String QUERY_HIERARCHY = "Realationship.loadHierarchy";

	public static final String QUERY_HIERARCHY_ONTOLOGY = "Relationship.loadTermByOntologyName";
	public static final String SUBQUERY_IMPORTED_HIERARCHY = "WITH imported_hierarchy AS" +
			" ( select distinct oi.Imported_Ontology_Id from ontology o join ontology_imported oi on o.ontology_id = oi.ontology_id" +
			" start with o.ontology_name = :ontology_name" +
			" connect by prior Oi.Imported_Ontology_Id = O.Ontology_Id" +
			" UNION select ontology_id from ontology where ontology_name = :ontology_name)";
	public static final String SUBQUERY_RELATIONSHIP_TYPE =
			", rel_type AS (SELECT relationship_type_id AS is_a_id FROM relationship_type WHERE relationship_type = 'is_a') ";

	@NotNull
	@Valid
	@ManyToOne(optional=false)
	@JoinColumn(name = "TERM_ID", nullable = false)
	private Term term;
	
	@NotNull
	@Valid
	@ManyToOne(cascade={CascadeType.PERSIST}, optional=false)
	@JoinColumn(name = "RELATED_TERM_ID", nullable = false)
	private Term relatedTerm;
	
	@NotNull
	@Valid
	@ManyToOne(optional=false)
	@JoinColumn(name = "RELATIONSHIP_TYPE_ID", nullable = false)
	private RelationshipType type;	
	
	@Column(name = "IS_INTERSECTION", nullable = false)
	private boolean intersection;
	
	@Valid
	@ManyToOne(cascade={CascadeType.PERSIST})
	@JoinColumn(name = "REPLACED_BY")
	private Relationship replacedBy;

	@ManyToOne
	@JoinColumn(name = "ONTOLOGY_ID")
	private Ontology ontology;

	@Transient
	private boolean leaf;
	
	protected Relationship() {
	}

	public Relationship(Term term, Term relatedTerm, RelationshipType type,
			Curator creator, Version version) {
    	super(creator, version);
		setTerm(term);
		setRelatedTerm(relatedTerm);
		setType(type);
		
		term.getRelationships().add(this);
	}

	public Term getTerm() {
		return this.term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}
	
	public Term getRelatedTerm() {
		return this.relatedTerm;
	}

	public void setRelatedTerm(Term relatedTerm) {
		this.relatedTerm = relatedTerm;
	}
	
	public RelationshipType getType() {
		return this.type;
	}

	public void setType(RelationshipType type) {
		this.type = type;
	}
	
	public boolean isIntersection() {
		return intersection;
	}

	public void setIntersection(boolean intersection) {
		this.intersection = intersection;
	}

	@Override
	public Relationship getReplacedBy() {
		return replacedBy;
	}

	@Override
	public void setReplacedBy(Relationship replacedBy) {
		this.replacedBy = replacedBy;
	}

	public Ontology getOntology() {
		return ontology;
	}

	public void setOntology(final Ontology ontology) {
		this.ontology = ontology;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	@Override
	public String toString() {
		return term.getName() + " " + type + " " + relatedTerm.getName();
	}
}
