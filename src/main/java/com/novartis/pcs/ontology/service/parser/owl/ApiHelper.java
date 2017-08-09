/**
 * Copyright © 2017 Lhasa Limited
 * File created: 09/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.parser.owl;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * @author Artur Polit
 * @since 09/08/2017
 */
public class ApiHelper {

	private ApiHelper() {
	}

	public static final String getString(OWLAnnotation owlAnnotation) {
		String result = null;
		if (owlAnnotation.getValue() instanceof OWLLiteral) {
			OWLLiteral owlLiteral = (OWLLiteral) owlAnnotation.getValue();
			result = owlLiteral.getLiteral();
		} else if (owlAnnotation.getValue() instanceof IRI) {
			result = ((IRI) owlAnnotation.getValue()).getRemainder().orNull();
		}
		return result;
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
 