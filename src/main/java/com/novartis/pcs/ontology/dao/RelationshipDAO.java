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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.Query;

import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.Term;

/**
 * Stateless session bean DAO for Relationship entity
 */
@Stateless
@Local({RelationshipDAOLocal.class})
@Remote({RelationshipDAORemote.class})
public class RelationshipDAO extends VersionedEntityDAO<Relationship> 
	implements RelationshipDAOLocal, RelationshipDAORemote {
       
    public RelationshipDAO() {
        super();
    }
    
    @Override
	@SuppressWarnings("unchecked")
	public Collection<Relationship> loadByRelatedTermId(long relatedTermId) {
		Query query = entityManager.createNamedQuery(Relationship.QUERY_BY_RELATED_TERM_ID);
		query.setParameter("relatedTermId", relatedTermId);
		return query.getResultList();
	}
    
    @Override
	@SuppressWarnings("unchecked")
	public Collection<Relationship> loadByRelatedTermRefId(String termRefId) {
		Query query = entityManager.createNamedQuery(Relationship.QUERY_BY_RELATED_TERM_REF_ID);
		query.setParameter("termRefId", termRefId.toUpperCase());
		return query.getResultList();
	}
    
	@Override
	@SuppressWarnings("unchecked")
	public Collection<Relationship> loadHierarchy(long termId, final String ontologyId, final boolean deep) {
		if(isOracle()) {
			// Oracle database hierarchical query implementation.
			Query query = entityManager.createNamedQuery(Relationship.QUERY_HIERARCHY);
			// Setting cache hint causes exception due to hibernate bug (https://hibernate.atlassian.net/browse/HHH-9111)
	       	//query.setHint("org.hibernate.cacheable", Boolean.TRUE);
			query.setParameter("termId", termId);
			query.setParameter("ontology_name", ontologyId);
			query.setParameter("deep", deep ? 1 : 0);
			List<Object[]> rows = query.getResultList();
			return mapRelationships(rows);
		} else {
			// non-Oracle implementation which loads all relationships (from second level cache)
			List<Relationship> all = loadAll();
			Map<Term, List<Relationship>> parents = new HashMap<Term, List<Relationship>>(all.size());
			Map<Term, List<Relationship>> children = new HashMap<Term, List<Relationship>>(all.size());
			Term term = null;		
			for(Relationship relationship : all) {
				addToList(parents, relationship.getTerm(), relationship);
				addToList(children, relationship.getRelatedTerm(), relationship);
				
				if(term == null && relationship.getTerm().getId() == termId) {
					term = relationship.getTerm();
				}
				
				if(term == null && relationship.getRelatedTerm().getId() == termId) {
					term = relationship.getRelatedTerm();
				}
			}
			
			Set<Relationship> hierarchy = new LinkedHashSet<Relationship>();
			addToHierarchy(hierarchy, parents, term);
			
			List<Relationship> list = children.get(term);
			if(list == null) { // term is a leaf node
				list = parents.get(term);
				if(list != null) {
					for(Relationship relationship : list) {
						relationship.setLeaf(true);
					}
				}
			} else {
				for(Relationship relationship : list) {
					relationship.setLeaf(!children.containsKey(relationship.getTerm()));
					hierarchy.add(relationship);
				}
			}	
			
			return new ArrayList<Relationship>(hierarchy);
		}
	}

	@Override
	public Collection<Relationship> loadHierarchy(final String ontologyName, final boolean deep) {
		if (isOracle()) {
			Query query = entityManager.createNamedQuery(Relationship.QUERY_HIERARCHY_ONTOLOGY);
			// Setting cache hint causes exception due to hibernate bug (https://hibernate.atlassian.net/browse/HHH-9111)
			//query.setHint("org.hibernate.cacheable", Boolean.TRUE);
			query.setParameter("ontology_name", ontologyName);
			query.setParameter("deep", deep ? 1 : 0);
			return mapRelationships(query.getResultList());
		} else {
			throw new RuntimeException("Not yet implemented");
		}
	}

	private Collection<Relationship> mapRelationships(final List<Object[]> rows) {
		List<Relationship> relationships = new ArrayList<>(rows.size());
		long relIds[] = new long[rows.size()];
		for (int i = 0; i < rows.size(); i++) {
			relIds[i] = ((BigDecimal) rows.get(i)[0]).longValue();
		}
		List<Relationship> loaded = load(relIds);
		Map<Long, Relationship> relationshipMap = new HashMap<>();
		for (Relationship relationship : loaded) {
			relationshipMap.put(relationship.getId(), relationship);
		}

		for(Object[] row : rows) {
			Relationship relationship = relationshipMap.get(((BigDecimal) row[0]).longValue());
			Number leaf = (Number)row[1];
			relationship.setLeaf(leaf.intValue() != 0);
			relationships.add(relationship);
		}
		return relationships;
	}

	private void addToList(Map<Term, List<Relationship>> map, Term term, Relationship relationship) {
		List<Relationship> list = map.get(term);
		if(list == null) {
			list = new ArrayList<Relationship>();
			map.put(term, list);
		}
		list.add(relationship);
	}
	
	private void addToHierarchy(Set<Relationship> hierarchy, Map<Term, List<Relationship>> parents, Term term) {
		List<Relationship> list = parents.get(term);
		if(list != null) {
			for(Relationship relationship : list) {
				if(hierarchy.add(relationship)) {
					addToHierarchy(hierarchy, parents, relationship.getRelatedTerm());
				}
			}
		}
	}
}
