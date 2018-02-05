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
package com.novartis.pcs.ontology.entity;

import java.util.Objects;

import javax.persistence.AssociationOverride;
import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
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
/**
 * @author Artur
 *
 */
@Entity
@Table(name = "ANNOTATION")
@AttributeOverride(name = "id",
		column = @Column(name = "ANNOTATION_ID", unique = true, nullable = false))
@AssociationOverride(name = "curatorActions", joinColumns = @JoinColumn(name = "ANNOTATION_ID"))
public class Annotation extends VersionedEntity implements ReplaceableEntity<Annotation> {

	public Annotation(final String annotation, final AnnotationType annotationType, final Term term, final Curator creator, final Version version) {
		super(creator, version);
		this.annotation = annotation;
		this.annotationType = annotationType;
		this.term = term;
		term.getAnnotations().add(this);
	}

	protected Annotation() {
	}

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

	@Valid
	@ManyToOne(cascade={CascadeType.PERSIST})
	@JoinColumn(name = "REPLACED_BY")
	private Annotation replacedBy;

	@ManyToOne
	@JoinColumn(name = "ONTOLOGY_ID")
	private Ontology ontology;

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

	public Ontology getOntology() {
		return ontology;
	}

	public void setOntology(final Ontology ontology) {
		this.ontology = ontology;
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

	@Override
	public String toString() {
		return "Annotation [annotation=" + annotation + ", annotationType=" + annotationType + "]";
	}
	
	
}

 