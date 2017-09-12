/**
 * Copyright © 2017 Lhasa Limited
 * File created: 08/09/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
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
/*
 * ---------------------------------------------------------------------* This
 * software is the confidential and proprietary information of Lhasa Limited
 * Granary Wharf House, 2 Canal Wharf, Leeds, LS11 5PY --- No part of this
 * confidential information shall be disclosed and it shall be used only in
 * accordance with the terms of a written license agreement entered into by
 * holder of the information with LHASA Ltd.
 * ---------------------------------------------------------------------
 */
