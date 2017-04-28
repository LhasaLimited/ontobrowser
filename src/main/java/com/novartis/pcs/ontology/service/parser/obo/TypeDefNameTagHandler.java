package com.novartis.pcs.ontology.service.parser.obo;

import org.coode.owlapi.obo.parser.OBOVocabulary;

import com.novartis.pcs.ontology.entity.RelationshipType;

public class TypeDefNameTagHandler extends OBOTagHandler {

	public TypeDefNameTagHandler(OBOParseContext context) {
		super(OBOVocabulary.NAME, context);
	}
	@Override
	void handleTagValue(String tag, String value, String qualifierBlock, String comment) {
		RelationshipType relationshipType = context.getCurrentRelationshipType();
		relationshipType.setName(context.unescapeTagValue(value));
	}

}
