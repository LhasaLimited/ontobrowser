/**
 * Copyright © 2017 Lhasa Limited
 * File created: 12/07/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.dao;

import java.util.Collection;

import com.novartis.pcs.ontology.entity.AnnotationType;
import com.novartis.pcs.ontology.entity.Ontology;

/**
 * @author Artur Polit
 * @since 12/07/2017
 */
public interface AnnotationTypeDAOLocal extends VersionedDAO<AnnotationType> {
	Collection<AnnotationType> loadByOntology(Ontology ontology);

	AnnotationType loadByAnnotation(String propertyFragment);
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
