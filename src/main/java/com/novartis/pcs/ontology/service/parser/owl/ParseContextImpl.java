package com.novartis.pcs.ontology.service.parser.owl;

import java.util.Collection;

import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.service.parser.ParseContext;

public class ParseContextImpl implements ParseContext {

    private Collection<Term> terms;
    private Collection<Datasource> datasources;
    private Collection<RelationshipType> relationshipTypes;
    private Collection<AnnotationType> annotationTypes;


    public ParseContextImpl(Collection<Term> terms, Collection<Datasource> datasources,
							Collection<RelationshipType> relationshipTypes, final Collection<AnnotationType> annotationTypes) {
        super();
        this.terms = terms;
        this.datasources = datasources;
        this.relationshipTypes = relationshipTypes;
        this.annotationTypes = annotationTypes;
    }

    @Override
    public Collection<Term> getTerms() {
        return terms;
    }

    @Override
    public Collection<Datasource> getDatasources() {
        return datasources;
    }

    @Override
    public Collection<RelationshipType> getRelationshipTypes() {
        return relationshipTypes;
    }

    @Override
    public Collection<AnnotationType> getAnnotationTypes() {
        return annotationTypes;
    }
}
