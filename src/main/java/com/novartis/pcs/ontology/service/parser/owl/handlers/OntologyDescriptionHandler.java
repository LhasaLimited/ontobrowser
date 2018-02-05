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

package com.novartis.pcs.ontology.service.parser.owl.handlers;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.OWLAnnotation;

import com.novartis.pcs.ontology.service.parser.owl.ApiHelper;
import com.novartis.pcs.ontology.service.parser.owl.OWLParserContext;
import com.novartis.pcs.ontology.service.parser.owl.OWLVisitorHandler;
import com.novartis.pcs.ontology.service.parser.owl.ParserState;

/**
 * @author Artur Polit
 * @since 09/08/2017
 */
public class OntologyDescriptionHandler implements OWLVisitorHandler {
	@Override
	public boolean match(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		return ParserState.ONTOLOGY.equals(context.statePeek()) && owlAnnotation.getProperty().isComment();
	}

	@Override
	public void handleAnnotation(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		String description = appendNonEmpty(context.getOntology().getDescription(),	ApiHelper.getString(owlAnnotation));
		context.getOntology().setDescription(StringUtils.abbreviate(description, 1024));
	}

	private String appendNonEmpty(String existing, String appended) {
		return existing != null && !existing.isEmpty() ? existing + " " + appended : appended;
	}
}

 