/**
 * Copyright © 2017 Lhasa Limited
 * File created: 10/07/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.entity;

import java.util.Objects;

import javax.persistence.AssociationOverride;
import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.Valid;

/**
 * @author Artur Polit
 * @since 10/07/2017
 */
@Entity
@Table(name = "ANNOTATION_TYPE")
@AttributeOverride(name = "id",
		column = @Column(name = "ANNOTATION_TYPE_ID", unique = true, nullable = false))
@AssociationOverride(name = "curatorActions", joinColumns = @JoinColumn(name = "ANNOTATION_TYPE_ID"))
@NamedQueries({
		@NamedQuery(name = AnnotationType.QUERY_BY_ONTOLOGY, query = "from AnnotationType at where at.ontology = :ontology"),
		@NamedQuery(name = AnnotationType.QUERY_BY_ANNOTATION, query = "from AnnotationType at where at.prefixedXmlType = :annotation") })
public class AnnotationType extends VersionedEntity implements ReplaceableEntity<AnnotationType> {

	public static final String QUERY_BY_ONTOLOGY = "AnnotationType.queryByOntology";
	public static final String QUERY_BY_ANNOTATION = "AnnotationType.queryByAnnotation";

	public AnnotationType(final String prefixedXmlType, final Curator creator, final Version version) {
		super(creator, version);
		this.prefixedXmlType = prefixedXmlType;
		this.annotationType = prefixedXmlType;
	}

	protected AnnotationType() {
	}

	@Valid
	@ManyToOne(cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "REPLACED_BY")
	private AnnotationType replacedBy;

	@Column(name = "ANNOTATION_TYPE")
	private String annotationType;

	@Column(name = "PREFIXED_XML_TYPE")
	private String prefixedXmlType;

	@Column(name = "DEFINITION_URL")
	private String definitionUrl;

	@ManyToOne
	@JoinColumn(name = "ONTOLOGY_ID")
	private Ontology ontology;

	public String getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(String annotationType) {
		this.annotationType = annotationType;
	}

	public String getPrefixedXmlType() {
		return prefixedXmlType;
	}

	public void setPrefixedXmlType(final String prefixedXmlType) {
		this.prefixedXmlType = prefixedXmlType;
	}

	public Ontology getOntology() {
		return ontology;
	}

	public void setOntology(final Ontology ontology) {
		this.ontology = ontology;
	}

	public String getDefinitionUrl() {
		return definitionUrl;
	}

	public void setDefinitionUrl(final String definitionUrl) {
		this.definitionUrl = definitionUrl;
	}

	@Override
	public AnnotationType getReplacedBy() {
		return replacedBy;
	}

	@Override
	public void setReplacedBy(final AnnotationType replacedBy) {
		this.replacedBy = replacedBy;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		final AnnotationType that = (AnnotationType) o;
		return Objects.equals(annotationType, that.annotationType)
				&&
				Objects.equals(prefixedXmlType, that.prefixedXmlType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), annotationType, prefixedXmlType);
	}

	@Override
	public String toString() {
		return "AnnotationType [annotationType=" + annotationType + "]";
	}

	public String getLabel() {
		return (getAnnotationType() == null || getAnnotationType().isEmpty()
		? getPrefixedXmlType()
		: getAnnotationType()) + ":";
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
