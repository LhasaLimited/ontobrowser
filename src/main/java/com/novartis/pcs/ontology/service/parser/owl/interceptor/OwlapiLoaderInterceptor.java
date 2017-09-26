/**
 * Copyright © 2017 Lhasa Limited
 * File created: 19/09/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
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
			logger.info("Loading:" + importIRI.toString());
			if (ignored.containsKey(importIRI)) {
				logger.info("Ignoring:" + importIRI.toString());
				OWLOntologyImpl owlOntology = new OWLOntologyImpl((OWLOntologyManager) invocation.getThis(),
						new OWLOntologyID(Optional.of(importIRI), Optional.absent()));
				OWLOntologyFactory.OWLOntologyCreationHandler owlOntologyManager = (OWLOntologyFactory.OWLOntologyCreationHandler) invocation
						.getThis();
				owlOntologyManager.ontologyCreated(owlOntology);
				return owlOntology;
			}
			return invocation.proceed();
		}
		logger.info("Loading without any check");
		return invocation.proceed();
	}

	public void setIgnored(final Map<IRI, Ontology> ignored) {
		this.ignored = ignored;
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
