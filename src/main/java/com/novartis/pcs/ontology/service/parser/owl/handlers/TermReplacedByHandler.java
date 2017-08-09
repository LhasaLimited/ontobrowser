/**
 * Copyright © 2017 Lhasa Limited
 * File created: 09/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.parser.owl.handlers;

import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.service.parser.owl.OWLParserContext;
import com.novartis.pcs.ontology.service.parser.owl.OWLVisitorHandler;
import org.obolibrary.obo2owl.Obo2OWLConstants;
import org.semanticweb.owlapi.model.OWLAnnotation;

import java.util.logging.Logger;

import static java.util.logging.Level.INFO;

/**
 * @author Artur Polit
 * @since 09/08/2017
 */
public class TermReplacedByHandler implements OWLVisitorHandler {
	private final Logger logger = Logger.getLogger(getClass().getName());

	@Override
	public boolean match(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		return Obo2OWLConstants.Obo2OWLVocabulary.IRI_IAO_0100001.getIRI()
				.equals(owlAnnotation.getProperty().getIRI());
	}

	@Override
	public void handleAnnotation(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		logger.log(INFO, "replaced_by unused in BAO");
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
 