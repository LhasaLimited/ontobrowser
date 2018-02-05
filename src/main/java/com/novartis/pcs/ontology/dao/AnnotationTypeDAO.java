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
package com.novartis.pcs.ontology.dao;

import java.util.Collection;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;

import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Ontology;

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

