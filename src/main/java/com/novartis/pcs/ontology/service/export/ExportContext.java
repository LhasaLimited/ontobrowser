/*

Copyright 2017 Lhasa Limited

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

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.novartis.pcs.ontology.entity.RelationshipType;

/**
 * @author Artur Polit
 * @since 16/08/2017
 */
class ExportContext {
	private final OWLOntologyManager manager;
	private final OWLOntology ontology;
	private final OWLOntologyFormat format;
	private final IRIProvider iriProvider;
	private final Set<RelationshipType> relationshipTypes = new HashSet<>();

	ExportContext(final OWLOntologyManager manager, final OWLOntology ontology, final OWLOntologyFormat format, final IRIProvider iriProvider) {
		this.manager = manager;
		this.ontology = ontology;
		this.format = format;
		this.iriProvider = iriProvider;
	}

	public void addRelationshipType(RelationshipType relationshipType) {
		relationshipTypes.add(relationshipType);
	}

	public OWLOntology getOntology() {
		return ontology;
	}

	public IRIProvider getIriProvider() {
		return iriProvider;
	}

	public Set<RelationshipType> getRelationshipTypes() {
		return relationshipTypes;
	}

	public OWLOntologyFormat getFormat() {
		return format;
	}

	public void addAxiom(OWLAxiom owlAxiom) {
		manager.addAxiom(getOntology(), owlAxiom);
	}

}

 