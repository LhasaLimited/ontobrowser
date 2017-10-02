package com.novartis.pcs.ontology.service.parser.owl;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.semanticweb.owlapi.OWLAPIParsersModule;
import org.semanticweb.owlapi.OWLAPIServiceLoaderModule;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportListener;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;
import org.semanticweb.owlapi.util.OWLOntologyWalker;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.novartis.pcs.ontology.dao.OntologyDAOLocal;
import com.novartis.pcs.ontology.dao.TermDAOLocal;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.OntologyAlias;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.VersionedEntity;
import com.novartis.pcs.ontology.service.parser.ParseContext;
import com.novartis.pcs.ontology.service.parser.owl.interceptor.OwlapiInterceptorModule;
import com.novartis.pcs.ontology.service.parser.owl.interceptor.OwlapiLoaderInterceptor;

import uk.ac.manchester.cs.owl.owlapi.OWLAPIImplModule;
import uk.ac.manchester.cs.owl.owlapi.concurrent.Concurrency;

@Stateless
@Local(OWLParsingServiceLocal.class)
public class OWLParsingServiceImpl implements OWLParsingServiceLocal {
	private final Logger logger = Logger.getLogger(getClass().getName());

	@EJB
	private OntologyDAOLocal ontologyDAO;

	@EJB
	private TermDAOLocal termDAO;

	@PersistenceContext(unitName = "ontobrowser")
	protected EntityManager entityManager;

	private class MyOWLOntologyWalker extends OWLOntologyWalker {
		MyOWLOntologyWalker(OWLOntology owlOntology) {
			super(Collections.singleton(owlOntology));
			visitor = new OWLObjectVisitorAdapter();
		}
	}

	@Override
	public ParseContext parseOWLontology(InputStream inputStream, Ontology mainOntology, final OWLParserContext context)
			throws OWLOntologyCreationException {

		Map<IRI, Ontology> alreadyImported = createIgnoredConfig(
				ontologyDAO.loadNonIntermediateWithAliases(mainOntology));
		OWLOntologyManager owlOntologyManager = createInterceptedOntologyManager(alreadyImported);

		StreamDocumentSource ds = new StreamDocumentSource(inputStream);
		OWLOntology mainOwlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(ds);

		OWLDocumentFormat ontologyFormat = owlOntologyManager.getOntologyFormat(mainOwlOntology);
		context.getOntology().setSourceFormat(ontologyFormat.getClass().getSimpleName());

		List<OWLOntology> sortedImportsClosure = owlOntologyManager.getSortedImportsClosure(mainOwlOntology);
		logger.log(INFO, "Sorted import closure: {0}", listToString(sortedImportsClosure));

		List<OWLOntology> mainImportClosure = Lists.reverse(sortedImportsClosure);
		logger.log(INFO, "Final import order: {0}", listToString(mainImportClosure));

		List<Ontology> existingOntologies = new ArrayList<>();

		Map<String, Ontology> ontologyMap = new HashMap<>();
		for (OWLOntology owlOntology : mainImportClosure) {
			String importedUri = getImportedUri(owlOntology);
			Ontology ontology;
			if (mainImportClosure.indexOf(owlOntology) == mainImportClosure.size() - 1) {
				ontology = mainOntology; // last one is a main imported
			} else {
				Ontology foundByAlias = ontologyDAO.loadByAlias(importedUri);
				if (foundByAlias != null && !foundByAlias.equals(mainOntology)) {
					ontology = foundByAlias;
					existingOntologies.add(ontology);
				} else {
					ontology = createOntology(context, getImportedName(owlOntology));
				}
			}
			if (ontology.getId() == 0) {
				// mainOntology is merged later
				ontology = entityManager.merge(ontology);
			}
			ontologyMap.put(importedUri, ontology);
		}

		for (Ontology existing : existingOntologies) {
			context.addTerms(termDAO.loadAll(existing));
		}

		mapImportTree(mainOntology, mainImportClosure, ontologyMap);
		removeBackReferences(mainOntology, new HashSet<>());
		visitWithWalker(context, mainImportClosure, ontologyMap);

		context.setOntology(mainOntology);

		Boolean validated = validateDuplicates(context);
		if (!validated) {
			throw new ParsingException("Ontology saving exception");
		}
		Map<String, Term> terms2 = context.getTerms();
		logger.log(Level.INFO, () -> "Terms count:" + terms2.values().size());

		logger.log(Level.INFO, "The End!");
		return new ParseContextImpl(terms2.values(), context.getDatasources(), context.getRelationshipTypes(),
				context.getAnnotationTypes());
	}

	private Map<IRI, Ontology> createIgnoredConfig(final Collection<Ontology> ontologies) {
		Map<IRI, Ontology> ignoredIRIs = new HashMap<>();
		for (Ontology ontology : ontologies) {
			String sourceUri = ontology.getSourceUri();
			if (!Strings.isNullOrEmpty(sourceUri)) {
				ignoredIRIs.put(IRI.create(sourceUri), ontology);
			}
			String sourceRelease = ontology.getSourceRelease();
			if (!Strings.isNullOrEmpty(sourceRelease)) {
				ignoredIRIs.put(IRI.create(sourceRelease), ontology);
			}
			for (OntologyAlias alias : ontology.getAliases()) {
				ignoredIRIs.put(IRI.create(alias.getAliasUrl()), ontology);
			}
		}
		return ignoredIRIs;
	}

