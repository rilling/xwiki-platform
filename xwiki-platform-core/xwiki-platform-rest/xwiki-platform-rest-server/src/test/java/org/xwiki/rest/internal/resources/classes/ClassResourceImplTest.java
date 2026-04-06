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
package org.xwiki.rest.internal.resources.classes;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rest.XWikiRestException;
import org.xwiki.rest.model.jaxb.Class;
import org.xwiki.security.authorization.AccessDeniedException;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

/**
 * Unit test for {@link ClassResourceImpl}
 *
 * @version $Id$
 * @since 10.11.10
 * @since 11.3.4
 * @since 11.8RC1
 */
@ComponentTest
class ClassResourceImplTest extends AbstractClassesResourceImplTest
{
    @InjectMockComponents
    private ClassResourceImpl resource;

    @Override
    protected Object getResource()
    {
        return resource;
    }

    @Test
    void authorizedClassesOnly() throws XWikiRestException, AccessDeniedException
    {
        DocumentReference protectedReference = new DocumentReference("xwiki", "XWiki", "Protected");
        doThrow(new AccessDeniedException(xcontext.getUserReference(), protectedReference)).when(
            authorization).checkAccess(eq(Right.VIEW), eq(protectedReference));

        String protectedClass = "XWiki.Protected";
        for (String availableClass : availableClasses) {
            try {
                Class aClass = resource.getClass("xwiki", availableClass);
                if (availableClass.equals(protectedClass)) {
                    fail();
                } else {
                    assertEquals(availableClass, aClass.getId());
                }
            } catch (WebApplicationException e) {
                if (availableClass.equals(protectedClass)) {
                    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), e.getResponse().getStatus());
                } else {
                    throw e;
                }
            }
        }
    }
}
