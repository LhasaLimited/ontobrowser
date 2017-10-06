package com.novartis.pcs.ontology.service.importer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag.TAG_IS_A;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ejb.EJB;

import org.coode.owlapi.obo12.parser.OBOVocabulary;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.novartis.pcs.ontology.dao.CuratorDAOLocal;
import com.novartis.pcs.ontology.dao.OntologyDAOLocal;
import com.novartis.pcs.ontology.dao.RelationshipDAOLocal;
import com.novartis.pcs.ontology.dao.TermDAOLocal;
import com.novartis.pcs.ontology.entity.Annotation;
import com.novartis.pcs.ontology.entity.CrossReference;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.DuplicateEntityException;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.PropertyType;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.Synonym;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.TermType;
import com.novartis.pcs.ontology.service.OntologyTermServiceLocal;

import junit.framework.AssertionFailedError;

@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)
public class OwlReferenceImportTestIT {

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
	public void shouldImportOntology() throws DuplicateEntityException, InvalidEntityException {
		assertThat(ontology.getDescription(), is("Ontology comment Ontology comment 2"));
		assertThat(ontology.getSourceNamespace(), is("http://www.lhasalimited.org/ontobrowser.owl#"));
		assertThat(ontology.getSourceUri(), is("http://www.lhasalimited.org/ontobrowser.owl"));
		assertThat(ontology.getSourceRelease(), is("http://www.lhasalimited.org/1.0/ontobrowser.owl"));
	}

	@Test
	public void shouldImportTermWithDefinition() throws DuplicateEntityException, InvalidEntityException {
		Term term = termDAO.loadByName(ROOT_NAME, ontology, true);
		assertThat(term, notNullValue());
		assertThat(term.getReferenceId(), is(ROOT_IRI));
		assertThat(term.getDefinition(), is("The biological assay"));
		assertThat(term.getComments(), is("Some comment"));
		assertThat(term.getUrl(), is("http://www.lhasalimited.org/ontobrowser.owl#OB_00001"));
	}

	@Test
	public void shouldImportCrossReference() throws DuplicateEntityException, InvalidEntityException {
		Term term = termDAO.loadByName(ROOT_NAME, ontology, true);
		assertThat(term, notNullValue());

		assertThat(term.getCrossReferences().size(), is(1));
		CrossReference crossReference = term.getCrossReferences().iterator().next();

		Datasource datasource = crossReference.getDatasource();
		assertThat(datasource, notNullValue());
		assertThat(datasource.getAcronym(), is("SEND"));

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
		Optional<Annotation> first = term.getAnnotations().stream()
				.filter(a -> a.getAnnotationType().getPrefixedXmlType().equals("seeAlso")).findFirst();
		Annotation annotation = first.orElseThrow(AssertionFailedError::new);
		assertThat(annotation.getAnnotation(), is("See the wikipedia"));
	}

	@Test
	public void shouldMapAnnotationLabel() {
		Term term = termDAO.loadByName("assay kit", ontology, true);
		Optional<Annotation> first = term.getAnnotations().stream()
				.filter(a -> a.getAnnotationType().getPrefixedXmlType().equals("IAO_0000114")).findFirst();
		Annotation annotation = first.orElseThrow(AssertionFailedError::new);
		assertThat(annotation.getAnnotation(), is("http://purl.obolibrary.org/obo/IAO_0000122"));
		assertThat(annotation.getAnnotationType().getAnnotationType(), is("has curation status"));
		assertThat(annotation.getAnnotationType().getType(), is(PropertyType.ANNOTATION));
		assertThat(annotation.getAnnotationType().getDefinitionUrl(), is("http://purl.obolibrary.org/obo/IAO_0000114"));
	}

	@Test
	public void shouldSelectRootTermsForOntology() {
		Term thing = termDAO.loadByReferenceId("Thing", "OWL");
		Collection<Relationship> relationships = ontologyTermService.getRelationships(thing, "OntobrowserTest", false);
		Optional<Relationship> topClassRelOpt = findRel(relationships, "topLeafClass");
		Relationship topClassRel = topClassRelOpt.orElseThrow(AssertionError::new);
		assertThat(topClassRel.isLeaf(), is(Boolean.TRUE));
		Optional<Relationship> parentClassRelOpt = findRel(relationships, ROOT_IRI);
		Relationship parentClassRel = parentClassRelOpt.orElseThrow(AssertionError::new);
		assertThat(parentClassRel.isLeaf(), is(Boolean.FALSE));
	}

	@Test
	public void shouldImportIndividual() {
		Collection<Term> terms = termDAO.loadAll(ontology);
		assertThat(terms.size(), is(10 + 2));
		Term topClassIndividual = termDAO.loadByReferenceId("TopClassIndividual", ontology.getName(), true);
		Term topClass = termDAO.loadByReferenceId("OB_00010", ontology.getName(), true);
		assertThat(topClassIndividual, CoreMatchers.notNullValue());
		assertThat(topClassIndividual.getType(), is(TermType.INDIVIDUAL));
		Relationship someObjectProperty = topClassIndividual.getRelationships().stream()
				.filter(r -> r.getType().getRelationship().equals(TAG_IS_A.getTag())).findAny()
				.orElseThrow(AssertionError::new);
		assertThat(someObjectProperty.getRelatedTerm(), is(topClass));
	}

	private Optional<Relationship> findRel(final Collection<Relationship> relationships, final String referenceId) {
		return relationships.stream().filter(r -> r.getTerm().getReferenceId().equals(referenceId)).findFirst();
	}

	@Test
	public void shouldImportSubClassExpressionWithObjectProperty() {
		Term classB = termDAO.loadByReferenceId("classB", ontology.getName(), true);
		Relationship relationship = classB.getRelationships().stream()
				.filter(r -> r.getType().getRelationship().equals("objectPropertyA")).findAny()
				.orElseThrow(AssertionError::new);
		Assert.assertThat(relationship.getRelatedTerm().getReferenceId(), is("classA"));
	}

	@Test
	public void shouldImportObjectPropertyForIndividual() {
		Term topClassIndividual = termDAO.loadByReferenceId("TopClassIndividual", ontology.getName(), true);
		Relationship relationship = topClassIndividual.getRelationships().stream()
				.filter(r -> r.getType().getRelationship().equals("someObjectProperty")).findAny()
				.orElseThrow(AssertionError::new);
		Assert.assertThat(relationship.getRelatedTerm().getReferenceId(), is("assayIndividual"));
	}

	@Test
	public void shouldImportDataPropertyValue() {
		Term topClassIndividual = termDAO.loadByReferenceId("TopClassIndividual", ontology.getName(), true);
		Optional<Annotation> first = topClassIndividual.getAnnotations().stream()
				.filter(a -> a.getAnnotationType().getPrefixedXmlType().equals("someDataProperty")).findFirst();
		Annotation annotation = first.orElseThrow(AssertionFailedError::new);
		assertThat(annotation.getAnnotation(), is("Data property value"));
		assertThat(annotation.getAnnotationType().getAnnotationType(), is("someDataProperty"));
		assertThat(annotation.getAnnotationType().getType(), is(PropertyType.DATA_PROPERTY));
	}
}
