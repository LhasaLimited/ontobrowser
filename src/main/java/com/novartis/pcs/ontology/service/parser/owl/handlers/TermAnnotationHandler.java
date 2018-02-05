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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.OWLAnnotation;

import com.google.common.base.Strings;
import com.novartis.pcs.ontology.entity.Annotation;
import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.service.export.ReferenceIdProvider;
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

 