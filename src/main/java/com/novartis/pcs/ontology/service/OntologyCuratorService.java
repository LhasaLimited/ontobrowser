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
import java.util.Set;

import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.CuratorAction;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.Synonym;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.VersionedEntity;

public interface OntologyCuratorService {
	
	Curator loadByUsername(String username);
	
	Collection<Term> loadPendingTerms();

	Collection<Synonym> loadPendingSynonyms();

	Collection<Relationship> loadPendingRelationships();
	
	Term approveTerm(long termId, String comments, String curatorUsername) throws InvalidEntityException;
	
	Term rejectTerm(long termId, String comments, String curatorUsername) throws InvalidEntityException;
	
	Synonym approveSynonym(long synonymId, String comments, String curatorUsername) throws InvalidEntityException;
	
	Synonym rejectSynonym(long synonymId, String comments, String curatorUsername) throws InvalidEntityException;
	
	Relationship approveRelationship(long relationshipId,
									 String comments, String curatorUsername) throws InvalidEntityException;
	
	Relationship rejectRelationship(long relationshipId,
									String comments, String curatorUsername) throws InvalidEntityException;
	
	<T extends VersionedEntity> Set<T> approve(Set<T> pending, String comments, String curatorUsername) throws InvalidEntityException;
	
	<T extends VersionedEntity> Set<T> reject(Set<T> pending, String comments, String curatorUsername) throws InvalidEntityException;
	
	Relationship obsoleteRelationship(long relationshipId, long replacementRelationshipId,
									  String comments, String curatorUsername) throws InvalidEntityException;
	
	Synonym obsoleteSynonym(long synonymId, long replacementSynonymId, String comments,
							String curatorUsername) throws InvalidEntityException;
	
	Term obsoleteTerm(long termId, long replacementTermId, String comments,
					  String curatorUsername) throws InvalidEntityException;
	
	Collection<CuratorAction> loadCuratorActions();
	
	void changePassword(String curator, String oldPassword, String newPassword) throws InvalidEntityException;

}
