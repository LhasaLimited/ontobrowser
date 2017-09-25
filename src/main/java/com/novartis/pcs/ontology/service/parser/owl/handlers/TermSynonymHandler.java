/**
 * Copyright © 2017 Lhasa Limited
 * File created: 09/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.parser.owl.handlers;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.novartis.pcs.ontology.entity.Synonym;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.service.parser.owl.ApiHelper;
import com.novartis.pcs.ontology.service.parser.owl.OWLParserContext;
import com.novartis.pcs.ontology.service.parser.owl.OWLVisitorHandler;
import com.novartis.pcs.ontology.service.parser.owl.ParserState;
import org.obolibrary.obo2owl.Obo2OWLConstants;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLLiteral;

import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Artur Polit
 * @since 09/08/2017
 */
public class TermSynonymHandler implements OWLVisitorHandler{

	private final Logger logger = Logger.getLogger(getClass().getName());

	private final Map<IRI, Synonym.Type> synonymMap = ImmutableMap.of(
			Obo2OWLConstants.Obo2OWLVocabulary.IRI_OIO_hasBroadSynonym.getIRI(), Synonym.Type.BROAD,
			Obo2OWLConstants.Obo2OWLVocabulary.IRI_OIO_hasExactSynonym.getIRI(), Synonym.Type.EXACT,
			Obo2OWLConstants.Obo2OWLVocabulary.IRI_OIO_hasRelatedSynonym.getIRI(), Synonym.Type.RELATED,
			Obo2OWLConstants.Obo2OWLVocabulary.IRI_OIO_hasNarrowSynonym.getIRI(), Synonym.Type.NARROW);

	@Override
	public boolean match(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		return ParserState.TERM.equals(context.statePeek()) && synonymMap.containsKey(owlAnnotation.getProperty().getIRI());
	}

	@Override
	public void handleAnnotation(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		Term term = context.termPeek();
		Synonym.Type type = synonymMap.get(owlAnnotation.getProperty().getIRI());
		String value = ApiHelper.getString(owlAnnotation);
		if (!Strings.isNullOrEmpty(value)) {
			Synonym synonym = new Synonym(term, value, type, context.getCurator(), context.getVersion());
			context.approve(synonym);
		} else  {
			logger.warning("Synonym value is empty:" + owlAnnotation.toString());
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
 