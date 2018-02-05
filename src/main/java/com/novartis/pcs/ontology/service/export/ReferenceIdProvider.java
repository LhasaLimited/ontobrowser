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

