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

