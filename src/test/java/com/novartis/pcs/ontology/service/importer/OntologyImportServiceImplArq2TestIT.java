package com.novartis.pcs.ontology.service.importer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ejb.EJB;

import org.coode.owlapi.obo12.parser.OBOVocabulary;
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
import com.novartis.pcs.ontology.entity.Annotation;
import com.novartis.pcs.ontology.entity.CrossReference;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.DuplicateEntityException;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.Synonym;
import com.novartis.pcs.ontology.entity.Term;

import junit.framework.AssertionFailedError;

@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)
public class OntologyImportServiceImplArq2TestIT {

	private static final String ROOT_NAME = "assay bioassay component";
	private static final String ROOT_IRI = "OB_00001";
	Logger logger = Logger.getLogger(getClass().getName());

	@EJB(beanName = "owlImportService")
	private OntologyImportServiceLocal importService;

	@EJB
	private CuratorDAOLocal curatorDAOLocal;

	@EJB
	private OntologyDAOLocal ontologyDAOLocal;

	@EJB
	private TermDAOLocal termDAO;
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
		importService.importOntology("OntobrowserTest", ontobrowserOwl, curatorDAOLocal.loadByUsername("SYSTEM"));

		ontology = ontologyDAOLocal.loadByName("OntobrowserTest");
		assertThat(ontology, notNullValue());
	}

	@Test
	public void shouldImportOntology() throws DuplicateEntityException, InvalidEntityException {
		assertThat(ontology.getDescription(), is("Ontology comment 2 Ontology comment"));
	}

	@Test
	public void shouldImportTermWithDefinition() throws DuplicateEntityException, InvalidEntityException {
		Term term = termDAO.loadByName(ROOT_NAME, ontology, true);
		assertThat(term, notNullValue());
		assertThat(term.getReferenceId(), is(ROOT_IRI));
		assertThat(term.getDefinition(), is("The biological assay"));
		assertThat(term.getComments(), is("Some comment"));
	}

	@Test
	public void shouldImportCrossReference() throws DuplicateEntityException, InvalidEntityException {
		Term term = termDAO.loadByName(ROOT_NAME, ontology, true);
		assertThat(term, notNullValue());

		assertThat(term.getCrossReferences().size(), is(1));
		CrossReference crossReference = term.getCrossReferences().iterator().next();

		Datasource datasource = crossReference.getDatasource();
		assertThat(datasource, notNullValue());
		assertThat(datasource.getAcronym(), is("DICT"));

		assertThat(crossReference.getReferenceId(), is("D0001"));
	}

	@Test
	public void shouldImportSubclass() throws DuplicateEntityException, InvalidEntityException {
		Term term = termDAO.loadByName("assay kit", ontology, true);
		assertThat(term.getReferenceId(), is("OB_00003"));
		Optional<Relationship> found = term.getRelationships().stream()
				.filter(relationship -> OBOVocabulary.IS_A.getName().equals(relationship.getType().getRelationship()))
				.findAny();
		Term relatedTerm = found.orElseThrow(AssertionFailedError::new).getRelatedTerm();
		assertThat(relatedTerm.getName(), is(ROOT_NAME));
		assertThat(relatedTerm.getReferenceId(), is(ROOT_IRI));
	}

	@Test
	public void shouldImportSynonyms() {
		Term term = termDAO.loadByName("assay kit", ontology, true);
		Map<Synonym.Type, Synonym> synonymMap = term.getSynonyms().stream()
				.collect(Collectors.toMap(Synonym::getType, Function.identity()));
		assertThat(synonymMap.get(Synonym.Type.BROAD).getSynonym(), is("assay kit broad"));
		assertThat(synonymMap.get(Synonym.Type.EXACT).getSynonym(), is("assay kit exact"));
		assertThat(synonymMap.get(Synonym.Type.NARROW).getSynonym(), is("assay kit narrow"));
		assertThat(synonymMap.get(Synonym.Type.RELATED).getSynonym(), is("assay kit related"));
	}

	@Test
	public void shouldImportAnnotation() {
		Term term = termDAO.loadByName("assay kit", ontology, true);
		Optional<Annotation> first = term.getAnnotations().stream().filter(
				a -> a.getAnnotationType().getPrefixedXmlType().equals("seeAlso"))
				.findFirst();
		Annotation annotation = first.orElseThrow(AssertionFailedError::new);
		assertThat(annotation.getAnnotation(), is("See the wikipedia"));
	}

	@Test
	public void shouldMapAnnotationLabel() {
		Term term = termDAO.loadByName("assay kit", ontology, true);
		Optional<Annotation> first = term.getAnnotations().stream().filter(
				a -> a.getAnnotationType().getPrefixedXmlType().equals("IAO_0000114"))
				.findFirst();
		Annotation annotation = first.orElseThrow(AssertionFailedError::new);
		assertThat(annotation.getAnnotation(), is("IAO_0000122"));
		assertThat(annotation.getAnnotationType().getAnnotationType(), is("has curation status"));
	}

}