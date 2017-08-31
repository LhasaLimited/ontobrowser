/**
 * Copyright © 2017 Lhasa Limited
 * File created: 31/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.webapp.client;

/**
 * @author Artur Polit
 * @since 31/08/2017
 */
public class PathExtractor {
	private final String historyToken;
	private final char separator;
	private String ontologyName;
	private String referenceId;

	public PathExtractor(final String historyToken, final char separator) {
		this.historyToken = historyToken;
		this.separator = separator;
	}

	public String getOntologyName() {
		return ontologyName;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public PathExtractor invoke() {
		int semicolonIndex = historyToken.indexOf(separator);
		referenceId = null;
		if (semicolonIndex > -1) {
			ontologyName = historyToken.substring(historyToken.indexOf('=') + 1, semicolonIndex);
			referenceId = historyToken.substring(historyToken.lastIndexOf('=') + 1);
		} else {
			ontologyName = historyToken.substring(historyToken.indexOf('=') + 1);
		}
		return this;
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
