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
 * @since 17/08/2017
 */
@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)
public class OntologyExportOBOArqTestIT {

	private static final String BFO_TEST = "BFOTest";

	@EJB(beanName = "oboImportService")
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
				.addAsResource("META-INF/persistence-test.xml", "META-INF/persistence.xml").addAsResource("bfo.obo");
	}

	@Before
	public void loadOBOOntology() throws DuplicateEntityException, InvalidEntityException {
		InputStream bfoOwl = this.getClass().getResourceAsStream("/bfo.obo");
		importService.importOntology(BFO_TEST, bfoOwl, curatorDAOLocal.loadByUsername("SYSTEM"), Collections.emptyList(), true);
		ontology = ontologyDAOLocal.loadByName(BFO_TEST);
		assertThat(ontology, notNullValue());
	}

	@Test
	public void shouldExportOBOAsOWL()
			throws DuplicateEntityException, InvalidEntityException, OntologyNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		exportService.exportOntology(BFO_TEST, baos, OntologyFormat.RDFXML);
		String rdfXML = baos.toString();
		assertThat(rdfXML, containsString("rdf:about=\"http://localhost/ontobrowser/ontologies/BFOTest#BFO:0000001\""));
	}

}

