package com.novartis.pcs.ontology.service.importer;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.novartis.pcs.ontology.dao.CuratorDAOLocal;

@RunWith(Arquillian.class)
public class OntologyImportServiceImplArqTestIT {

	Logger logger = Logger.getLogger(getClass().getName());

	@EJB(beanName = "oboImportService")
	private OntologyImportServiceLocal importService;

	@EJB
	private CuratorDAOLocal curatorDAOLocal;

	@Deployment(name = "ontobrowser")
	public static WebArchive create() {
		File[] testDeps = Maven.resolver().loadPomFromFile("pom.xml")
				.importRuntimeDependencies().importTestDependencies()
				.resolve().withTransitivity().asFile();

		return ShrinkWrap.create(WebArchive.class, "ontobrowser.war").addAsLibraries(testDeps).addPackages(true, "com.novartis.pcs.ontology")
				.addPackages(false, "org.semanticweb.owlapi.util", "org.coode.owlapi.obo12.parser").addAsResource("META-INF/persistence.xml")
				.addAsResource("bao.obo");
	}

	@Test
	@ShouldMatchDataSet("bao_obo_dbunit.xml")
	public void shouldImportOboOntology() throws Exception {
		InputStream baoObo = this.getClass().getResourceAsStream("/bao.obo");
		importService.importOntology("BioAssay OBO", baoObo, curatorDAOLocal.loadByUsername("SYSTEM"));
	}

}
