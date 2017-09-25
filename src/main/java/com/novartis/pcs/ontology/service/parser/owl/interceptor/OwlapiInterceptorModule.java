/**
 * Copyright © 2017 Lhasa Limited
 * File created: 19/09/2017 by Artur Polit
 * Creator : Artur Polit
 * Version : $$Id$$
 */
package com.novartis.pcs.ontology.service.parser.owl.interceptor;

import java.lang.reflect.Method;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;

/**
 * @author Artur Polit
 * @since 19/09/2017
 */
public class OwlapiInterceptorModule implements Module {
	@Override
	public void configure(final Binder binder) {
		OwlapiLoaderInterceptor loaderInterceptor = new OwlapiLoaderInterceptor();
		binder.bindInterceptor(Matchers.any(), new AbstractMatcher<Method>() {
			@Override
			public boolean matches(final Method method) {
				return method.getName().equals("loadOntology");
			}
		}, loaderInterceptor);
		binder.bind(OwlapiLoaderInterceptor.class).toInstance(loaderInterceptor);
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
