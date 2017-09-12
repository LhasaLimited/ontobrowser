/**
 * Copyright © 2017 Lhasa Limited
 * File created: 16/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.export;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.novartis.pcs.ontology.dao.CuratorDAOLocal;
import com.novartis.pcs.ontology.dao.OntologyDAOLocal;
import com.novartis.pcs.ontology.dao.TermDAOLocal;
import com.novartis.pcs.ontology.entity.DuplicateEntityException;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.service.importer.OntologyImportServiceLocal;

/**
 * @author Artur Polit
 * @since 16/08/2017
 */
@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)
public class OntologyExportOBIRdfTestIT {

	private static final String ONTOLOGY_NAME = "obi";

	@EJB(beanName = "owlImportService")
	private OntologyImportServiceLocal importService;

	@EJB
	private CuratorDAOLocal curatorDAOLocal;

	@EJB
	private OntologyDAOLocal ontologyDAOLocal;

	@EJB
	private TermDAOLocal termDAO;

	@EJB
	private OntologyExportServiceLocal exportService;

	private Ontology ontology;

	@Deployment(name = "ontobrowser")
	public static WebArchive create() {
		File[] testDeps = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies()
				.importTestDependencies().resolve().withTransitivity().asFile();

		return ShrinkWrap.create(WebArchive.class, "ontobrowser.war").addAsLibraries(testDeps)
				.addPackages(true, "com.novartis.pcs.ontology")
				.addPackages(false, "org.semanticweb.owlapi.util", "org.coode.owlapi.obo12.parser")
				.addAsResource("META-INF/persistence-test.xml", "META-INF/persistence.xml").addAsResource("obi.owl");
	}

	@Before
	public void loadOntology() throws DuplicateEntityException, InvalidEntityException {
		InputStream ontobrowserOwl = this.getClass().getResourceAsStream("/obi.owl");
		importService.importOntology(ONTOLOGY_NAME, ontobrowserOwl, curatorDAOLocal.loadByUsername("SYSTEM"));

		ontology = ontologyDAOLocal.loadByName(ONTOLOGY_NAME);
		assertThat(ontology, notNullValue());
	}

	@Test
	public void shouldExportOwlClassWithSourceIRI() throws OntologyNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		exportService.exportOntology(ONTOLOGY_NAME, baos, OntologyFormat.RDFXML);
		String rdfXML = baos.toString();
		// Individual with proper IRI
		assertThat(rdfXML,
				containsString("<owl:NamedIndividual rdf:about=\"http://purl.obolibrary.org/obo/OBI_0000462\">"));
		assertThat(rdfXML, containsString(
				"<rdfs:label rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">Affymetrix</rdfs:label>"));
	}

}
/*
 * ---------------------------------------------------------------------* This
 * software is the confidential and proprietary information of Lhasa Limited
 * Granary Wharf House, 2 Canal Wharf, Leeds, LS11 5PY --- No part of this
 * confidential information shall be disclosed and it shall be used only in
 * accordance with the terms of a written license agreement entered into by
 * holder of the information with LHASA Ltd.
 * ---------------------------------------------------------------------
 */
