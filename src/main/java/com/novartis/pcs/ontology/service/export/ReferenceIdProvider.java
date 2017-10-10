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
		return getRefId(owlNamedObject.getIRI());
	}

	public static String getRefId(final IRI iri) {
		String refId = null;
		if (iri.getScheme().startsWith("http")) {
			if (iri.getRemainder().isPresent()) {
				refId = iri.getRemainder().get();
			} else {
				String iriString = iri.toString();
				if (iriString.endsWith("/")) {
					iriString = iriString.substring(0, iriString.lastIndexOf('/'));
				}
				refId = iriString.substring(iriString.lastIndexOf('/') + 1);
			}

		} else if (iri.getScheme().equalsIgnoreCase("mailto")) {
			refId = iri.toString();
		}

		if (refId == null) {
			throw new IllegalArgumentException("Cannot find reference id:" + iri.toString());
		}
		return refId;
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
