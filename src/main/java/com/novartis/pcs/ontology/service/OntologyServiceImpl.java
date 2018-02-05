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

package com.novartis.pcs.ontology.service;

import java.text.MessageFormat;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;

import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.entity.VersionedEntity;

/**
 * @author Artur Polit
 * @since 18/08/2017
 */
@Stateless
@Local(OntologyServiceLocal.class)
public class OntologyServiceImpl extends OntologyService implements OntologyServiceLocal {

	@Override
	public void createOntology(final Ontology ontology, final String username) throws InvalidEntityException {

		Curator curator = curatorDAO.loadByUsername(username);
		if (curator == null) {
			return;
		}

		if (ontologyDAO.loadByName(ontology.getName()) != null) {
			throw new InvalidEntityException(ontology, MessageFormat.format("Ontology {0} already exist", ontology.getName()));
		}

		Version version = lastUnpublishedVersion(curator);
		ontology.setCreatedDate();
		ontology.setCreatedBy(curator);
		ontology.setCreatedVersion(version);
		ontology.setStatus(VersionedEntity.Status.APPROVED);
		ontology.setApprovedVersion(version);
		ontologyDAO.save(ontology);
	}

	@Override
	public List<Ontology> loadAll() {
		return ontologyDAO.loadAll();
	}

	@Override
	public List<Ontology> loadRecursive() {
		return ontologyDAO.loadRecursive();
	}
}

 