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
package org.coode.owlapi.obo12.parser;

import java.io.Reader;
import java.util.logging.Logger;

import com.novartis.pcs.ontology.service.parser.InvalidFormatException;
import com.novartis.pcs.ontology.service.parser.obo.OBOParseContext;

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

 