package com.novartis.pcs.ontology.service.parser.owl;

import java.io.InputStream;
import java.util.Collection;

import com.novartis.pcs.ontology.entity.AnnotationType;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.service.parser.ParseContext;

public interface OWLParsingService {

    ParseContext parseOWLontology(InputStream inputStream, Collection<RelationshipType> relationshipTypes,
								  Collection<Datasource> datasources, Curator curator, Version version, Ontology ontology, final Collection<AnnotationType> annotationTypes) throws OWLOntologyCreationException;

}