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

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author Artur Polit
 * @since 08/09/2017
 */
@Converter
public class TermTypeConverter implements AttributeConverter<TermType, Character> {
	@Override
	public Character convertToDatabaseColumn(final TermType attribute) {
		switch (attribute) {
		case CLASS:
			return 'C';
		case INDIVIDUAL:
			return 'I';
		default:
			throw new IllegalArgumentException("Unknown" + attribute);
		}
	}

	@Override
	public TermType convertToEntityAttribute(final Character dbData) {
		switch (dbData) {
		case 'C':
			return TermType.CLASS;
		case 'I':
			return TermType.INDIVIDUAL;
		default:
			throw new IllegalArgumentException("Unknown" + dbData);
		}
	}
}

