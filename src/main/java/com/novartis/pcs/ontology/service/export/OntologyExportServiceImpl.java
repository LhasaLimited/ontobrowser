/* 

Copyright 2015 Novartis Institutes for Biomedical Research

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

import static com.novartis.pcs.ontology.entity.VersionedEntity.Status.APPROVED;
import static com.novartis.pcs.ontology.entity.VersionedEntity.Status.OBSOLETE;
import static com.novartis.pcs.ontology.service.export.OntologyExportUtil.createIRI;
import static com.novartis.pcs.ontology.service.export.OntologyExportUtil.escapeOBO;
import static com.novartis.pcs.ontology.service.export.OntologyExportUtil.escapeQuote;
import static com.novartis.pcs.ontology.service.export.OntologyExportUtil.getRelationshipIRISafe;
import static com.novartis.pcs.ontology.service.export.OntologyExportUtil.isBuiltIn;
import static java.util.Arrays.asList;
import static org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag.TAG_IS_A;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import com.novartis.pcs.ontology.entity.PropertyType;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.novartis.pcs.ontology.dao.AnnotationTypeDAOLocal;
import com.novartis.pcs.ontology.dao.DatasourceDAOLocal;
import com.novartis.pcs.ontology.dao.OntologyDAOLocal;
import com.novartis.pcs.ontology.dao.TermDAOLocal;
import com.novartis.pcs.ontology.entity.Annotation;
import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.ControlledVocabularyTerm;
import com.novartis.pcs.ontology.entity.CrossReference;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Synonym;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.TermType;
import com.novartis.pcs.ontology.entity.VersionedEntity.Status;
import com.novartis.pcs.ontology.service.util.TermReferenceIdComparator;
import com.novartis.pcs.ontology.webapp.client.util.UrlValidator;

/**
 * Session Bean implementation class OntologyExportServiceImpl
 */
@Stateless
@Local(OntologyExportServiceLocal.class)
@Remote(OntologyExportServiceRemote.class)
public class OntologyExportServiceImpl implements OntologyExportServiceRemote, OntologyExportServiceLocal {
	
	@EJB
	protected OntologyDAOLocal ontologyDAO;
	
	@EJB
	protected TermDAOLocal termDAO;
	
	@EJB
	protected DatasourceDAOLocal datasourceDAO;

	@EJB
	protected AnnotationTypeDAOLocal annotationTypeDao;
	
	@Resource(lookup="java:global/ontobrowser/export/owl/uri")
	private URL baseURL;
	
	private final Logger logger = Logger.getLogger(getClass().getName());
	
    /**
     * Default constructor. 
     */
    public OntologyExportServiceImpl() {
    }
    
    @Override
	public void exportOntology(String ontologyName, OutputStream os, 
			OntologyFormat format) throws OntologyNotFoundException {    	
    	exportOntology(ontologyName, os, format, false);
    }
    
    @Override
	public void exportOntology(String ontologyName, OutputStream os, 
			OntologyFormat format, boolean includeNonPublicXrefs) 
    		throws OntologyNotFoundException {
    	Collection<Datasource> datasources = datasourceDAO.loadAll();
    	if(!includeNonPublicXrefs) {
			Collection<Datasource> external = new ArrayList<>();
	    	for(Datasource datasource : datasources) {
	    		if(datasource.isPubliclyAccessible()) {
	    			external.add(datasource);
	    		}
	    	}
	    	datasources = external;
    	}
    	
    	exportOntology(ontologyName, os, format, datasources);
    }
    
