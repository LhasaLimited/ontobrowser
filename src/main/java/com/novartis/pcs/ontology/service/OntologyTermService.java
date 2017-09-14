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

import java.util.Collection;

import com.novartis.pcs.ontology.entity.ControlledVocabularyTerm;
import com.novartis.pcs.ontology.entity.DuplicateEntityException;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Synonym;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.webapp.client.ChildTermDto;

public interface OntologyTermService {
	
	Collection<Term> loadAll(String ontologyName);
	
	Collection<Term> loadLastCreated(int max);
	
	Term loadByReferenceId(String referenceId, final String ontologyName);
	
	Collection<RelationshipType> loadAllRelationshipTypes();
	
	Term createTerm(String ontologyName, String term,
					String definition, String url, String comments,
					String relatedTermRefId, String relationshipType,
					String curatorUsername) throws DuplicateEntityException, InvalidEntityException;
	
	Term createTerm(final ChildTermDto childTermDto, String curatorUsername)
			throws DuplicateEntityException, InvalidEntityException;
	
	Term addSynonym(String termRefId, String synonym, Synonym.Type type,
					String datasourceAcronym, String referenceId, String curatorUsername)
			throws DuplicateEntityException, InvalidEntityException;
	
	Term addSynonym(String termRefId,
					ControlledVocabularyTerm vocabTerm,
					Synonym.Type synonymType,
					String curatorUsername)
			throws DuplicateEntityException, InvalidEntityException;
	
	Term addSynonyms(String termRefId,
					 Collection<ControlledVocabularyTerm> terms,
					 Synonym.Type synonymType,
					 String curatorUsername)
			throws DuplicateEntityException, InvalidEntityException;
	
	Term addRelationship(String termRefId, String relatedTermRefId,
						 String relationshipType, String curatorUsername, final String ontologyName)
			throws DuplicateEntityException, InvalidEntityException;
	
	Term updateTerm(long termId, String definition, String url,
					String comments, String curatorUsername) throws InvalidEntityException;
	
	Synonym updateSynonym(long synonymId, Synonym.Type type,
						  String curatorUsername) throws InvalidEntityException;
	
	Relationship updateRelationship(long relationshipId,
									String relationship, String curatorUsername)
			throws InvalidEntityException, DuplicateEntityException;
	
	void deleteTerm(long termId, String curatorUsername) throws InvalidEntityException;
	
	void deleteSynonym(long synonymId, String curatorUsername) throws InvalidEntityException;
	
	void deleteRelationship(long relationshipId, String curatorUsername)
			throws InvalidEntityException;

	Term loadByOntology(String ontologyName);

	Collection<Relationship> getRelationships(Term term, String ontologyName, final boolean deep);
}
