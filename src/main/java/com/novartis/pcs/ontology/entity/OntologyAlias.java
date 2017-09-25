/**
 * Copyright © 2017 Lhasa Limited
 * File created: 18/09/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
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
/*
 * ---------------------------------------------------------------------* This
 * software is the confidential and proprietary information of Lhasa Limited
 * Granary Wharf House, 2 Canal Wharf, Leeds, LS11 5PY --- No part of this
 * confidential information shall be disclosed and it shall be used only in
 * accordance with the terms of a written license agreement entered into by
 * holder of the information with LHASA Ltd.
 * ---------------------------------------------------------------------
 */
