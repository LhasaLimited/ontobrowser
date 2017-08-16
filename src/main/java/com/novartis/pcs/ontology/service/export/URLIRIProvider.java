/**
 * Copyright © 2017 Lhasa Limited
 * File created: 16/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.export;

import java.net.URISyntaxException;

import org.semanticweb.owlapi.model.IRI;

import com.novartis.pcs.ontology.entity.Term;

/**
 * @author Artur Polit
 * @since 16/08/2017
 */
class URLIRIProvider implements IRIProvider {

	@Override
	public IRI getIRI(final Term term) throws URISyntaxException {
		return IRI.create(term.getUrl());
	}
}
/*
 * ---------------------------------------------------------------------* This
 * software is the confidential and proprietary information of Lhasa Limited
 * Granary Wharf House, 2 Canal Wharf, Leeds, LS11 5PY --- No part of this
 * confidential information shall be disclosed and it shall be used only in
 * accordance with the terms of a written license agreement entered into by
 * holder of the information with LHASA Ltd.
 * ---------------------------------------------------------------------
 */
