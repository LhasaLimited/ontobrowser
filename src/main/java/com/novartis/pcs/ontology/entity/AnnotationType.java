/**
 * Copyright © 2017 Lhasa Limited
 * File created: 10/07/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.entity;

import javax.persistence.AssociationOverride;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.Objects;

/**
 * @author Artur Polit
 * @since 10/07/2017
 */
@Entity
@Table
@AttributeOverride(name = "id",
		column = @Column(name = "ANNOTATION_TYPE_ID", unique = true, nullable = false))
@AssociationOverride(name = "curatorActions", joinColumns = @JoinColumn(name = "ANNOTATION_TYPE_ID"))
public class AnnotationType extends VersionedEntity implements ReplaceableEntity<AnnotationType> {

	private AnnotationType replacedBy;

	private String annotation;

	private Ontology sourceOntology;

	private String prefixedXmlType;

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(final String annotation) {
		this.annotation = annotation;
	}

	public Ontology getSourceOntology() {
		return sourceOntology;
	}

	public void setSourceOntology(final Ontology sourceOntology) {
		this.sourceOntology = sourceOntology;
	}

	public String getPrefixedXmlType() {
		return prefixedXmlType;
	}

	public void setPrefixedXmlType(final String prefixedXmlType) {
		this.prefixedXmlType = prefixedXmlType;
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
		return Objects.equals(annotation, that.annotation) &&
				Objects.equals(sourceOntology, that.sourceOntology) &&
				Objects.equals(prefixedXmlType, that.prefixedXmlType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), annotation, sourceOntology, prefixedXmlType);
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
 