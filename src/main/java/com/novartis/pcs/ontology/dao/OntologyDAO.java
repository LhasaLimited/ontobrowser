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
package com.novartis.pcs.ontology.dao;

import java.util.Collection;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.annotations.QueryHints;

import com.novartis.pcs.ontology.entity.Ontology;

/**
 * Stateless session bean DAO for Ontology entity
 */
@Stateless
@Local({OntologyDAOLocal.class})
@Remote({OntologyDAORemote.class})
public class OntologyDAO extends VersionedEntityDAO<Ontology>
	implements OntologyDAOLocal, OntologyDAORemote {

    public OntologyDAO() {
        super();
    }

	@Override
	public Ontology loadByName(String ontologyName) {
		return loadByName(ontologyName, false);
	}

	@Override
	public Ontology loadByName(String ontologyName, boolean lock) {
		try {
			Query query = entityManager.createNamedQuery(Ontology.QUERY_BY_NAME);
			query.setParameter("name", ontologyName);

			if(lock) {
				query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
			}

			return (Ontology)query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Ontology loadByAlias(final String alias) {
		try {
			TypedQuery<Ontology> query = entityManager.createNamedQuery(Ontology.QUERY_BY_ALIAS, Ontology.class);
			query.setParameter("alias", alias);
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Collection<Ontology> loadNonIntermediateWithAliases(final Ontology ontology) {
		TypedQuery<Ontology> query = entityManager.createNamedQuery(Ontology.QUERY_NON_INTERMEDIATE, Ontology.class);
		query.setParameter("current", ontology);
		return query.getResultList();
	}

	@Override
	public List<Ontology> loadRecursive() {
		TypedQuery<Ontology> query = entityManager.createNamedQuery(Ontology.LOAD_ALL_NON_INTERMEDIATE, Ontology.class);
		query.setHint(QueryHints.FETCHGRAPH,entityManager.createEntityGraph(Ontology.GRAPH_ONTOLOGY_ALL));
		List<Ontology> resultList = query.getResultList();
		initRecursive(resultList);
		return resultList;
	}

	@Override
	public List<Ontology> loadClosure(final String name){
		TypedQuery<Ontology> query = entityManager.createNamedQuery(Ontology.QUERY_BY_NAME, Ontology.class);
		query.setParameter("name", name);
		return query.getResultList();
	}

	private void initRecursive(final Collection<Ontology> resultList) {
		for (Ontology ontology : resultList) {
			initRecursive(ontology.getImportedOntologies());
		}
	}

}
