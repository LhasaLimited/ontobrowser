/*

Copyright 2017 Lhasa Limited

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

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

 