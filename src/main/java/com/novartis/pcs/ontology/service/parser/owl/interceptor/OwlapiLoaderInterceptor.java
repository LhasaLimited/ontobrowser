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

package com.novartis.pcs.ontology.service.parser.owl.interceptor;

import java.util.Map;
import java.util.logging.Logger;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyFactory;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Optional;
import com.novartis.pcs.ontology.entity.Ontology;

import uk.ac.manchester.cs.owl.owlapi.OWLOntologyImpl;

/**
 * @author Artur Polit
 * @since 19/09/2017
 */
public class OwlapiLoaderInterceptor implements MethodInterceptor {

	private Logger logger = Logger.getLogger(getClass().getName());
	private Map<IRI, Ontology> ignored;

	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		IRI importIRI = (IRI) invocation.getArguments()[0];
		if (invocation.getArguments()[1] instanceof IRIDocumentSource) {
			if (ignored.containsKey(importIRI)) {
				logger.info("Ignoring:" + importIRI.toString());
				OWLOntologyImpl owlOntology = new OWLOntologyImpl((OWLOntologyManager) invocation.getThis(),
						new OWLOntologyID(Optional.of(importIRI), Optional.absent()));
				OWLOntologyFactory.OWLOntologyCreationHandler owlOntologyManager = (OWLOntologyFactory.OWLOntologyCreationHandler) invocation
						.getThis();
				owlOntologyManager.ontologyCreated(owlOntology);
				return owlOntology;
			}
			logger.info("Loading:" + importIRI.toString());
			return invocation.proceed();
		}
		logger.info("Loading without any check");
		return invocation.proceed();
	}

	public void setIgnored(final Map<IRI, Ontology> ignored) {
		this.ignored = ignored;
	}
}

