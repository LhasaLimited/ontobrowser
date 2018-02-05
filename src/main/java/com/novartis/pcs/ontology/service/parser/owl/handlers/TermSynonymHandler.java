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

import java.util.Map;
import java.util.logging.Logger;

import org.obolibrary.obo2owl.Obo2OWLConstants;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.novartis.pcs.ontology.entity.Synonym;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.service.parser.owl.ApiHelper;
import com.novartis.pcs.ontology.service.parser.owl.OWLParserContext;
import com.novartis.pcs.ontology.service.parser.owl.OWLVisitorHandler;
import com.novartis.pcs.ontology.service.parser.owl.ParserState;

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

 