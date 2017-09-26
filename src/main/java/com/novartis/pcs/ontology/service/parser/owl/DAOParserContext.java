/**
 * Copyright © 2017 Lhasa Limited
 * File created: 15/09/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.parser.owl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.novartis.pcs.ontology.dao.AnnotationTypeDAOLocal;
import com.novartis.pcs.ontology.dao.DatasourceDAOLocal;
import com.novartis.pcs.ontology.dao.RelationshipTypeDAOLocal;
import com.novartis.pcs.ontology.dao.TermDAOLocal;
import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Curator;
import com.novartis.pcs.ontology.entity.Datasource;
import com.novartis.pcs.ontology.entity.InvalidEntityException;
import com.novartis.pcs.ontology.entity.Ontology;
import com.novartis.pcs.ontology.entity.RelationshipType;
import com.novartis.pcs.ontology.entity.Term;
import com.novartis.pcs.ontology.entity.Version;

/**
 * @author Artur Polit
 * @since 15/09/2017
 */
public class DAOParserContext extends OWLParserContext {

	private TermDAOLocal termDAO;
	private RelationshipTypeDAOLocal relationshipTypeDAO;
	private DatasourceDAOLocal datasourceDAO;
	private AnnotationTypeDAOLocal annotationTypeDAO;

	public DAOParserContext(final Curator curator, final Ontology ontology, final Version version,
			final TermDAOLocal termDAO, final RelationshipTypeDAOLocal relationshipTypeDAO,
			final DatasourceDAOLocal datasourceDAO, final AnnotationTypeDAOLocal annotationTypeDAO) {
		super(curator, ontology, version);
		this.termDAO = termDAO;
		this.relationshipTypeDAO = relationshipTypeDAO;
		this.datasourceDAO = datasourceDAO;
		this.annotationTypeDAO = annotationTypeDAO;
	}

	@Override
	public void setDatasources(final Map<String, Datasource> datasources) {
		// managed by DAO
	}

	@Override
	public Datasource getDatasource(final String acronym) {
		return datasourceDAO.loadByAcronym(acronym);
	}

	@Override
	public Collection<Datasource> getDatasources() {
		return Collections.emptyList(); // saved in JPA context
	}

	@Override
	public boolean hasRelationshipType(final String relationshipType) {
		return relationshipTypeDAO.loadByRelationship(relationshipType) != null;
	}

	@Override
	public void addRelationshipType(final RelationshipType relationshipType) throws InvalidEntityException {
		relationshipTypeDAO.save(relationshipType);
	}

	@Override
	public RelationshipType getRelationshipType(final String relationshipType) {
		return relationshipTypeDAO.loadByRelationship(relationshipType);
	}

	@Override
	public Collection<RelationshipType> getRelationshipTypes() {
		return Collections.emptyList(); // saved in JPA context
	}

	@Override
	public AnnotationType getAnnotationType(final String propertyFragment) {
		return annotationTypeDAO.loadByAnnotation(propertyFragment);
	}

	@Override
	public boolean hasAnnotationType(final String propertyFragment) {
		return annotationTypeDAO.loadByAnnotation(propertyFragment) != null;
	}

	@Override
	public Collection<AnnotationType> getAnnotationTypes() {
		return Collections.emptyList(); // saved in JPA context
	}

	@Override
	public Map<String, Term> getTerms() {
		return Collections.emptyMap();
	}

	@Override
	public Term getTerm(final String referenceId) {
		return termDAO.loadByReferenceId(referenceId, ontology.getName());
	}

	@Override
	public void putTerm(final String referenceId, final Term term) throws InvalidEntityException {
		termDAO.save(term);
	}

	@Override
	public boolean hasTerm(final String referenceId) {
		return termDAO.loadByReferenceIdSafe(referenceId, ontology.getName()) != null;
	}

	@Override
	public void addTerms(final Collection<Term> existingTerms) {

	}

	@Override
	public void putAnnotationType(final String annotationTypeFragment, final AnnotationType annotationType)
			throws InvalidEntityException {
		annotationTypeDAO.save(annotationType);
	}

}
/*
 * ---------------------------------------------------------------------* This
 * software is the confidential and proprietary information of Lhasa Limited
 * Granary Wharf House, 2 Canal Wharf, Leeds, LS11 5PY --- No part of this
 * confidential information shall be disclosed and it shall be used only in
 * accordance with the terms of a written license agreement entered into by
 * holder of the information with LHASA Ltd.
 * ---------------------------------------------------------------------
 */
