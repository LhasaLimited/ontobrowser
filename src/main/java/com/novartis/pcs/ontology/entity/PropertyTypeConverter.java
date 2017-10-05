/**
 * Copyright © 2017 Lhasa Limited
 * File created: 05/10/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
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
/*
 * ---------------------------------------------------------------------* This
 * software is the confidential and proprietary information of Lhasa Limited
 * Granary Wharf House, 2 Canal Wharf, Leeds, LS11 5PY --- No part of this
 * confidential information shall be disclosed and it shall be used only in
 * accordance with the terms of a written license agreement entered into by
 * holder of the information with LHASA Ltd.
 * ---------------------------------------------------------------------
 */
