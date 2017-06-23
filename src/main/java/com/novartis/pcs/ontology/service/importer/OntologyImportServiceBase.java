package com.novartis.pcs.ontology.service.importer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;

import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.DuplicateEntityException;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.Relationship;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.entity.VersionedEntity.Status;
import com.novartis.pcs.ontology.service.OntologyService;
import com.novartis.pcs.ontology.service.parser.ParseContext;
import com.novartis.pcs.ontology.service.search.OntologySearchServiceLocal;

public abstract class OntologyImportServiceBase extends OntologyService implements OntologyImportServiceRemote, OntologyImportServiceLocal {

    private Logger logger = Logger.getLogger(getClass().getName());

    @EJB
    private OntologySearchServiceLocal searchService;

    public OntologyImportServiceBase() {
        super();
    }

    @Override
    public void importOntology(String ontologyName, InputStream is, Curator curator)
            throws DuplicateEntityException, InvalidEntityException {
            	// Lock ontology while we are importing/updating
            	// also need to lock ontology because we potentially
            	// update the current term reference id value
            	Ontology ontology = ontologyDAO.loadByName(ontologyName, true);
            	Collection<Term> terms = Collections.emptyList();
            	Version version = lastUnpublishedVersion(curator);
            	
            	if(ontology == null) {
            		ontology = new Ontology(ontologyName, curator, version);
            		ontology.setStatus(Status.APPROVED);
            		ontology.setApprovedVersion(version);
            	} else {
            		terms = termDAO.loadAll(ontology);
            	}
            	    	
           		// According to spec OBO files are UTF-8 encoded
           	    ParseContext context = parse(is, curator, version, ontology, terms);
           		saveParsed(ontology, version, context);            	
            }

    abstract protected ParseContext parse(InputStream is, Curator curator, Version version, Ontology ontology,
            Collection<Term> terms);

    private void saveParsed(Ontology ontology, Version version, ParseContext context)
            throws InvalidEntityException, DuplicateEntityException {
                Collection<Term> terms;
                Collection<RelationshipType> relationshipTypes;
                Collection<Datasource> datasources;
                terms = context.getTerms();
                
                String refIdPrefix = null;
                int refIdValue = 0;
                Set<String> names = new HashSet<String>(terms.size());
                for(Term term : terms) {
                	String refId = term.getReferenceId();
                	int colon = refId.indexOf(':');
                	
                	if(colon == -1) {
                		throw new InvalidEntityException(term, 
                				"No reference id prefix defined for term: " + refId);
                	}
                	
                	if(refIdPrefix == null) {
                		refIdPrefix = refId.substring(0,colon);
                	} /*else if(!refIdPrefix.equals(refId.substring(0,colon))) {
                		throw new InvalidEntityException(term, 
                				"Invalid term reference id prefix: " + refId);
                	}*/
                	
                	try {
                		int value = Integer.parseInt(refId.substring(colon+1));
                		refIdValue = Math.max(refIdValue, value);
                	} catch(Exception e) {
                		throw new InvalidEntityException(term, 
                				"Invalid term reference id: " + refId, e);
                	}
                	
                	if(!names.add(term.getName().toLowerCase())) {
                		throw new DuplicateEntityException(term, "Duplicate term: " + term.getName());
                	}
                	
                	if(term.getStatus() == Status.APPROVED) {
                		boolean found = false;
                		for(Relationship relationship : term.getRelationships()) {
                			if(relationship.getStatus() == Status.APPROVED) {
                				found = true;
                				break;
                			}
                		}
                		
                		if(!found) {
                			term.setRoot(true);
                		}
                	}
                }
                
                datasources = context.getDatasources();
                for(Datasource datasource : datasources) {
                	if(datasource.getId() == 0L) {
                		datasourceDAO.save(datasource);
                	}
                }
                
                relationshipTypes = context.getRelationshipTypes();
                for(RelationshipType relationshipType : relationshipTypes) {
                	if(relationshipType.getId() == 0L) {
                		relationshipTypeDAO.save(relationshipType);
                	}
                }
                
                ontology.setReferenceIdPrefix(refIdPrefix);
                ontology.setReferenceIdValue(refIdValue);
                if(ontology.getId() == 0L) {
                	ontologyDAO.save(ontology);
                }	
                
                for(Term term : terms) {
                	if(term.getId() == 0L) {
                		termDAO.save(term);
                	} else if(term.getStatus() == Status.OBSOLETE) {
                		removePendingDependents(term, version);
                	}
                	searchService.update(term);
                }
            }

}