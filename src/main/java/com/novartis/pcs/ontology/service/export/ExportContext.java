/**
 * Copyright © 2017 Lhasa Limited
 * File created: 16/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.export;

import com.novartis.pcs.ontology.entity.RelationshipType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Artur Polit
 * @since 16/08/2017
 */
class ExportContext {
	private final OWLOntologyManager manager;
	private final OWLOntology ontology;
	private final Set<RelationshipType> relationshipTypes = new HashSet<>();

	ExportContext(final OWLOntologyManager manager, final OWLDataFactory factory, final OWLOntology ontology) {
		this.manager = manager;
		this.ontology = ontology;
	}

	public void addRelationshipType(RelationshipType relationshipType) {
		relationshipTypes.add(relationshipType);
	}

	public OWLOntology getOntology() {
		return ontology;
	}

	public Set<RelationshipType> getRelationshipTypes() {
		return relationshipTypes;
	}

	public void addAxiom(OWLAxiom owlAxiom) {
		manager.addAxiom(getOntology(), owlAxiom);
	}

}
/* ---------------------------------------------------------------------*
 * This software is the confidential and proprietary
 * information of Lhasa Limited
 * Granary Wharf House, 2 Canal Wharf, Leeds, LS11 5PY
 * ---
 * No part of this confidential information shall be disclosed
 * and it shall be used only in accordance with the terms of a
 * written license agreement entered into by holder of the information
 * with LHASA Ltd.
 * ---------------------------------------------------------------------*/
 