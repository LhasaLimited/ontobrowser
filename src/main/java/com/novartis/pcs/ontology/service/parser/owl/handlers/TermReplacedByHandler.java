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

import static java.util.logging.Level.INFO;

import java.util.logging.Logger;

import org.obolibrary.obo2owl.Obo2OWLConstants;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLLiteral;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.novartis.pcs.ontology.entity.Annotation;
import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.service.export.ReferenceIdProvider;
import com.novartis.pcs.ontology.service.parser.owl.OWLParserContext;
import com.novartis.pcs.ontology.service.parser.owl.OWLVisitorHandler;

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

 