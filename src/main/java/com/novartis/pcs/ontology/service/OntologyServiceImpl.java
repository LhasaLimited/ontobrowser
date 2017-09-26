/**
 * Copyright © 2017 Lhasa Limited
 * File created: 18/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service;

import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.entity.VersionedEntity;

import javax.ejb.Local;
import javax.ejb.Stateless;
import java.text.MessageFormat;
import java.util.List;

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
 