/**
 * Copyright © 2017 Lhasa Limited
 * File created: 09/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.parser.owl;

import org.semanticweb.owlapi.model.OWLAnnotation;

/**
 * @author Artur Polit
 * @since 09/08/2017
 */
public interface OWLVisitorHandler {
	boolean match(final OWLParserContext context, OWLAnnotation owlAnnotation);

	void handleAnnotation(final OWLParserContext context, OWLAnnotation owlAnnotation);
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
 