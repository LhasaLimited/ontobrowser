/**
 * Copyright © 2017 Lhasa Limited
 * File created: 09/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.parser.owl;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * @author Artur Polit
 * @since 09/08/2017
 */
public final class ApiHelper {

	private ApiHelper() {
	}

	public static String getString(OWLAnnotation owlAnnotation) {
		String result = null;
		if (owlAnnotation.getValue() instanceof OWLLiteral) {
			OWLLiteral owlLiteral = (OWLLiteral) owlAnnotation.getValue();
			result = owlLiteral.getLiteral();
		} else if (owlAnnotation.getValue() instanceof IRI) {
			result = owlAnnotation.getValue().toString();
		}
		return result;
	}

	public static String getString(OWLDataPropertyAssertionAxiom owlAnnotation) {
		OWLLiteral owlLiteral = owlAnnotation.getObject();
		return owlLiteral.getLiteral();
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
 