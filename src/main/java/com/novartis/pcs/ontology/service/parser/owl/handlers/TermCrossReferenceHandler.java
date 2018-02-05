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

import org.obolibrary.obo2owl.Obo2OWLConstants;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLLiteral;

import com.novartis.pcs.ontology.entity.CrossReference;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.service.parser.owl.ApiHelper;
import com.novartis.pcs.ontology.service.parser.owl.OWLParserContext;
import com.novartis.pcs.ontology.service.parser.owl.OWLVisitorHandler;
import com.novartis.pcs.ontology.service.parser.owl.ParserState;

/**
 * @author Artur Polit
 * @since 09/08/2017
 */
public class TermCrossReferenceHandler implements OWLVisitorHandler {

	private static final int ACRONYM_INDEX = 0;
	private static final int REFID_INDEX = 1;

	@Override
	public boolean match(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		return ParserState.TERM.equals(context.statePeek()) && Obo2OWLConstants.Obo2OWLVocabulary.IRI_OIO_hasDbXref.getIRI()
				.equals(owlAnnotation.getProperty().getIRI());
	}

	@Override
	public void handleAnnotation(final OWLParserContext context, final OWLAnnotation owlAnnotation) {
		Term term = context.termPeek();
		if (owlAnnotation.getValue() instanceof OWLLiteral) {
			String string = ApiHelper.getString(owlAnnotation);
			if (string != null && string.contains(":")) {
				String[] splitted = string.split(":");
				Datasource datasource = context.getDatasource(splitted[ACRONYM_INDEX]);
				new CrossReference(term, datasource, splitted[REFID_INDEX], context.getCurator());
			} else if (string != null) {
				// TS28 from bioontology - without prefix
			}
		} else if (owlAnnotation.getValue() instanceof IRI) {
			new CrossReference(term, context.getIri().toString(), context.getCurator());
		}
	}
}

 