	private OWLOntologyManager createInterceptedOntologyManager(final Map<IRI, Ontology> ignoredConfig) {
		Injector injector = Guice.createInjector(new OWLAPIImplModule(Concurrency.NON_CONCURRENT),
				new OWLAPIParsersModule(), new OWLAPIServiceLoaderModule(), new OwlapiInterceptorModule());
		OWLOntologyManager owlOntologyManager = injector.getInstance(OWLOntologyManager.class);
		injector.injectMembers(owlOntologyManager);

		OwlapiLoaderInterceptor interceptor = injector.getInstance(OwlapiLoaderInterceptor.class);
		interceptor.setIgnored(ignoredConfig);

		owlOntologyManager.addMissingImportListener(
				(MissingImportListener) event -> logger.log(INFO, "Missing import: " + event.getImportedOntologyURI()));
		return owlOntologyManager;
	}

	private List<OWLOntologyID> listToString(final List<OWLOntology> sortedImportsClosure) {
		return sortedImportsClosure.stream().map(OWLOntology::getOntologyID).collect(Collectors.toList());
	}

	private Ontology createOntology(final OWLParserContext context, final String importedName) {
		final Ontology ontology = new Ontology(importedName, context.getCurator(), context.getVersion());
		ontology.setStatus(VersionedEntity.Status.APPROVED);
		ontology.setApprovedVersion(context.getVersion());
		ontology.setInternal(false);
		ontology.setIntermediate(true);
		return ontology;
	}

	private String getImportedName(final OWLOntology importedOwlOntology) {
		Optional<IRI> documentIRI = importedOwlOntology.getOntologyID().getDefaultDocumentIRI();
		return documentIRI.isPresent() ? documentIRI.get().getRemainder().get()
				: importedOwlOntology.getOntologyID().toString();
	}

	private String getImportedUri(final OWLOntology importedOwlOntology) {
		Optional<IRI> documentIRI = importedOwlOntology.getOntologyID().getDefaultDocumentIRI();
		return documentIRI.isPresent() ? documentIRI.get().toString() : importedOwlOntology.getOntologyID().toString();
	}

	private void mapImportTree(final Ontology mainOntology, final List<OWLOntology> mainImportClosure, final Map<String, Ontology> ontologyMap) {
		for (OWLOntology owlOntology : mainImportClosure) {
			Ontology ontology = ontologyMap.get(getImportedUri(owlOntology));
			if (ontology.equals(mainOntology) || ontology.isIntermediate()) { // not already imported
				Set<OWLOntology> directImports = owlOntology.getDirectImports();
				Set<Ontology> imported = directImports.stream().map(o -> ontologyMap.get(getImportedUri(o))).collect(Collectors.toSet());
				logger.log(INFO, "Setting imports [ontology={0},imported={1}]", new String[] { ontology.toString(), imported.toString() });
				ontology.setImportedOntologies(imported);
			}
		}
	}

	private void removeBackReferences(final Ontology parentOntology, final Set<Ontology> visited) {
		visited.add(parentOntology);
		parentOntology.getImportedOntologies().removeIf(visited::contains);
		parentOntology.getImportedOntologies().forEach(ontology ->  removeBackReferences(ontology, visited));
	}

	private void visitWithWalker(final OWLParserContext context, final List<OWLOntology> mainImportClosure, final Map<String, Ontology> ontologyMap) {
		for (OWLOntology owlOntology : mainImportClosure) {
			Ontology ontology = ontologyMap.get(getImportedUri(owlOntology));
			context.setOntology(ontology);
			OWLOntologyWalker owlObjectWalker = new MyOWLOntologyWalker(owlOntology);
			ParsingStructureWalker parsingStructureWalker = new ParsingStructureWalker(owlObjectWalker, context);
			parsingStructureWalker.visit(owlOntology);
		}
	}

	private Boolean validateDuplicates(OWLParserContext context) {
		final MutableBoolean validated = new MutableBoolean(true);
		Map<String, Term> terms = context.getTerms();
		Map<String, List<Term>> grouped = terms.values().stream().collect(Collectors.groupingBy(Term::getReferenceId));
		grouped.forEach((referenceId, termList) -> {
			if (termList.size() > 1) {
				logger.log(SEVERE, "Duplicated referenceId for {}", referenceId);
				String termsStr = termList.stream().map(ReflectionToStringBuilder::toString)
						.collect(Collectors.joining("; "));
				logger.log(SEVERE, "Terms [{}]", termsStr);
				validated.setFalse();
			}
		});

		terms.forEach((termName, term) -> {
			Map<String, Set<String>> relationshipsIndex = new HashMap<>();
			for (Relationship aRelationship : term.getRelationships()) {
				Set<String> types = relationshipsIndex.computeIfAbsent(aRelationship.getRelatedTerm().getReferenceId(),
						id -> new HashSet<>());
				String relationshipTypeId = aRelationship.getType().getRelationship();
				if (types.contains(relationshipTypeId)) {
					logger.log(INFO, "Duplicated relationship {0} {1} {2}", new String[] { term.getName(),
							aRelationship.getRelatedTerm().getName(), aRelationship.getType().getRelationship() });
				} else {
					types.add(relationshipTypeId);
				}
			}
		});
		return validated.getValue();
	}

}
