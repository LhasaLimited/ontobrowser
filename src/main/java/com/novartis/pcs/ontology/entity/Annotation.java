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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author Artur Polit
 * @since 10/07/2017
 */
@Entity
@Table
@AttributeOverride(name = "id",
		column = @Column(name = "ANNOTATION_ID", unique = true, nullable = false))
@AssociationOverride(name = "curatorActions", joinColumns = @JoinColumn(name = "ANNOTATION_ID"))
public class Annotation extends VersionedEntity implements ReplaceableEntity<Annotation> {

	private String annotation;

	@NotNull
	@Valid
	@ManyToOne(optional=false)
	@JoinColumn(name = "ANNOTATION_TYPE_ID", nullable = false)
	private AnnotationType annotationType;

	@NotNull
	@Valid
	@ManyToOne(optional=false)
	@JoinColumn(name = "TERM_ID", nullable = false)
	private Term term;

	private Annotation replacedBy;

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(final String annotation) {
		this.annotation = annotation;
	}

	@Override
	public Annotation getReplacedBy() {
		return replacedBy;
	}

	@Override
	public void setReplacedBy(final Annotation replacedBy) {
		this.replacedBy = replacedBy;
	}

	public AnnotationType getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(final AnnotationType annotationType) {
		this.annotationType = annotationType;
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(final Term term) {
		this.term = term;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		final Annotation that = (Annotation) o;
		return Objects.equals(annotation, that.annotation) &&
				Objects.equals(annotationType, that.annotationType) &&
				Objects.equals(term, that.term) &&
				Objects.equals(replacedBy, that.replacedBy);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), annotation, annotationType, term, replacedBy);
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
 