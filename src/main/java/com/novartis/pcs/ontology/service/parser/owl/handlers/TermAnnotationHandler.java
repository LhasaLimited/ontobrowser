/**
 * Copyright © 2017 Lhasa Limited
 * File created: 09/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.parser.owl.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.novartis.pcs.ontology.service.export.ReferenceIdProvider;
import org.semanticweb.owlapi.model.OWLAnnotation;

import com.google.common.base.Strings;
import com.novartis.pcs.ontology.entity.Annotation;
import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.service.parser.owl.ApiHelper;
import com.novartis.pcs.ontology.service.parser.owl.OWLParserContext;
import com.novartis.pcs.ontology.service.parser.owl.OWLVisitorHandler;
import com.novartis.pcs.ontology.service.parser.owl.ParserState;

/**
 * @author Artur Polit
 * @since 09/08/2017
 */
public class TermAnnotationHandler implements OWLVisitorHandler {
	private final Logger logger = Logger.getLogger(getClass().getName());

	@Override
	public boolean match(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		return  ParserState.TERM.equals(context.statePeek());
	}

	@Override
	public void handleAnnotation(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		Term term = context.termPeek();
		AnnotationType annotationType = context.getAnnotationType(ReferenceIdProvider.getRefId(owlAnnotation.getProperty()));
		String value = ApiHelper.getString(owlAnnotation);
		if (!Strings.isNullOrEmpty(value)) {
			Annotation annotation = new Annotation(value, annotationType, term, context.getCurator(),
					context.getVersion());
			annotation.setOntology(context.getOntology());
			context.approve(annotation);
			term.getAnnotations().add(annotation);
		} else {
			logger.log(Level.WARNING, "Annotation omitted, value is null [termReferenceId={0}, annotationType={1}]",
					new String[] { term.getReferenceId(), annotationType.getAnnotationType() });
		}
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
 