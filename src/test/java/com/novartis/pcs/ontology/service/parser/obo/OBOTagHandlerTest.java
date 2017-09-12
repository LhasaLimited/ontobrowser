package com.novartis.pcs.ontology.service.parser.obo;

import static org.hamcrest.CoreMatchers.is;

import org.apache.commons.lang.mutable.MutableInt;
import org.coode.owlapi.obo12.parser.OBOVocabulary;
import org.junit.Assert;
import org.junit.Test;

/**
 * Copyright © 2017 Lhasa Limited File created: 08/09/2017 by Artur Polit
 * Creator : Artur Polit Version : $$Id$$
 */
public class OBOTagHandlerTest {

	@Test
	public void shouldAcceptSpaceAtTheBeginning() {
		OBOTagHandler oboTagHandler = new OBOTagHandler(OBOVocabulary.SYNONYM, null) {
			@Override
			void handleTagValue(final String tag, final String value, final String qualifierBlock,
					final String comment) {

			}
		};
		MutableInt fromIndex = new MutableInt();
		String substr = oboTagHandler.substr(" \" Long lower jaw\" RELATED [:devtox.org]", '"', '"', fromIndex);
		Assert.assertThat(substr, is(" Long lower jaw"));
	}
}