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
package com.novartis.pcs.ontology.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;

import com.novartis.pcs.ontology.dao.CuratorActionDAOLocal;
import com.novartis.pcs.ontology.dao.CuratorDAOLocal;
import com.novartis.pcs.ontology.dao.DatasourceDAOLocal;
import com.novartis.pcs.ontology.dao.OntologyDAOLocal;
import com.novartis.pcs.ontology.dao.RelationshipDAOLocal;
import com.novartis.pcs.ontology.dao.RelationshipTypeDAOLocal;
import com.novartis.pcs.ontology.dao.SynonymDAOLocal;
import com.novartis.pcs.ontology.dao.TermDAOLocal;
import com.novartis.pcs.ontology.dao.VersionDAOLocal;
import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.Synonym;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.entity.VersionedEntity;

public class OntologyService {
	@EJB 
	protected VersionDAOLocal versionDAO;
	
	@EJB
	protected CuratorDAOLocal curatorDAO;
	
	@EJB
	protected CuratorActionDAOLocal curatorActionDAO;
	
	@EJB
	protected DatasourceDAOLocal datasourceDAO;

	@EJB
	protected OntologyDAOLocal ontologyDAO;
	
	@EJB
	protected TermDAOLocal termDAO;
				
	@EJB
	protected SynonymDAOLocal synonymDAO;
			
	@EJB
	protected RelationshipDAOLocal relationshipDAO;
	
	@EJB
	protected RelationshipTypeDAOLocal relationshipTypeDAO;

	public OntologyService() {
		super();
	}

	protected Version lastUnpublishedVersion(Curator curator) throws InvalidEntityException {
		Version version = null;
		Collection<Version> versions = versionDAO.loadAll();
		if(!versions.isEmpty()) {
			if(!(versions instanceof List<?>)) {
				versions = new ArrayList<Version>(versions);
			}
			Collections.sort((List<Version>)versions);
			Version last = ((List<Version>)versions).get(versions.size()-1);
			if(last.getPublishedBy() == null && last.getPublishedDate() == null) {
				version = last;
			}
		}
		
		if(version == null) {
			version = new Version(curator);
			versionDAO.save(version);
		} 
		 
		return version;
	}

	@SuppressWarnings("incomplete-switch")
	protected void removePendingDependents(Term term, Version version)
			throws InvalidEntityException {
		Collection<Synonym> synonyms = new ArrayList<Synonym>(term.getSynonyms());
		for(Synonym synonym : synonyms) {
			switch(synonym.getStatus()) {
			case PENDING:
				term.getSynonyms().remove(synonym);
				synonymDAO.delete(synonym);
				break;
			case APPROVED:
				synonym.setStatus(VersionedEntity.Status.OBSOLETE);
				synonym.setObsoleteVersion(version);
				break;
			}
		}

		Collection<Relationship> relationships = new ArrayList<Relationship>(term.getRelationships());
		for(Relationship relationship : relationships) {
			switch(relationship.getStatus()) {
			case PENDING:
				term.getRelationships().remove(relationship);
				relationshipDAO.delete(relationship);
				break;
			case APPROVED:
				relationship.setStatus(VersionedEntity.Status.OBSOLETE);
				relationship.setObsoleteVersion(version);
				break;
			}
		}

		Collection<Relationship> descendents = relationshipDAO.loadByRelatedTermId(term.getId());
		for (Relationship relationship : descendents) {
			switch(relationship.getStatus()) {
			case PENDING:
				Term childTerm = relationship.getTerm();
				childTerm.getRelationships().remove(relationship);
				relationshipDAO.delete(relationship);
				break;
			case APPROVED:
				relationship.setStatus(VersionedEntity.Status.OBSOLETE);
				relationship.setObsoleteVersion(version);
				break;
			}
		}
	}
}