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

package com.novartis.pcs.ontology.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Artur Polit
 * @since 18/09/2017
 */
@Entity
@Table(name = "ONTOLOGY_ALIAS")
public class OntologyAlias extends AbstractEntity {

	private static final long serialVersionUID = 121231293897343423L;

	@ManyToOne
	@JoinColumn(name = "ONTOLOGY_ID")
	private Ontology ontology;

	@Column(name = "ALIAS_URI")
	private String aliasUrl;

	public OntologyAlias() {
	}

	public OntologyAlias(final Ontology ontology, final String aliasUrl) {
		this.ontology = ontology;
		this.aliasUrl = aliasUrl;
	}

	public Ontology getOntology() {
		return ontology;
	}

	public void setOntology(final Ontology ontology) {
		this.ontology = ontology;
	}

	public String getAliasUrl() {
		return aliasUrl;
	}

	public void setAliasUrl(final String aliasUrl) {
		this.aliasUrl = aliasUrl;
	}

}