	@Override
	public void exportOntology(String ontologyName, OutputStream os,
			OntologyFormat format, Collection<Datasource> xrefDatasources) 
			throws OntologyNotFoundException {
		
		Ontology ontology = ontologyDAO.loadByName(ontologyName);
		if(ontology == null || ontology.isCodelist()) {
			throw new OntologyNotFoundException("Ontology not found: " + ontologyName, ontologyName);
		}
		
		logger.info("Exporting " + ontology.getName() + " ontology in " + format + " format");
						
		switch(format) {
		case OBO:
			exportAsOBO(ontology, os, xrefDatasources);
			break;
		case RDFXML:
			exportAsOWL(ontology, os, new RDFXMLOntologyFormat(), xrefDatasources);
			break;
		case OWLXML:
			exportAsOWL(ontology, os, new OWLXMLOntologyFormat(), xrefDatasources);
			break;
		case Manchester:
			exportAsOWL(ontology, os, new ManchesterOWLSyntaxOntologyFormat(), xrefDatasources);
			break;
		case Turtle:
			exportAsOWL(ontology, os, new TurtleOntologyFormat(), xrefDatasources);
			break;	
		default:
			throw new IllegalArgumentException("Invalid/Unsupported ontology export format: " + format);
		}
	}

	private void exportAsOBO(Ontology ontology, OutputStream os, 
			Collection<Datasource> xrefDatasources) {
		Collection<Term> terms = termDAO.loadAll(ontology);
		Date now = new Date();
		DateFormat formatter = new SimpleDateFormat("dd:MM:yyyy HH:mm");
		
		if(!(terms instanceof List<?>)) {
			terms = new ArrayList<>(terms);
		}
		
		Collections.sort((List<Term>)terms, new TermReferenceIdComparator());
		
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"), 4096);
			
			writer.append("format-version: 1.2\n")
					.append("date: ").append(formatter.format(now)).append("\n")
					.append("auto-generated-by: OntoBrowser Export Service\n")
					.append("\n");
			
			Set<RelationshipType> relationshipTypes = new HashSet<>();
			for(Term term : terms) {
				if(term.getStatus().equals(Status.APPROVED) 
						|| term.getStatus().equals(Status.OBSOLETE)) {
					writer.append("[Term]\n")
						.append("id: ").append(term.getReferenceId()).append("\n")
						.append("name: ").append(escapeOBO(term.getName())).append("\n");
					if(term.getDefinition() != null) {
						writer.append("def: \"").append(escapeQuote(term.getDefinition())).append("\" [");
						if(term.getUrl() != null) {
							writer.append(term.getUrl());
						}
						for(CrossReference xref : term.getCrossReferences()) {
							if(xref.isDefinitionCrossReference()) {
								if(term.getUrl() != null) {
									writer.append(", ");
								}
								if(xref.getDatasource() != null 
										&& xrefDatasources.contains(xref.getDatasource())) {
									writer.append(escapeOBO(xref.getDatasource().getAcronym()));
									if(xref.getReferenceId() != null) {
										writer.append(":").append(escapeOBO(xref.getReferenceId()));
									}
									
									if(xref.getDescription() != null) {
										writer.append(" \"").append(escapeQuote(xref.getDescription())).append("\"");
									}
								} else if(xref.getUrl() != null) {
									writer.append(xref.getUrl());
								}
							}
						}
						
						writer.append("]\n");
					}
					
					if(term.getComments() != null) {
						writer.append("comment: ").append(escapeOBO(term.getComments())).append("\n");
					}
					
					for(Synonym synonym : term.getSynonyms()) {
						if(synonym.getStatus().equals(Status.APPROVED)) {
							Datasource datasource = null;
							String referenceId = null;
							String description = null;					
							if(synonym.getControlledVocabularyTerm() != null) {
								ControlledVocabularyTerm ctrldVocabTerm = synonym.getControlledVocabularyTerm();
								datasource = ctrldVocabTerm.getControlledVocabulary().getDatasource();
								referenceId = ctrldVocabTerm.getReferenceId();
								description = synonym.getDescription();
							} else if(synonym.getDatasource() != null) {
								datasource = synonym.getDatasource();
								referenceId = synonym.getReferenceId();
								description = synonym.getDescription();
							}
							
							if(datasource == null || xrefDatasources.contains(datasource)) {
								writer.append("synonym: \"")
									.append(escapeQuote(synonym.getSynonym())).append("\"")
									.append(" ").append(synonym.getType().name()).append(" [");
								if(datasource != null) {
									writer.append(escapeOBO(datasource.getAcronym()));
									if(referenceId != null) {
										writer.append(":").append(escapeOBO(referenceId));
									}
									
									if(description != null) {
										writer.append(" \"").append(escapeQuote(description)).append("\"");
									}
								} else if(synonym.getUrl() != null) {
									writer.append(synonym.getUrl());
								}
								writer.append("]\n");
							}
						}
					}
					
					for(CrossReference xref : term.getCrossReferences()) {
						if(!xref.isDefinitionCrossReference() &&
								(xref.getDatasource() == null || xrefDatasources.contains(xref.getDatasource()))) {
							writer.append("xref: \"");
							if(xref.getDatasource() != null) {
								writer.append(escapeOBO(xref.getDatasource().getAcronym()));
								if(xref.getReferenceId() != null) {
									writer.append(":").append(escapeOBO(xref.getReferenceId()));
								}
								
								if(xref.getDescription() != null) {
									writer.append(" \"").append(escapeQuote(xref.getDescription())).append("\"");
								}
							} else if(xref.getUrl() != null) {
								writer.append(xref.getUrl());
							}
							writer.append("\n");
						}
					}
					
					List<Relationship> relationships = new ArrayList<>(term.getRelationships());
					Collections.sort(relationships, new RelationshipComparator());				
					for(Relationship relationship : relationships) {
						if(relationship.getStatus().equals(Status.APPROVED)) {
							RelationshipType type = relationship.getType();
							relationshipTypes.add(type);
							
							if(relationship.isIntersection()) {
								writer.append("intersection_of: ");
								if (!type.getRelationship().equals(TAG_IS_A.getTag())) {
									writer.append(escapeOBO(type.getRelationship())).append(" ");
								}
							} else if(isBuiltIn(type)) {
								writer.append(type.getRelationship()).append(": ");
							} else {
								writer.append("relationship: ").append(escapeOBO(type.getRelationship())).append(" ");
							}
																	
							writer.append(relationship.getRelatedTerm().getReferenceId())
									.append(" ! ").append(relationship.getRelatedTerm().getName())
									.append("\n");
						}
					}
					
					if(term.getStatus().equals(Status.OBSOLETE)) {
						writer.append("is_obsolete: true\n");
						if(term.getReplacedBy() != null) {
							writer.append("replaced_by: ")
									.append(term.getReplacedBy().getReferenceId())
									.append(" ! ").append(term.getReplacedBy().getName())
									.append("\n");
						}
					}
					
					writer.append("\n");
				}
			}
		
			for(RelationshipType type : relationshipTypes) {
				if(!isBuiltIn(type) && type.getStatus().equals(Status.APPROVED)) {
					RelationshipType inverse = type.getInverseOf();
					RelationshipType transitive = type.getTransitiveOver();
					
					writer.append("[Typedef]\n")
						.append("id: ").append(escapeOBO(type.getRelationship())).append("\n")
						.append("name: ").append(escapeOBO(type.getRelationship().replace('_', ' '))).append("\n");
					
					if(inverse != null) {
						writer.append("inverse_of: ").append(escapeOBO(inverse.getRelationship())).append("\n");
					}
					
					if(transitive != null) {
						writer.append("transitive_over: ").append(escapeOBO(transitive.getRelationship())).append("\n");
					}
					
					if(type.isCyclic()) {
						writer.append("is_cyclic: true\n");
					}
													
					if(type.isSymmetric()) {
						writer.append("is_symmetric: true\n");
					}
													
					if(type.isTransitive()) {
						writer.append("is_transitive: true\n");
					}
													
					writer.append("\n");
				}
			}
			writer.flush();
		} catch(IOException e) {
			logger.log(Level.WARNING, "Failed to export " + ontology.getName() + " in OBO Format" , e);
			throw new RuntimeException(e);
		}
	}

	private void exportAsOWL(Ontology ontology, OutputStream os, OWLOntologyFormat format,
			Collection<Datasource> xrefDatasources) {
		try {

			IRIProvider iriProvider = ontology.isInternal() || ontology.getSourceFormat().startsWith("OBO")
					? new DefaultIRIProvider(ontology, baseURL)
					: new URLIRIProvider();

			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

			OWLDataFactory factory = manager.getOWLDataFactory();
			OWLOntology onto = exportOntology(ontology, manager, factory);
			ExportContext exportContext = new ExportContext(manager, onto, format, iriProvider);
			Collection<AnnotationType> annotationTypes = annotationTypeDao.loadByOntology(ontology);
			exportAnnotationTypes(factory, exportContext, annotationTypes);
			Collection<Term> terms = termDAO.loadAll(ontology);
			exportTerms(iriProvider, factory, terms, exportContext);
			exportTermsIndividuals(iriProvider, factory, terms, exportContext);

			exportRelationshipTypes(factory, exportContext);

			manager.saveOntology(onto, format, os);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to export " + ontology.getName() + " in OWL format", e);
			throw new RuntimeException(e);
		}
	}

	private void exportAnnotationTypes(final OWLDataFactory factory, final ExportContext exportContext, final Collection<AnnotationType> annotationTypes) throws URISyntaxException {
		for (AnnotationType annotationType : annotationTypes) {
			IRI annTypeIRI = exportContext.getIriProvider().getIRI(annotationType);
			// declaration
			exportContext.addAxiom(factory.getOWLDeclarationAxiom(factory.getOWLAnnotationProperty(annTypeIRI)));
			// label
			exportContext.addAxiom(factory.getOWLAnnotationAssertionAxiom(factory.getRDFSLabel(), annTypeIRI,
					factory.getOWLLiteral(annotationType.getAnnotationType())));
		}

	}

	private OWLOntology exportOntology(final Ontology ontology, final OWLOntologyManager manager,
			final OWLDataFactory factory) throws OWLOntologyCreationException, URISyntaxException {

		OWLOntologyID owlOntologyID = Strings.isNullOrEmpty(ontology.getSourceUri())
				|| Strings.isNullOrEmpty(ontology.getSourceRelease())
						? new OWLOntologyID(createIRI(baseURL.toURI(), ontology.getName()))
						: new OWLOntologyID(IRI.create(ontology.getSourceUri()),
								IRI.create(ontology.getSourceRelease()));
		OWLOntology onto = manager.createOntology(owlOntologyID);

		OWLAnnotation ontologyLabel = factory.getOWLAnnotation(factory.getRDFSLabel(),
				factory.getOWLLiteral(ontology.getName()));
		manager.applyChange(new AddOntologyAnnotation(onto, ontologyLabel));

		if (ontology.getDescription() != null) {
			OWLAnnotation comment = factory.getOWLAnnotation(factory.getRDFSComment(),
					factory.getOWLLiteral(ontology.getDescription()));
			manager.applyChange(new AddOntologyAnnotation(onto, comment));
		}
		return onto;
	}

	private void exportTerms(final IRIProvider iriProvider, final OWLDataFactory factory, final Collection<Term> terms,
			final ExportContext exportContext) throws URISyntaxException {
		for (Term term : terms) {
			if ((term.getStatus().equals(Status.APPROVED) || term.getStatus().equals(Status.OBSOLETE))
					&& term.getType().equals(TermType.CLASS)) {

				IRI termIRI = iriProvider.getIRI(term);
				OWLClass termClass = factory.getOWLClass(termIRI);

				exportCommon(factory, exportContext, term, termIRI, termClass);

				Set<OWLClassExpression> intersectClasses = new HashSet<>();
				Set<OWLClassExpression> unionClasses = new HashSet<>();

				exportRelationships(iriProvider, factory, exportContext, term, termClass, intersectClasses,
						unionClasses);
				term.getAnnotations().stream().filter(this::isOWLAnnotation)
						.forEach(annotation -> exportOWLAnnotation(factory, exportContext, termIRI, annotation));

				if (!intersectClasses.isEmpty()) {
					OWLObjectIntersectionOf intersection = factory.getOWLObjectIntersectionOf(intersectClasses);
					OWLAxiom axiom = factory.getOWLEquivalentClassesAxiom(termClass, intersection);
					exportContext.addAxiom(axiom);
				}

				if (!unionClasses.isEmpty()) {
					OWLObjectUnionOf unionOf = factory.getOWLObjectUnionOf(unionClasses);
					OWLAxiom axiom = factory.getOWLEquivalentClassesAxiom(termClass, unionOf);
					exportContext.addAxiom(axiom);
				}
			}
		}
	}

	private void exportCommon(final OWLDataFactory factory, final ExportContext exportContext, final Term term,
			final IRI termIRI, final OWLEntity termClass) {
		exportContext.addAxiom(factory.getOWLDeclarationAxiom(termClass));

		exportContext.addAxiom(factory.getOWLAnnotationAssertionAxiom(factory.getRDFSLabel(), termIRI,
				factory.getOWLLiteral(term.getName())));

		if (term.getComments() != null) {
			OWLLiteral comment = factory.getOWLLiteral(term.getComments());
			OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(factory.getRDFSComment(), termIRI, comment);
			exportContext.addAxiom(axiom);
		}

		if (term.getStatus().equals(OBSOLETE)) {
			OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(factory.getOWLDeprecated(), termIRI,
					factory.getOWLLiteral(true));
			exportContext.addAxiom(axiom);
		}
	}

	private void exportTermsIndividuals(final IRIProvider iriProvider, final OWLDataFactory factory,
			final Collection<Term> terms, final ExportContext exportContext) throws URISyntaxException {
		for (Term term : terms) {
			if ((term.getStatus().equals(Status.APPROVED) || term.getStatus().equals(Status.OBSOLETE))
					&& term.getType().equals(TermType.INDIVIDUAL)) {

				IRI termIRI = iriProvider.getIRI(term);
				OWLNamedIndividual individual = factory.getOWLNamedIndividual(termIRI);

				exportCommon(factory, exportContext, term, termIRI, individual);

				Set<OWLIndividual> sameIndividuals = new HashSet<>();
				Set<OWLIndividual> differentIndividuals = new HashSet<>();

				exportRelationshipsIndividuals(iriProvider, factory, exportContext, term, individual, sameIndividuals,
						differentIndividuals);
				term.getAnnotations().stream().filter(this::isOWLAnnotation)
						.forEach(annotation -> exportOWLAnnotation(factory, exportContext, termIRI, annotation));
				for (Annotation a : term.getAnnotations()) {
					if (PropertyType.DATA_PROPERTY.equals(a.getAnnotationType().getType())) {
						exportDataPropertyAxiom(factory, exportContext, termIRI, a);
					}
				}
			}
		}
	}

	private void exportDataPropertyAxiom(final OWLDataFactory factory, final ExportContext exportContext, final IRI termIRI, final Annotation annotation)
			throws URISyntaxException {
		IRIProvider iriProvider = exportContext.getIriProvider();
		OWLDataProperty owlDataProperty = factory.getOWLDataProperty(iriProvider.getIRI(annotation.getAnnotationType()));
		OWLNamedIndividual owlNamedIndividual = factory.getOWLNamedIndividual(termIRI);
		OWLLiteral owlLiteral = factory.getOWLLiteral(annotation.getAnnotation());
		OWLDataPropertyAssertionAxiom owlDataPropertyAssertionAxiom = factory.getOWLDataPropertyAssertionAxiom(owlDataProperty, owlNamedIndividual, owlLiteral);
		exportContext.addAxiom(owlDataPropertyAssertionAxiom);
	}

	private boolean isOWLAnnotation(final Annotation annotation) {
		return annotation.getAnnotationType().getType().equals(PropertyType.ANNOTATION);
	}

	private void exportOWLAnnotation(final OWLDataFactory factory, final ExportContext exportContext, final IRI termIRI, final Annotation annotation) {
		String annotationValue = Strings.nullToEmpty(annotation.getAnnotation());
		String definitionUrl = annotation.getAnnotationType().getDefinitionUrl();
		OWLAnnotationValue owlAnnotationValue;
		if (UrlValidator.validate(annotationValue)) {
			owlAnnotationValue = IRI.create(annotationValue);
		} else if (CharMatcher.INVISIBLE.matchesAnyOf(annotationValue)) {
			owlAnnotationValue = factory.getOWLLiteral(annotationValue, "en");
		} else {
			owlAnnotationValue = factory.getOWLLiteral(annotationValue, OWL2Datatype.RDF_PLAIN_LITERAL);
		}
		if (!Strings.isNullOrEmpty(annotationValue)) {
			OWLAnnotationProperty property = factory.getOWLAnnotationProperty(IRI.create(definitionUrl));
			OWLAnnotationAssertionAxiom axiom = factory.getOWLAnnotationAssertionAxiom(property, termIRI, owlAnnotationValue);
			exportContext.addAxiom(axiom);
		} else {
			logger.log(Level.SEVERE, "Annotation is null or empty {0}, {1}", new String[] { termIRI.toString(), definitionUrl });
		}
	}

	private void exportRelationships(final IRIProvider iriProvider, final OWLDataFactory factory,
			final ExportContext exportContext, final Term term, final OWLClass termClass,
			final Set<OWLClassExpression> intersectClasses, final Set<OWLClassExpression> unionClasses)
			throws URISyntaxException {
		for (Relationship relationship : term.getRelationships()) {
			if (relationship.getStatus().equals(APPROVED)) {
				RelationshipType type = relationship.getType();
				Term relatedTerm = relationship.getRelatedTerm();
				IRI relatedTermIRI = iriProvider.getIRI(relatedTerm);
				OWLClass relatedTermClass = factory.getOWLClass(relatedTermIRI);
				if (relationship.isIntersection()) {
					OWLClassExpression intersect = relatedTermClass;
					if (!type.getRelationship().equals(TAG_IS_A.getTag())) {
						OWLObjectProperty objectProp = getOwlObjectProperty(factory, type);
						intersect = factory.getOWLObjectSomeValuesFrom(objectProp, relatedTermClass);
					}
					intersectClasses.add(intersect);
				} else if (type.getRelationship().equals(TAG_IS_A.getTag())) {
					OWLAxiom axiom = factory.getOWLSubClassOfAxiom(termClass, relatedTermClass);
					exportContext.addAxiom(axiom);
				} else if (type.getRelationship().equals("union_of")) {
					unionClasses.add(relatedTermClass);
				} else if (type.getRelationship().equals("disjoint_from")) {
					OWLAxiom axiom = factory.getOWLDisjointClassesAxiom(termClass, relatedTermClass);
					exportContext.addAxiom(axiom);
				} else if (term.getType().equals(TermType.CLASS) && relatedTerm.getType().equals(TermType.CLASS)) {
					OWLObjectProperty objectProp = getOwlObjectProperty(factory, type);
					OWLObjectSomeValuesFrom someValuesFrom = factory.getOWLObjectSomeValuesFrom(objectProp,
							relatedTermClass);
					OWLAxiom axiom = factory.getOWLSubClassOfAxiom(termClass, someValuesFrom);
					exportContext.addAxiom(axiom);
				}
			}
		}
	}

	private void exportRelationshipsIndividuals(final IRIProvider iriProvider, final OWLDataFactory factory,
			final ExportContext exportContext, final Term term, final OWLNamedIndividual termClass,
			final Set<OWLIndividual> sameIndividuals, final Set<OWLIndividual> differentIndividuals)
			throws URISyntaxException {
		for (Relationship relationship : term.getRelationships()) {
			if (relationship.getStatus().equals(APPROVED) && term.getType().equals(TermType.INDIVIDUAL)) {
				// class assertion
				RelationshipType type = relationship.getType();
				Term relatedTerm = relationship.getRelatedTerm();
				IRI relatedTermIRI = iriProvider.getIRI(relatedTerm);
				OWLClass relatedTermClass = factory.getOWLClass(relatedTermIRI);
				exportContext.addRelationshipType(type);
				if (type.getRelationship().equals(TAG_IS_A.getTag())) {
					OWLAxiom axiom = factory.getOWLClassAssertionAxiom(relatedTermClass, termClass);
					exportContext.addAxiom(axiom);
				} else if (TermType.INDIVIDUAL.equals(relatedTerm.getType())) {
					OWLObjectProperty objectProp = getOwlObjectProperty(factory, type);
					OWLNamedIndividual relatedIndividual = factory.getOWLNamedIndividual(relatedTermIRI);
					OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(objectProp, termClass,
							relatedIndividual);
					exportContext.addAxiom(axiom);
				}
			}
		}
	}

	private void exportRelationshipTypes(final OWLDataFactory factory, final ExportContext exportContext) {
		for (RelationshipType type : exportContext.getRelationshipTypes()) {
			if (!isBuiltIn(type) && type.getStatus().equals(Status.APPROVED)) {
				OWLObjectProperty objectProp = getOwlObjectProperty(factory, type);

				exportContext.addAxiom(factory.getOWLDeclarationAxiom(objectProp));
				OWLAnnotationAssertionAxiom label = factory.getOWLAnnotationAssertionAxiom(factory.getRDFSLabel(), objectProp.getIRI(),
						factory.getOWLLiteral(type.getName()));
				exportContext.addAxiom(label);
				OWLAnnotationAssertionAxiom comment = factory.getOWLAnnotationAssertionAxiom(factory.getRDFSComment(), objectProp.getIRI(),
						factory.getOWLLiteral(type.getDefintion()));
				exportContext.addAxiom(comment);

				exportRelationshipTypeFeatures(factory, exportContext, type, objectProp);
			}
		}
	}

	private void exportRelationshipTypeFeatures(final OWLDataFactory factory, final ExportContext exportContext,
												final RelationshipType type, final OWLObjectProperty objectProp) {
		RelationshipType inverse = type.getInverseOf();
		RelationshipType transitiveOver = type.getTransitiveOver();
		if (inverse != null) {
			OWLObjectProperty inverseObjectProperty = getOwlObjectProperty(factory, inverse);
			exportContext.addAxiom(factory.getOWLInverseObjectPropertiesAxiom(objectProp, inverseObjectProperty));
		}

		if (transitiveOver != null) {
			OWLObjectProperty transitiveOverObjectProperty = getOwlObjectProperty(factory, transitiveOver);
			List<OWLObjectProperty> chain = asList(objectProp, transitiveOverObjectProperty);
			OWLAxiom axiom = factory.getOWLSubPropertyChainOfAxiom(chain, objectProp);
			exportContext.addAxiom(axiom);
		}

		if (type.isCyclic()) {
			logger.warning("Cyclic relationships are not supported by OWL: " + type.getRelationship());
		}

		/*
		 * if(type.isReflexive()) { manager.addAxiom(ont,
		 * factory.getOWLReflexiveObjectPropertyAxiom(objectProp)); }
		 */

		if (type.isSymmetric()) {
			exportContext.addAxiom(factory.getOWLSymmetricObjectPropertyAxiom(objectProp));
		}

		/*
		 * if(type.isAntiSymmetric()) { manager.addAxiom(ont,
		 * factory.getOWLASymmetricObjectPropertyAxiom(objectProp)); }
		 */

		if (type.isTransitive()) {
			exportContext.addAxiom(factory.getOWLTransitiveObjectPropertyAxiom(objectProp));
		}
	}

	private OWLObjectProperty getOwlObjectProperty(final OWLDataFactory factory, final RelationshipType relationshipType) {

		IRI relationshipIRI = getRelationshipIRISafe(relationshipType.getRelationship());
		if (relationshipIRI == null) {
			relationshipIRI = IRI.create(relationshipType.getUrl());
		}
		return factory.getOWLObjectProperty(relationshipIRI);
	}
}
