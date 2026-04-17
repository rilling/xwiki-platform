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
package org.xwiki.notifications.filters.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.notifications.NotificationConfiguration;
import org.xwiki.notifications.filters.NotificationFilter;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LegacyDefaultNotificationFilterManager}.
 *
 * @version $Id$
 * @since 15.5RC1
 */
@ComponentTest
public class LegacyDefaultNotificationFilterManagerTest extends AbstractNotificationFilterManagerTest
{
    @InjectMockComponents
    private LegacyDefaultNotificationFilterManager filterManager;

    @MockComponent
    @Named("cached")
    private FilterPreferencesModelBridge filterPreferencesModelBridge;

    @MockComponent
    private NotificationConfiguration configuration;

    @Override
    protected DefaultNotificationFilterManager getFilterManager()
    {
        return this.filterManager;
    }

    @BeforeEach
    void setUp() throws Exception
    {
        this.testUser = new DocumentReference("wiki", "test", "user");

        // Set a default comportment for the wikiDescriptorManager (inherited from Abstract class)
        when(this.wikiDescriptorManager.getMainWikiId()).thenReturn("wiki");
        when(this.wikiDescriptorManager.getCurrentWikiId()).thenReturn("currentWikiId");
        when(this.wikiDescriptorManager.getAllIds()).