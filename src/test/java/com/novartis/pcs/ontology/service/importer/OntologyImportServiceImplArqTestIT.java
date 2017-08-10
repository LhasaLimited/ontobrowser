package com.novartis.pcs.ontology.service.importer;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.TransactionAttribute;

import com.novartis.pcs.ontology.dao.AnnotationDAOLocal;
import com.novartis.pcs.ontology.dao.OntologyDAOLocal;
import com.novartis.pcs.ontology.dao.RelationshipDAOLocal;
import com.novartis.pcs.ontology.dao.RelationshipTypeDAOLocal;
import com.novartis.pcs.ontology.dao.SynonymDAOLocal;
import com.novartis.pcs.ontology.dao.TermDAOLocal;
import com.novartis.pcs.ontology.entity.Ontology;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.novartis.pcs.ontology.dao.CuratorDAOLocal;

import static org.hamcrest.core.Is.is;

@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)
public class OntologyImportServiceImplArqTestIT {

	Logger logger = Logger.getLogger(getClass().getName());

	@EJB(beanName = "oboImportService")
	private OntologyImportServiceLocal importService;

	@EJB
	private CuratorDAOLocal curatorDAOLocal;

	@EJB
	private TermDAOLocal termDAOLocal;

	@EJB
	private RelationshipDAOLocal relationshipDAOLocal;

	@EJB
	private RelationshipTypeDAOLocal relationshipTypeDAOLocal;

	@EJB
	private OntologyDAOLocal ontologyDAOLocal;

	@EJB
	private SynonymDAOLocal synonymDAOLocal;

	@EJB
	private AnnotationDAOLocal annotationDAOLocal;

	@Deployment(name = "ontobrowser")
	public static WebArchive create() {
		File[] testDeps = Maven.resolver().loadPomFromFile("pom.xml")
				.importRuntimeDependencies().importTestDependencies()
				.resolve().withTransitivity().asFile();

		return ShrinkWrap.create(WebArchive.class, "ontobrowser.war").addAsLibraries(testDeps)
				.addPackages(true, "com.novartis.pcs.ontology")
				.addPackages(false, "org.semanticweb.owlapi.util", "org.coode.owlapi.obo12.parser")
				.addAsResource("META-INF/persistence-test.xml", "META-INF/persistence.xml")
				.addAsResource("bfo.obo");
	}

	@Test
	public void shouldImportBfoOntology() throws Exception {
		InputStream bfoObo = this.getClass().getResourceAsStream("/bfo.obo");
		importService.importOntology("BFO OBO", bfoObo, curatorDAOLocal.loadByUsername("SYSTEM"));
		Ontology ontology = ontologyDAOLocal.loadByName("BFO OBO");
		Assert.assertThat(termDAOLocal.loadAll(ontology).size(), is(35));
		Assert.assertThat(relationshipDAOLocal.loadAll().size(), is(52));
		Assert.assertThat(relationshipTypeDAOLocal.loadAll().size(), is(5));
	}

}
