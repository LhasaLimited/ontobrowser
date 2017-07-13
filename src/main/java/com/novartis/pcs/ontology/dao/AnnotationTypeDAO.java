/**
 * Copyright © 2017 Lhasa Limited
 * File created: 11/07/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.dao;

import com.novartis.pcs.ontology.entity.AnnotationType;

import javax.ejb.Local;
import javax.ejb.Stateless;

/**
 * @author Artur Polit
 * @since 11/07/2017
 */
@Stateless
@Local(AnnotationTypeDAOLocal.class)
public class AnnotationTypeDAO extends VersionedEntityDAO<AnnotationType> implements ReadOnlyDAO<AnnotationType> {

	public AnnotationTypeDAO() {
		super();
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
 