/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tests.interceptors.binding.transitivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InterceptionType;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.jboss.weld.tests.interceptors.binding.transitivity.Secure.SecureLiteral;
import static org.jboss.weld.tests.interceptors.binding.transitivity.Synchronized.SynchronizedLiteral;
import static org.jboss.weld.tests.interceptors.binding.transitivity.Transactional.TransactionalLiteral;
import static org.jboss.weld.tests.interceptors.binding.transitivity.UltraSecure.UltraSecureLiteral;
import static org.jboss.weld.tests.interceptors.binding.transitivity.UltraSynchronized.UltraSynchronizedLiteral;
import static org.jboss.weld.tests.interceptors.binding.transitivity.UltraTransactional.UltraTransactionalLiteral;

/**
 * Tests interceptor binding transitivity for both normal and extension-provided interceptor bindings.
 * 
 * @author <a href="http://community.jboss.org/people/jharting">Jozef Hartinger</a>
 * 
 */
@RunWith(Arquillian.class)
@Ignore("WELD-999")
public class InterceptorBindingTransitivityTest {

    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(BeanArchive.class)
                .intercept(SecureInterceptor.class, TransactionalInterceptor.class, UltraSecureInterceptor.class,
                        UltraTransactionalInterceptor.class, SynchronizedInterceptor.class, UltraSynchronizedInterceptor.class)
                .addPackage(InterceptorBindingTransitivityTest.class.getPackage())
                .addAsServiceProvider(Extension.class, InterceptorBindingExtension.class);
    }

    @Test
    public void testInterceptorBindingRecognized() {
        assertTrue(manager.isInterceptorBinding(Secure.class));
        assertTrue(manager.isInterceptorBinding(UltraSecure.class));
        assertTrue(manager.isInterceptorBinding(Transactional.class));
        assertTrue(manager.isInterceptorBinding(UltraTransactional.class));
        assertTrue(manager.isInterceptorBinding(Synchronized.class));
        assertTrue(manager.isInterceptorBinding(UltraSynchronized.class));
    }

    @Test
    public void testTransitivityOfInterceptorBindings() {
        // non-transitive bindings
        assertEquals(1, manager.resolveInterceptors(InterceptionType.AROUND_INVOKE, new SynchronizedLiteral()).size());
        assertEquals(1, manager.resolveInterceptors(InterceptionType.AROUND_INVOKE, new SecureLiteral()).size());
        assertEquals(1, manager.resolveInterceptors(InterceptionType.AROUND_INVOKE, new TransactionalLiteral()).size());
        // transitive bindings
        assertEquals(2, manager.resolveInterceptors(InterceptionType.AROUND_INVOKE, new UltraSynchronizedLiteral()).size());
        // should resolve UltraSecureInterceptor and transitively also SecureInterceptor
        assertEquals(2, manager.resolveInterceptors(InterceptionType.AROUND_INVOKE, new UltraSecureLiteral()).size());
        // should resolve UltraTransactionalInterceptor and transitively also TransactionalInterceptor
        assertEquals(2, manager.resolveInterceptors(InterceptionType.AROUND_INVOKE, new UltraTransactionalLiteral()).size());
    }

}
