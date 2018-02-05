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

