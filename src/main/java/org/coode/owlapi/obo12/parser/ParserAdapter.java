/**
 * Copyright Â© 2017 Lhasa Limited
 * File created: 13/06/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package org.coode.owlapi.obo12.parser;

import com.novartis.pcs.ontology.service.parser.InvalidFormatException;
import com.novartis.pcs.ontology.service.parser.obo.OBOParseContext;

import java.io.Reader;
import java.util.logging.Logger;

/**
 * @author Artur Polit
 * @since 13/06/2017
 */
public class ParserAdapter {

	private Logger logger = Logger.getLogger(getClass().getName());

	public void parse(Reader reader, OBOParseContext context)
	{
		try
		{
			OBOParser parser = new OBOParser(reader);
			logger.info(parser.getClass().getClassLoader().toString());
			parser.setHandler(context);
			parser.parse();
		}
		catch (ParseException e)
		{
			logger.warning("Parse exception occuring during OBO file import: " + e.getMessage());
			throw new InvalidFormatException(e.getMessage());
		}
	}
}
/* ---------------------------------------------------------------------*
 * This software is the confidential and proprietary
 * information of Lhasa Limited
 * Granary Wharf House, 2 Canal Wharf, Leeds, LS11 5PY
 * ---
 * No part of this confidential information shall be disclosed
 * and it shall be used only in accordance with the terms of a
 * written license agreement entered into by holder of the information
 * with LHASA Ltd.
 * ---------------------------------------------------------------------*/
 