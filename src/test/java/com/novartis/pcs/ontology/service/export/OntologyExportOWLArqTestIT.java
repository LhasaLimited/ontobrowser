/**
 * Copyright © 2017 Lhasa Limited
 * File created: 16/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.export;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import javax.ejb.EJB;

import com.google.common.collect.ImmutableMap;
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
import org.xmlunit.matchers.EvaluateXPathMatcher;

/**
 * @author Artur Polit
 * @since 16/08/2017
 */
@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)
public class OntologyExportOWLArqTestIT {

	private static final String ONTOBROWSER_TEST = "OntobrowserTest";

	private static final Map<String, String> PREFIXES = ImmutableMap.of(
			"ob", "http://www.lhasalimited.org/ontobrowser.owl#" ,
			"owl", "http://www.w3.org/2002/07/owl#",
			"rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
			"rdfs", "http://www.w3.org/2000/01/rdf-schema#");

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
				.addAsResource("META-INF/persistence-test.xml", "META-INF/persistence.xml")
				.addAsResource("test-reference/test-reference.owl");
	}

	@Before
	public void loadOntology() throws DuplicateEntityException, InvalidEntityException {
		InputStream ontobrowserOwl = this.getClass().getResourceAsStream("/test-reference/test-reference.owl");
		importService.importOntology("OntobrowserTest", ontobrowserOwl, curatorDAOLocal.loadByUsername("SYSTEM"), Collections.emptyList(), true);

		ontology = ontologyDAOLocal.loadByName("OntobrowserTest");
		assertThat(ontology, notNullValue());
	}

	@Test
	public void shouldExportOwlClassWithSourceIRI() throws OntologyNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		exportService.exportOntology(ONTOBROWSER_TEST, baos, OntologyFormat.RDFXML);
		String rdfXML = baos.toString();
		assertThat(rdfXML, containsString("about=\"http://www.lhasalimited.org/ontobrowser.owl#OB_00001\""));

	}

	@Test
	public void shouldExportAnnotationValueAsUrl() throws OntologyNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		exportService.exportOntology(ONTOBROWSER_TEST, baos, OntologyFormat.RDFXML);
		String rdfXML = baos.toString();
		assertThat(rdfXML, containsString("<obo:IAO_0000114 rdf:resource=\"http://purl.obolibrary.org/obo/IAO_0000122\"/>"));
	}

	@Test
	public void shouldExportClassObjectProperty() throws OntologyNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		exportService.exportOntology(ONTOBROWSER_TEST, baos, OntologyFormat.RDFXML);
		String rdfXML = baos.toString();
		assertThat(rdfXML,
				EvaluateXPathMatcher.hasXPath(
						"//owl:Class[@rdf:about=\"http://www.lhasalimited.org/ontobrowser.owl#classB\"]"
								+ "/rdfs:subClassOf/owl:Restriction/owl:onProperty/@rdf:resource",
						is("http://www.lhasalimited.org/ontobrowser.owl#objectPropertyA")).withNamespaceContext(PREFIXES));
		assertThat(rdfXML, EvaluateXPathMatcher
				.hasXPath("//owl:Class[@rdf:about=\"http://www.lhasalimited.org/ontobrowser.owl#classB\"]"
						+ "/rdfs:subClassOf/owl:Restriction/owl:someValuesFrom/@rdf:resource", is("http://www.lhasalimited.org/ontobrowser.owl#classA"))
				.withNamespaceContext(PREFIXES));
			}

	@Test
	public void shouldExportIndividualObjectProperty() throws OntologyNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		exportService.exportOntology(ONTOBROWSER_TEST, baos, OntologyFormat.RDFXML);
		String rdfXML = baos.toString();
		assertThat(rdfXML,
				EvaluateXPathMatcher.hasXPath("//owl:NamedIndividual[@rdf:about=\"http://www.lhasalimited.org/ontobrowser.owl#TopClassIndividual\"]" +
								"/ob:someObjectProperty/@rdf:resource",
						is("http://www.lhasalimited.org/ontobrowser.owl#assayIndividual")).withNamespaceContext(PREFIXES));
	}

	@Test
	public void shouldExportDataPropertyValue() throws OntologyNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		exportService.exportOntology(ONTOBROWSER_TEST, baos, OntologyFormat.RDFXML);
		String rdfXML = baos.toString();
		assertThat(rdfXML,
				EvaluateXPathMatcher.hasXPath("//owl:NamedIndividual[@rdf:about=\"http://www.lhasalimited.org/ontobrowser.owl#TopClassIndividual\"]" +
								"/ob:someDataProperty",
						is("Data property value")).withNamespaceContext(PREFIXES));
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
