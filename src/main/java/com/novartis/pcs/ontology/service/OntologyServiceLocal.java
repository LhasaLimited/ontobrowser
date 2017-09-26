/**
 * Copyright © 2017 Lhasa Limited
 * File created: 18/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service;

import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;

import javax.ejb.Local;
import java.util.List;

/**
 * @author Artur Polit
 * @since 18/08/2017
 */
@Local
public interface OntologyServiceLocal {

	void createOntology(Ontology ontology, final String username) throws InvalidEntityException;

	List<Ontology> loadAll();

	List<Ontology> loadRecursive();
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
 