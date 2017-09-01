/**
 * Copyright © 2017 Lhasa Limited
 * File created: 31/08/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.webapp.client;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Artur Polit
 * @since 31/08/2017
 */
public class PathExtractor implements OntoParams {
	private final String historyToken;
	private final String separator;
	private String ontologyName;
	private String referenceId;
	private boolean deep;

	public PathExtractor(final String historyToken, final String separator) {
		this.historyToken = historyToken;
		this.separator = separator;
	}

	@Override
	public String getOntologyName() {
		return ontologyName;
	}

	@Override
	public String getReferenceId() {
		return referenceId;
	}

	@Override
	public boolean isDeep() {
		return deep;
	}

	public OntoParams invoke() {

		String[] rawParams = historyToken.split(separator);
		Map<String, String> params = Stream.of(rawParams)
				.collect(Collectors.toMap(s -> s.substring(0, s.indexOf('=')), s -> s.substring(s.indexOf('=') + 1)));
		ontologyName = params.get("ontology");
		referenceId = params.get("term");
		deep = Boolean.valueOf(params.getOrDefault("deep", "false"));
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
