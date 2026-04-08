package org.xwiki.notifications.filters.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.ModelContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.notifications.filters.NotificationFilter;
import org.xwiki.notifications.preferences.NotificationPreference;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Base class for Notification Filter Manager tests to avoid duplication
 * between standard and legacy implementations.
 */
public abstract class AbstractNotificationFilterManagerTest {
    // These must be protected so the child classes (Default and Legacy) can access them
    @MockComponent
    protected ComponentManager componentManager;

    @MockComponent
    protected WikiDescriptorManager wikiDescriptorManager;

    @MockComponent
    protected DocumentReference testUser;

    @MockComponent
    protected ModelContext modelContext;

    /**
     * Helper to get the manager instance from the child class.
     * This allows the abstract tests to run against different implementations.
     */
    protected abstract DefaultNotificationFilterManager getFilterManager();

    @Test
    void getAllFiltersWithMainWiki() throws Exception {
        NotificationFilter fakeFilter1 = mock(NotificationFilter.class);

        when(this.componentManager.getInstanceMap(NotificationFilter.class))
                .thenReturn(Collections.singletonMap("1", fakeFilter1));

        Collection<NotificationFilter> filters = this.getFilterManager().getAllFilters(testUser, false);

        assertEquals(1, filters.size());
        assertTrue(filters.contains(fakeFilter1));
    }

    @Test
    void getAllFiltersForWiki() throws Exception {
        NotificationFilter fakeFilter1 = mock(NotificationFilter.class);
        NotificationFilter fakeFilter2 = mock(NotificationFilter.class);

        WikiReference currentWiki = new WikiReference("current");
        when(this.wikiDescriptorManager.getCurrentWikiReference()).thenReturn(currentWiki);

        WikiReference argumentWiki = new WikiReference("foo");

        when(this.componentManager.getInstanceList(NotificationFilter.class))
                .thenReturn(Arrays.asList(fakeFilter1, fakeFilter2));

        Collection<NotificationFilter> filters = this.getFilterManager().getAllFilters(argumentWiki);

        assertEquals(2, filters.size());
        assertTrue(filters.contains(fakeFilter1));
        verify(this.modelContext).setCurrentEntityReference(argumentWiki);
        verify(this.modelContext).setCurrentEntityReference(currentWiki);
    }

    @Test
    void getFiltersWithMatchingFilters() throws Exception {
        NotificationFilter fakeFilter1 = mock(NotificationFilter.class);
        NotificationPreference preference = mock(NotificationPreference.class);

        when(fakeFilter1.matchesPreference(preference)).thenReturn(true);

        Collection<NotificationFilter> filters = this.getFilterManager().getFiltersRelatedToNotificationPreference(
                Arrays.asList(fakeFilter1), preference).collect(java.util.stream.Collectors.toList());

        assertEquals(1, filters.size());
        assertTrue(filters.contains(fakeFilter1));
    }

    @Test
    void getFiltersWithOneBadFilter() throws Exception {
        NotificationFilter fakeFilter1 = mock(NotificationFilter.class);
        NotificationPreference preference = mock(NotificationPreference.class);

        when(fakeFilter1.matchesPreference(preference)).thenReturn(false);

        Collection<NotificationFilter> filters = (Collection<NotificationFilter>) this.getFilterManager();
    }
}