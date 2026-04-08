/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rest.internal.resources;

import org.xwiki.component.manager.ComponentManager;
import org.xwiki.security.SecurityConfiguration;
import org.xwiki.test.mockito.MockitoComponentManager;

import com.xpn.xwiki.web.Utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Utility methods for resource tests.
 *
 * @version $Id$
 */
public final class ResourceTestUtils
{
    private ResourceTestUtils()
    {
    }

    /**
     * Initialize the test component manager and common mocks used by REST resource tests.
     *
     * @param componentManager the injected test component manager
     * @param contextComponentManager the context component manager mock
     * @param securityConfiguration the security configuration mock
     * @throws Exception if one of the manager lookups fails
     */
    public static void setUpResourceTest(MockitoComponentManager componentManager, ComponentManager contextComponentManager,
        SecurityConfiguration securityConfiguration) throws Exception
    {
        Utils.setComponentManager(componentManager);

        // Because XWikiResource injects the context component manager, it exists as a mock, and we thus need to mock
        // its behavior - otherwise it would just be ignored.
        when(contextComponentManager.getInstance(any()))
            .thenAnswer(invocation -> componentManager.getInstance(invocation.getArgument(0)));
        when(contextComponentManager.getInstance(any(), any()))
            .thenAnswer(invocation -> componentManager.getInstance(invocation.getArgument(0), invocation.getArgument(1)));

        when(securityConfiguration.getQueryItemsLimit()).thenReturn(1000);
    }
}
