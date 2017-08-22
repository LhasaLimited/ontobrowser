/**
 * Copyright © 2017 Lhasa Limited
 * File created: 09/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.parser.owl.handlers;

import com.novartis.pcs.ontology.entity.Annotation;
import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.service.parser.owl.ApiHelper;
import com.novartis.pcs.ontology.service.parser.owl.OWLParserContext;
import com.novartis.pcs.ontology.service.parser.owl.OWLVisitorHandler;
import com.novartis.pcs.ontology.service.parser.owl.ParserState;
import org.semanticweb.owlapi.model.OWLAnnotation;

/**
 * @author Artur Polit
 * @since 09/08/2017
 */
public class TermAnnotationHandler implements OWLVisitorHandler {
	@Override
	public boolean match(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		return  ParserState.TERM.equals(context.statePeek());
	}

	@Override
	public void handleAnnotation(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		Term term = context.termPeek();
		AnnotationType annotationType = context.getAnnotationType(owlAnnotation.getProperty().getIRI().getShortForm());
		String value = ApiHelper.getString(owlAnnotation);
		Annotation annotation = new Annotation(value, annotationType, term, context.getCurator(), context.getVersion());
		annotation.setOntology(context.getOntology());
		context.approve(annotation);
		term.getAnnotations().add(annotation);
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
 