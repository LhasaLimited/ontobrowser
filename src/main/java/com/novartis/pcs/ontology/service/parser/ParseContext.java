package com.novartis.pcs.ontology.service.parser;

import java.util.Collection;

import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;

public interface ParseContext {

    Collection<Term> getTerms();

    Collection<Datasource> getDatasources();

    Collection<RelationshipType> getRelationshipTypes();

	Collection<AnnotationType> getAnnotationTypes();
}
