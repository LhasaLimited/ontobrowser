package com.novartis.pcs.ontology.service.parser.owl;

import java.io.InputStream;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.service.parser.ParseContext;

public interface OWLParsingService {

    ParseContext parseOWLontology(InputStream inputStream,
								  Ontology ontology, final OWLParserContext context)
			throws OWLOntologyCreationException;

}