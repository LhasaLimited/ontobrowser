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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import com.novartis.pcs.ontology.service.parser.ParseContext;
import org.coode.owlapi.obo12.parser.ParserAdapter;

import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;
import com.novartis.pcs.ontology.service.parser.obo.OBOParseContext;

/**
 * Session Bean implementation class OntologyImportServiceImpl
 */
@Stateless(name = "oboImportService")
@Local(OntologyImportServiceLocal.class)
@Remote(OntologyImportServiceRemote.class)
public class OntologyImportServiceImpl extends OntologyImportServiceBase
{
	Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Default constructor. 
     */
    public OntologyImportServiceImpl() {
    }

	public ParseContext parse(InputStream is, Curator curator, Version version, Ontology ontology,
							  Collection<Term> terms)
	{

		OBOParseContext context = null;
		try
		{
			Collection<RelationshipType> relationshipTypes = relationshipTypeDAO.loadAll();
			Collection<Datasource> datasources = datasourceDAO.loadAll();

			Reader reader = new InputStreamReader(is, "UTF-8");
			ParserAdapter parserAdapter = new ParserAdapter();
			context = new OBOParseContext(ontology, terms, relationshipTypes, datasources, curator, version);
			parserAdapter.parse(reader, context);
		}
		catch (UnsupportedEncodingException e)
		{
			// Never happen because UTF-8 is built-in to JVM
		}
		catch (IOException e)
		{
			logger.warning("IO exception: " + e.getMessage());
		}
		return context;
	}

    @Override
    protected void findRefId(Ontology ontology, Collection<Term> terms) throws InvalidEntityException {
        String refIdPrefix = null;
        int refIdValue = 0;
        
        for(Term term: terms){
        	if ("Thing".equals(term.getReferenceId())){
        		continue;
			}
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
        }
        
        ontology.setReferenceIdPrefix(refIdPrefix);
        ontology.setReferenceIdValue(refIdValue);
    }
}
