package com.novartis.pcs.ontology.service.importer;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

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
import com.novartis.pcs.ontology.dao.RelationshipDAOLocal;
import com.novartis.pcs.ontology.dao.TermDAOLocal;
import com.novartis.pcs.ontology.entity.DuplicateEntityException;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.OntologyAlias;
import com.novartis.pcs.ontology.service.OntologyTermServiceLocal;

@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)
public class AliasedOntologyImportArqTestIT {

	@EJB(beanName = "owlImportService")
	private OntologyImportServiceLocal importService;

	@EJB
	private CuratorDAOLocal curatorDAOLocal;

	@EJB
	private OntologyDAOLocal ontologyDAOLocal;

	@EJB
	private RelationshipDAOLocal relationshipDAO;

	@EJB
	private TermDAOLocal termDAO;

	@EJB
	private OntologyTermServiceLocal ontologyTermService;

	private Ontology ontology;

	@Deployment(name = "ontobrowser")
	public static WebArchive create() {
		File[] testDeps = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies()
				.importTestDependencies().resolve().withTransitivity().asFile();

		return ShrinkWrap.create(WebArchive.class, "ontobrowser.war").addAsLibraries(testDeps)
				.addPackages(true, "com.novartis.pcs.ontology")
				.addPackages(false, "org.semanticweb.owlapi.util", "org.coode.owlapi.obo12.parser")
				.addAsResource("META-INF/persistence-test.xml", "META-INF/persistence.xml")
				.addAsResource("test-reference/test-reference.owl").addAsResource("simple.owl");
	}

	@Before
	public void loadOntology() throws DuplicateEntityException, InvalidEntityException {
		InputStream ontobrowserOwl = this.getClass().getResourceAsStream("/test-reference/test-reference.owl");
		importService.importOntology("OntobrowserTest", ontobrowserOwl, curatorDAOLocal.loadByUsername("SYSTEM"), Collections.emptyList(), true);

		ontology = ontologyDAOLocal.loadByName("OntobrowserTest");
		assertThat(ontology, notNullValue());
	}

	@Test
	public void shouldImportAliasedOntology() throws DuplicateEntityException, InvalidEntityException {
		OntologyAlias alias = new OntologyAlias();
		alias.setOntology(ontology);
		alias.setAliasUrl("http://www.lhasalimited.org/0.1/ontobrowser.owl");
		ontology.getAliases().add(alias);

		InputStream stream = this.getClass().getResourceAsStream("/simple.owl");
		importService.importOntology("simple.owl", stream, curatorDAOLocal.loadByUsername("SYSTEM"), Collections.emptyList(), true);

		Ontology simpleOntology = ontologyDAOLocal.loadByName("simple.owl");
		assertThat(simpleOntology.getImportedOntologies(), hasItem(ontology));
	}

}