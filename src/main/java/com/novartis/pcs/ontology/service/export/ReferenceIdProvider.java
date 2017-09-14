/**
 * Copyright © 2017 Lhasa Limited
 * File created: 11/09/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.export;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedObject;

/**
 * @author Artur Polit
 * @since 11/09/2017
 */
public final class ReferenceIdProvider {

	private ReferenceIdProvider() {
	}

	public static String getRefId(OWLNamedObject owlNamedObject) {
		IRI iri = owlNamedObject.getIRI();
		if (iri.getScheme().startsWith("http")) {
			if (iri.getRemainder().isPresent()) {
				return iri.getRemainder().get();
			} else {
				String iriString = iri.toString();
				if (iriString.endsWith("/")) {
					iriString = iriString.substring(0, iriString.lastIndexOf('/'));
				}
				return iriString.substring(iriString.lastIndexOf('/') + 1);
			}

		} else if (iri.getScheme().equalsIgnoreCase("mailto")) {
			return iri.toString();
		}
		throw new IllegalArgumentException("Cannot find reference id:" + iri.toString());
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
