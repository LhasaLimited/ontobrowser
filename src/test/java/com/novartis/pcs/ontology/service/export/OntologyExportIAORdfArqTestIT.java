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
package com.novartis.pcs.ontology.service.export;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;

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
public class OntologyExportIAORdfArqTestIT {

	private static final String ONTOLOGY_NAME = "iao";

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
				.addAsResource("iao-merged.owl");
	}

	@Before
	public void loadOntology() throws DuplicateEntityException, InvalidEntityException {
		InputStream ontobrowserOwl = this.getClass().getResourceAsStream("/iao-merged.owl");
		importService.importOntology(ONTOLOGY_NAME, ontobrowserOwl, curatorDAOLocal.loadByUsername("SYSTEM"),
				Collections.emptyList(), true);

		ontology = ontologyDAOLocal.loadByName(ONTOLOGY_NAME);
		assertThat(ontology, notNullValue());
	}

	@Test
	public void shouldExportOwlClassWithSourceIRI() throws OntologyNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		exportService.exportOntology(ONTOLOGY_NAME, baos, OntologyFormat.RDFXML);
		String rdfXML = baos.toString();
		// entity with proper IRI
		assertThat(rdfXML, containsString("<obo:BFO_0000179>entity</obo:BFO_0000179>"));
		// annotation property label
		assertThat(rdfXML, containsString("<rdfs:label rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">defaultLanguage</rdfs:label>"));
		System.out.print(rdfXML);
	}

}
