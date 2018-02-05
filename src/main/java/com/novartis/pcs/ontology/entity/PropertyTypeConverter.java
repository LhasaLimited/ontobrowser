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
 * @since 05/10/2017
 */
@Converter
public class PropertyTypeConverter implements AttributeConverter<PropertyType, Character> {
	@Override
	public Character convertToDatabaseColumn(final PropertyType attribute) {
		switch (attribute) {
		case ANNOTATION:
			return 'A';
		case DATA_PROPERTY:
			return 'D';
		default:
			throw new IllegalArgumentException("Unknown" + attribute);
		}
	}

	@Override
	public PropertyType convertToEntityAttribute(final Character dbData) {
		switch (dbData) {
		case 'A':
			return PropertyType.ANNOTATION;
		case 'D':
			return PropertyType.DATA_PROPERTY;
		default:
			throw new IllegalArgumentException("Unknown" + dbData);
		}
	}
}

