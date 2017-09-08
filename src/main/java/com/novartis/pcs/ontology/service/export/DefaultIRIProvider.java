/**
 * Copyright © 2017 Lhasa Limited
 * File created: 16/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.export;

import static com.novartis.pcs.ontology.service.export.OntologyExportUtil.createIRI;

import java.net.URISyntaxException;
import java.net.URL;

import com.novartis.pcs.ontology.entity.AnnotationType;
import org.semanticweb.owlapi.model.IRI;

import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.Term;

/**
 * @author Artur Polit
 * @since 16/08/2017
 */
class DefaultIRIProvider implements IRIProvider {

	private final Ontology ontology;
	private final URL baseURL;

	DefaultIRIProvider(Ontology ontology, URL baseURL) {

		this.ontology = ontology;
		this.baseURL = baseURL;
	}

	@Override
	public IRI getIRI(final Term term) throws URISyntaxException {
		return createIRI(baseURL.toURI(), ontology.getName(), term.getReferenceId());
	}

	@Override
	public IRI getIRI(final AnnotationType annotationType) throws URISyntaxException {
		return createIRI(baseURL.toURI(), ontology.getName(), annotationType.getPrefixedXmlType());
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
