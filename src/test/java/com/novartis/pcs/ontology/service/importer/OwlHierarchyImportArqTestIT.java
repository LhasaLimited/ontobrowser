package com.novartis.pcs.ontology.service.importer;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;

import org.hamcrest.CoreMatchers;
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

import com.google.common.collect.ImmutableSet;
import com.novartis.pcs.ontology.dao.CuratorDAOLocal;
import com.novartis.pcs.ontology.dao.OntologyDAOLocal;
import com.novartis.pcs.ontology.dao.RelationshipDAOLocal;
import com.novartis.pcs.ontology.dao.TermDAOLocal;
import com.novartis.pcs.ontology.entity.DuplicateEntityException;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.Term;

@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)
public class OwlHierarchyImportArqTestIT {

	private static final String ROOT_NAME = "assay bioassay component";
	private static final String ROOT_IRI = "OB_00001";
	public static final String ONTOLOGY_NAME = "bao_complete.owl";

	@EJB(beanName = "owlImportService")
	private OntologyImportServiceLocal importService;

	@EJB
	private CuratorDAOLocal curatorDAOLocal;

	@EJB
	private OntologyDAOLocal ontologyDAOLocal;

	@EJB
	private TermDAOLocal termDAO;

	@EJB
	private RelationshipDAOLocal relationshipDAO;

	@Deployment(name = "ontobrowser")
	public static WebArchive create() {
		File[] testDeps = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies()
				.importTestDependencies().resolve().withTransitivity().asFile();

		return ShrinkWrap.create(WebArchive.class, "ontobrowser.war").addAsLibraries(testDeps)
				.addPackages(true, "com.novartis.pcs.ontology")
				.addPackages(false, "org.semanticweb.owlapi.util", "org.coode.owlapi.obo12.parser")
				.addAsResource("META-INF/persistence-test.xml", "META-INF/persistence.xml")
				.addAsResource(ONTOLOGY_NAME);
	}

	@Before
	public void loadOntology() throws DuplicateEntityException, InvalidEntityException {
		InputStream ontobrowserOwl = this.getClass().getResourceAsStream("/bao_complete.owl");
		importService.importOntology(ONTOLOGY_NAME, ontobrowserOwl, curatorDAOLocal.loadByUsername("SYSTEM"), Collections.emptyList(), true);

		Ontology ontology = ontologyDAOLocal.loadByName(ONTOLOGY_NAME);
		assertThat(ontology, notNullValue());
	}

	@Test
	/**
	 * The behaviour is different here than in the Protege, bao_metadata.owl comes from OWL API
	 */
	public void shouldImportDirectOntology() throws DuplicateEntityException, InvalidEntityException {
		Ontology baoMetadata = ontologyDAOLocal.loadByName("bao_metadata.owl");
		List<Ontology> baoVocabularyAssayClosure = ontologyDAOLocal.loadClosure("bao_vocabulary_properties.owl");
		assertThat(baoVocabularyAssayClosure.size(), is(2));
		assertThat(baoVocabularyAssayClosure, hasItem(baoMetadata));
	}

	@Test
	public void shouldLoadTermHierarchy() {
		Term assayBioAssayComponent = termDAO.loadByReferenceId("BAO_0003112", ONTOLOGY_NAME);
		Collection<Relationship> relationships = relationshipDAO.loadHierarchy(assayBioAssayComponent.getId(),
				"bao_vocabulary_assay.owl", false);
		assertThat(relationships.size(), is(6));
		Set<String> termNames = relationships.stream().map(r -> r.getTerm().getName()).collect(Collectors.toSet());
		assertThat(termNames, is(ImmutableSet.of("bioassay", "bioassay specification", "bioassay type",
				"experimental specification", "measure group", "measure group specification")));
	}

}
