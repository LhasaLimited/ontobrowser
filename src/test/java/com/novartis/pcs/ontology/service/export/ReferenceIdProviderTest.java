package com.novartis.pcs.ontology.service.export;

import static org.hamcrest.CoreMatchers.is;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

/**
 * Copyright © 2017 Lhasa Limited File created: 11/09/2017 by Artur Polit
 * Creator : Artur Polit Version : $$Id$$
 */
public class ReferenceIdProviderTest {

	@Test
	public void shouldHandleHash() {
		String referenceId = ReferenceIdProvider
				.getRefId(new OWLNamedIndividualImpl(IRI.create("http://purl.obolibrary.org/obo#OBI_0001458")));
		Assert.assertThat(referenceId, is("OBI_0001458"));
	}

	@Test
	public void shouldHandleNoHash() {
		String referenceId = ReferenceIdProvider
				.getRefId(new OWLNamedIndividualImpl(IRI.create("http://purl.obolibrary.org/obo/OBI_0001458")));
		Assert.assertThat(referenceId, is("OBI_0001458"));
	}

	@Test
	public void shouldHandleMailto() {
		String referenceId = ReferenceIdProvider
				.getRefId(new OWLNamedIndividualImpl(IRI.create("mailto:obi-users@googlegroups.com")));
		Assert.assertThat(referenceId, is("mailto:obi-users@googlegroups.com"));
	}
}