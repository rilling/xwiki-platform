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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.rest.XWikiRestException;
import org.xwiki.rest.model.jaxb.Class;
import org.xwiki.rest.model.jaxb.Classes;
import org.xwiki.security.SecurityConfiguration;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import org.xwiki.model.reference.DocumentReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ClassesResourceImpl}
 *
 * @version $Id$
 * @since 10.11.10
 * @since 11.3.4
 * @since 11.8RC1
 */
@ComponentTest
class ClassesResourceImplTest extends AbstractClassesResourceImplTest
{
    @InjectMockComponents
    private ClassesResourceImpl resource;

    @MockComponent
    private SecurityConfiguration securityConfiguration;

    @Override
    protected Object getResource()
    {
        return resource;
    }

    @BeforeEach
    @Override
    void configure() throws Exception
    {
        super.configure();
        when(this.securityConfiguration.getQueryItemsLimit()).thenReturn(1000);
    }

    @Test
    void classesNoConstraint() throws XWikiRestException
    {
        Classes xwikiClasses = resource.getClasses("xwiki", 0, 100);
        assertEquals(4, xwikiClasses.getClazzs().size());
    }

    @Test
    void authorizedClassesOnly() throws XWikiRestException
    {
        when(authorization.hasAccess(eq(Right.VIEW), eq(new DocumentReference("xwiki", "XWiki", "Protected"))))
            .thenReturn(false);

        Classes xwikiClasses = resource.getClasses("xwiki", 0, 100);
        assertEquals(3, xwikiClasses.getClazzs().size());

        for (Class clazz : xwikiClasses.getClazzs()) {
            assertTrue(clazz.getId().equals("Foo.Class1")
                || clazz.getId().equals("XWiki.User")
                || clazz.getId().equals("Bar.Other"));
            assertNotEquals("XWiki.Protected", clazz.getId());
        }
    }
}
