/**
 * Copyright © 2017 Lhasa Limited
 * File created: 11/07/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.dao;

import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Ontology;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import java.util.Collection;

/**
 * @author Artur Polit
 * @since 11/07/2017
 */
@Stateless
@Local(AnnotationTypeDAOLocal.class)
public class AnnotationTypeDAO extends VersionedEntityDAO<AnnotationType> implements ReadOnlyDAO<AnnotationType>, AnnotationTypeDAOLocal {

	public AnnotationTypeDAO() {
		super();
	}

	@Override
	public Collection<AnnotationType> loadByOntology(final Ontology ontology) {
		TypedQuery<AnnotationType> namedQuery = entityManager.createNamedQuery(AnnotationType.QUERY_BY_ONTOLOGY, AnnotationType.class);
		namedQuery.setParameter("ontology", ontology);
		return namedQuery.getResultList();
	}

	@Override
	public AnnotationType loadByAnnotation(final String annotation) {
		TypedQuery<AnnotationType> namedQuery = entityManager.createNamedQuery(AnnotationType.QUERY_BY_ANNOTATION,
				AnnotationType.class);
		namedQuery.setParameter("annotation", annotation);
		return namedQuery.getResultList().isEmpty() ? null : namedQuery.getResultList().get(0);
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
