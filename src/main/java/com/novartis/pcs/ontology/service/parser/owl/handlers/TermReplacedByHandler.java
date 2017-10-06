/**
 * Copyright © 2017 Lhasa Limited
 * File created: 09/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.parser.owl.handlers;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.novartis.pcs.ontology.entity.Annotation;
import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.service.export.ReferenceIdProvider;
import com.novartis.pcs.ontology.service.parser.owl.ApiHelper;
import com.novartis.pcs.ontology.service.parser.owl.OWLParserContext;
import com.novartis.pcs.ontology.service.parser.owl.OWLVisitorHandler;
import org.obolibrary.obo2owl.Obo2OWLConstants;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLLiteral;

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
		Term term = context.termPeek();
		Optional<OWLLiteral> owlLiteralOptional = owlAnnotation.getValue().asLiteral();
		Optional<IRI> iriOptional = owlAnnotation.getValue().asIRI();
		String refId = null;
		if (iriOptional.isPresent()) {
			refId = ReferenceIdProvider.getRefId(iriOptional.get());
		} else if (owlLiteralOptional.isPresent()) {
			OWLLiteral owlLiteral = owlLiteralOptional.get();
			if (owlLiteral.isRDFPlainLiteral()) {
				refId = owlLiteral.getLiteral();
			}
		}
		if (!Strings.isNullOrEmpty(refId)) {
			if (context.hasTerm(refId)) {
				term.setReplacedBy(context.getTerm(refId));
			} else {
				AnnotationType annotationType = context.getAnnotationType(ReferenceIdProvider.getRefId(owlAnnotation.getProperty()));
				new Annotation(refId, annotationType, term, context.getCurator(), context.getVersion());
			}
		} else {
			logger.log(INFO, "Replacement not found [term={0}, replacement={1}]", new String[] { term.getReferenceId(), refId });
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
 