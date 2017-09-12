/**
 * Copyright © 2017 Lhasa Limited
 * File created: 23/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.parser.owl.handlers;

import org.semanticweb.owlapi.model.OWLAnnotation;

import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.VersionedEntity;
import com.novartis.pcs.ontology.service.parser.owl.OWLParserContext;
import com.novartis.pcs.ontology.service.parser.owl.OWLVisitorHandler;

/**
 * @author Artur Polit
 * @since 23/08/2017
 */
public class TermDeprecatedHandler implements OWLVisitorHandler {

	@Override
	public boolean match(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		return owlAnnotation.isDeprecatedIRIAnnotation();
	}

	@Override
	public void handleAnnotation(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		Term term = context.termPeek();
		term.setObsoleteVersion(context.getVersion());
		term.setStatus(VersionedEntity.Status.OBSOLETE);
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
 