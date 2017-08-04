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
package com.novartis.pcs.ontology.service.importer;

import java.io.InputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.novartis.pcs.ontology.dao.AnnotationTypeDAOLocal;
import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.service.parser.ParseContext;
import com.novartis.pcs.ontology.service.parser.owl.OWLParsingServiceLocal;

/**
 * Session Bean implementation class OntologyImportServiceImpl
 */
@Stateless(name = "owlImportService")
@Local(OntologyImportServiceLocal.class)
@Remote(OntologyImportServiceRemote.class)
public class OWLOntologyImportServiceImpl extends OntologyImportServiceBase {
    private Logger logger = Logger.getLogger(getClass().getName());

    @EJB
    private OWLParsingServiceLocal owlParsingService;

    @EJB
    private AnnotationTypeDAOLocal annotationTypeDAO;

    /**
     * Default constructor.
     */
    public OWLOntologyImportServiceImpl() {
    }

    public ParseContext parse(InputStream is, Curator curator, Version version, Ontology ontology,
            Collection<Term> terms) {

        ParseContext context = null;
        try {
            Collection<RelationshipType> relationshipTypes = relationshipTypeDAO.loadAll();
            Collection<Datasource> datasources = datasourceDAO.loadAll();
            Collection<AnnotationType> annotationTypes = annotationTypeDAO.loadAll();

            context = owlParsingService.parseOWLontology(is, relationshipTypes, datasources, curator, version,
					ontology, annotationTypes, terms);

        } catch (OWLOntologyCreationException e) {
            logger.log(Level.WARNING, "IO exception: ", e);
        }
        return context;
    }

    @Override
    protected void findRefId(Ontology ontology, Collection<Term> terms) throws InvalidEntityException {
        int nextInt = Math.abs(new java.util.Random().nextInt() % 1000);
        ontology.setReferenceIdPrefix(Integer.toString(nextInt));
        ontology.setReferenceIdValue(nextInt);

    }
}
