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

import static com.novartis.pcs.ontology.service.export.OntologyExportUtil.createIRI;

import java.net.URISyntaxException;
import java.net.URL;

import org.semanticweb.owlapi.model.IRI;

import com.novartis.pcs.ontology.entity.AnnotationType;
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

