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

package org.xwiki.notifications.filters.internal.livedata.custom;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.livedata.LiveDataException;
import org.xwiki.livedata.LiveDataQuery;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.notifications.filters.NotificationFilterPreference;
import org.xwiki.notifications.filters.NotificationFilterType;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.query.internal.DefaultQueryParameter;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link NotificationCustomFiltersQueryHelper}.
 *
 * @version $Id$
 * @since 16.3.0RC1
 */
@ComponentTest
class NotificationCustomFiltersQueryHelperTest
{
    @InjectMockComponents
    private NotificationCustomFiltersQueryHelper queryHelper;

    @MockComponent
    private QueryManager queryManager;

    private LiveDataQuery createFilterQuery(Long offset, int limit)
    {
        LiveDataQuery ldQuery = mock(LiveDataQuery.class);
        when(ldQuery.getOffset()).thenReturn(offset);
        when(ldQuery.getLimit()).thenReturn(limit);
        return ldQuery;
    }

    private LiveDataQuery.Constraint createConstraint(String name, String operator, String value)
    {
        LiveDataQuery.Constraint constraint = mock(LiveDataQuery.Constraint.class, name);
        when(constraint.getOperator()).thenReturn(operator);
        when(constraint.getValue()).thenReturn(value);
        return constraint;
    }

    private LiveDataQuery.Filter createFilter(String name, String property, List<LiveDataQuery.Constraint> constraints)
    {
        LiveDataQuery.Filter filter = mock(LiveDataQuery.Filter.class, name);
        when(filter.getProperty()).thenReturn(property);
        when(filter.getConstraints()).thenReturn(constraints);
        return filter;
    }

    private LiveDataQuery.Filter createLocationFilter()
    {
        LiveDataQuery.Filter filter = createFilter("filter1", "location", List.of(
            createConstraint("filter1Constraint1", "startsWith", "foo"),
            createConstraint("filter1Constraint2", "contains", "bar"),
            createConstraint("filter1Constraint3", "equals", "buz")
        ));
        when(filter.isMatchAll()).thenReturn(false);
        return filter;
    }

    private List<LiveDataQuery.Filter> createDefaultFilters()
    {
        return List.of(
            createLocationFilter(),
            createFilter("filter2", "eventTypes", List.of(
                createConstraint("filter2Constraint", "equals", "__ALL_EVENTS__")
            )),
            createFilter("filter3", "isEnabled", List.of(
                createConstraint("filter3Constraint", "equals", "true")
            )),
            createFilter("filter4", "scope", List.of(
                createConstraint("filter4Constraint", "equals", "PAGE")
            )),
            createFilter("filter5", "filterType", List.of(
                createConstraint("filter5Constraint", "equals", "INCLUSIVE")
            ))
        );
    }

    private DefaultQueryParameter startsWithParam(String value)
    {
        DefaultQueryParameter parameter = new DefaultQueryParameter(null);
        parameter.literal(value).anyChars();
        return parameter;
    }

    private DefaultQueryParameter containsParam(String value)
    {
        DefaultQueryParameter parameter = new DefaultQueryParameter(null);
        parameter.anyChars().literal(value).anyChars();
        return parameter;
    }

    private DefaultQueryParameter equalsParam(String value)
    {
        DefaultQueryParameter parameter = new DefaultQueryParameter(null);
        parameter.literal(value);
        return parameter;
    }

    private void mockCommonBindings(Query query, String owner, DefaultQueryParameter queryParameter1,
        DefaultQueryParameter queryParameter2, DefaultQueryParameter queryParameter3, String wikiName)
        throws QueryException
    {
        when(query.bindValue("owner", owner)).thenReturn(query);
        when(query.bindValue("constraint_0", queryParameter1)).thenReturn(query);
        when(query.bindValue("constraint_1", queryParameter2)).thenReturn(query);
        when(query.bindValue("constraint_2", queryParameter3)).thenReturn(query);
        when(query.bindValue("filterType", NotificationFilterType.INCLUSIVE)).thenReturn(query);
        when(query.setWiki(wikiName)).thenReturn(query);
    }

    private void mockSortEntries(LiveDataQuery ldQuery)
    {
        LiveDataQuery.SortEntry sortEntry1 = mock(LiveDataQuery.SortEntry.class, "sortEntry1");
        when(sortEntry1.isDescending()).thenReturn(false);
        when(sortEntry1.getProperty()).thenReturn("scope");

        LiveDataQuery.SortEntry sortEntry2 = mock(LiveDataQuery.SortEntry.class, "sortEntry2");
        when(sortEntry2.isDescending()).thenReturn(true);
        when(sortEntry2.getProperty()).thenReturn("isEnabled");

        LiveDataQuery.SortEntry sortEntry3 = mock(LiveDataQuery.SortEntry.class, "sortEntry3");
        when(sortEntry3.isDescending()).thenReturn(true);
        when(sortEntry3.getProperty()).thenReturn("notificationFormats");

        when(ldQuery.getSort()).thenReturn(List.of(sortEntry1, sortEntry2, sortEntry3));
    }

    @Test
    void getFilterPreferencesNoFilterNoSort() throws QueryException, LiveDataException
    {
        String owner = "xwiki:XWiki.Foo";
        String wikiName = "foo";
        WikiReference wikiReference = new WikiReference(wikiName);
        String queryString = "select nfp from DefaultNotificationFilterPreference nfp where owner = :owner";
        Query query = mock(Query.class);
        when(this.queryManager.createQuery(queryString, Query.HQL)).thenReturn(query);
        when(query.bindValue("owner", owner)).thenReturn(query);
        when(query.setWiki(wikiName)).thenReturn(query);
        Long offset = 3L;
        int limit = 12;

        LiveDataQuery ldQuery = mock(LiveDataQuery.class);
        when(ldQuery.getOffset()).thenReturn(offset);
        when(ldQuery.getLimit()).thenReturn(limit);
        when(query.setOffset(offset.intValue())).thenReturn(query);
        when(query.setLimit(limit)).thenReturn(query);

        List<Object> expectedList = List.of(
            mock(NotificationFilterPreference.class, "filterPref1"),
            mock(NotificationFilterPreference.class, "filterPref2"),
            mock(NotificationFilterPreference.class, "filterPref3")
        );
        when(query.execute()).thenReturn(expectedList);
        assertEquals(expectedList, this.queryHelper.getFilterPreferences(ldQuery, owner, wikiReference));
        verify(query).bindValue("owner", owner);
        verify(query).setWiki(wikiName);
        verify(query).setOffset(offset.intValue());
        verify(query).setLimit(limit);
    }

    @Test
    void countFilterPreferencesNoFilterNoSort() throws QueryException, LiveDataException
    {
        String owner = "xwiki:XWiki.Foo";
        String wikiName = "foo";
        WikiReference wikiReference = new WikiReference(wikiName);
        String queryString = "select count(nfp.id) from DefaultNotificationFilterPreference nfp where owner = :owner";
        Query query = mock(Query.class);
        when(this.queryManager.createQuery(queryString, Query.HQL)).thenReturn(query);
        when(query.bindValue("owner", owner)).thenReturn(query);
        when(query.setWiki(wikiName)).thenReturn(query);
        Long offset = 3L;
        int limit = 12;

        LiveDataQuery ldQuery = mock(LiveDataQuery.class);
        when(ldQuery.getOffset()).thenReturn(offset);
        when(ldQuery.getLimit()).thenReturn(limit);

        when(query.execute()).thenReturn(List.of(3L));
        assertEquals(3L, this.queryHelper.countTotalFilters(ldQuery, owner, wikiReference));
        verify(query).bindValue("owner", owner);
        verify(query).setWiki(wikiName);
        verify(query, never()).setOffset(anyInt());
        verify(query, never()).setLimit(anyInt());
    }

    @Test
    void getFilterPreferencesNoFilterSort() throws QueryException, LiveDataException
    {
        String owner = "xwiki:XWiki.Foo";
        String wikiName = "foo";
        WikiReference wikiReference = new WikiReference(wikiName);
        Long offset = 3L;
        int limit = 12;

        LiveDataQuery ldQuery = mock(LiveDataQuery.class);
        when(ldQuery.getOffset()).thenReturn(offset);
        when(ldQuery.getLimit()).thenReturn(limit);

        LiveDataQuery.SortEntry sortEntry1 = mock(LiveDataQuery.SortEntry.class, "sortEntry1");
        when(sortEntry1.isDescending()).thenReturn(false);
        when(sortEntry1.getProperty()).thenReturn("scope");

        LiveDataQuery.SortEntry sortEntry2 = mock(LiveDataQuery.SortEntry.class, "sortEntry2");
        when(sortEntry2.isDescending()).thenReturn(true);
        when(sortEntry2.getProperty()).thenReturn("isEnabled");

        LiveDataQuery.SortEntry sortEntry3 = mock(LiveDataQuery.SortEntry.class, "sortEntry3");
        when(sortEntry3.isDescending()).thenReturn(true);
        when(sortEntry3.getProperty()).thenReturn("notificationFormats");

        when(ldQuery.getSort()).thenReturn(List.of(sortEntry1, sortEntry2, sortEntry3));

        String queryString = "select nfp from DefaultNotificationFilterPreference nfp where owner = :owner "
            + "order by nfp.pageOnly asc, nfp.page asc, nfp.wiki asc, nfp.user asc, nfp.enabled desc, "
            + "nfp.emailEnabled asc, nfp.alertEnabled desc";
        Query query = mock(Query.class);
        when(this.queryManager.createQuery(queryString, Query.HQL)).thenReturn(query);
        when(query.bindValue("owner", owner)).thenReturn(query);
        when(query.setWiki(wikiName)).thenReturn(query);
        when(query.setOffset(offset.intValue())).thenReturn(query);
        when(query.setLimit(limit)).thenReturn(query);

        List<Object> expectedList = List.of(
            mock(NotificationFilterPreference.class, "filterPref1"),
            mock(NotificationFilterPreference.class, "filterPref2"),
            mock(NotificationFilterPreference.class, "filterPref3")
        );
        when(query.execute()).thenReturn(expectedList);
        assertEquals(expectedList, this.queryHelper.getFilterPreferences(ldQuery, owner, wikiReference));
        verify(query).bindValue("owner", owner);
        verify(query).setWiki(wikiName);
        verify(query).setOffset(offset.intValue());
        verify(query).setLimit(limit);
    }

    @Test
    void countFilterPreferencesNoFilterSort() throws QueryException, LiveDataException
    {
        String owner = "xwiki:XWiki.Foo";
        String wikiName = "foo";
        WikiReference wikiReference = new WikiReference(wikiName);

        Long offset = 3L;
        int limit = 12;

        LiveDataQuery ldQuery = mock(LiveDataQuery.class);
        when(ldQuery.getOffset()).thenReturn(offset);
        when(ldQuery.getLimit()).thenReturn(limit);

        LiveDataQuery.SortEntry sortEntry1 = mock(LiveDataQuery.SortEntry.class, "sortEntry1");
        when(sortEntry1.isDescending()).thenReturn(false);
        when(sortEntry1.getProperty()).thenReturn("scope");

        LiveDataQuery.SortEntry sortEntry2 = mock(LiveDataQuery.SortEntry.class, "sortEntry2");
        when(sortEntry2.isDescending()).thenReturn(true);
        when(sortEntry2.getProperty()).thenReturn("isEnabled");

        LiveDataQuery.SortEntry sortEntry3 = mock(LiveDataQuery.SortEntry.class, "sortEntry3");
        when(sortEntry3.isDescending()).thenReturn(true);
        when(sortEntry3.getProperty()).thenReturn("notificationFormats");

        when(ldQuery.getSort()).thenReturn(List.of(sortEntry1, sortEntry2, sortEntry3));

        String queryString = "select count(nfp.id) from DefaultNotificationFilterPreference nfp where owner = :owner";
        Query query = mock(Query.class);
        when(this.queryManager.createQuery(queryString, Query.HQL)).thenReturn(query);
        when(query.bindValue("owner", owner)).thenReturn(query);
        when(query.setWiki(wikiName)).thenReturn(query);

        when(query.execute()).thenReturn(List.of(3L));
        assertEquals(3L, this.queryHelper.countTotalFilters(ldQuery, owner, wikiReference));
        verify(query).bindValue("owner", owner);
        verify(query).setWiki(wikiName);
        verify(query, never()).setOffset(anyInt());
        verify(query, never()).setLimit(anyInt());
    }

    @Test
    void getFilterPreferencesFilterNoSort() throws QueryException, LiveDataException
    {
        String owner = "xwiki:XWiki.Foo";
        String wikiName = "foo";
        Long offset = 3L;
        int limit = 12;
        WikiReference wikiReference = new WikiReference(wikiName);

        LiveDataQuery ldQuery = createFilterQuery(offset, limit);
        when(ldQuery.getFilters()).thenReturn(createDefaultFilters());

        String queryString = "select nfp from DefaultNotificationFilterPreference nfp where owner = :owner "
            + "and ((nfp.pageOnly like :constraint_0 or nfp.page like :constraint_0 or nfp.wiki like :constraint_0 or "
            + "nfp.user like :constraint_0) or (nfp.pageOnly like :constraint_1 or nfp.page like :constraint_1 or "
            + "nfp.wiki like :constraint_1 or nfp.user like :constraint_1) or (nfp.pageOnly = :constraint_2 or "
            + "nfp.page = :constraint_2 or nfp.wiki = :constraint_2 or nfp.user = :constraint_2)) and "
            + "length(nfp.allEventTypes) = 0 and nfp.enabled = true and length(nfp.pageOnly) > 0 and "
            + "nfp.filterType = :filterType";

        Query query = mock(Query.class);
        when(this.queryManager.createQuery(queryString, Query.HQL)).thenReturn(query);

        DefaultQueryParameter queryParameter1 = startsWithParam("foo");
        DefaultQueryParameter queryParameter2 = containsParam("bar");
        DefaultQueryParameter queryParameter3 = equalsParam("buz");

        mockCommonBindings(query, owner, queryParameter1, queryParameter2, queryParameter3, wikiName);
        when(query.setOffset(offset.intValue())).thenReturn(query);
        when(query.setLimit(limit)).thenReturn(query);

        List<Object> expectedList = List.of(
            mock(NotificationFilterPreference.class, "filterPref1"),
            mock(NotificationFilterPreference.class, "filterPref2"),
            mock(NotificationFilterPreference.class, "filterPref3")
        );
        when(query.execute()).thenReturn(expectedList);
        assertEquals(expectedList, this.queryHelper.getFilterPreferences(ldQuery, owner, wikiReference));
        verify(query).bindValue("owner", owner);
        verify(query).bindValue("constraint_0", queryParameter1);
        verify(query).bindValue("constraint_1", queryParameter2);
        verify(query).bindValue("constraint_2", queryParameter3);
        verify(query).bindValue("filterType", NotificationFilterType.INCLUSIVE);
        verify(query).setWiki(wikiName);
        verify(query).setOffset(offset.intValue());
        verify(query).setLimit(limit);
    }

    @Test
    void countFilterPreferencesFilterNoSort() throws QueryException, LiveDataException
    {
        String owner = "xwiki:XWiki.Foo";
        String wikiName = "foo";
        Long offset = 3L;
        int limit = 12;
        WikiReference wikiReference = new WikiReference(wikiName);

        LiveDataQuery ldQuery = createFilterQuery(offset, limit);
        when(ldQuery.getFilters()).thenReturn(createDefaultFilters());

        String queryString = "select count(nfp.id) from DefaultNotificationFilterPreference nfp where owner = :owner "
            + "and ((nfp.pageOnly like :constraint_0 or nfp.page like :constraint_0 or nfp.wiki like :constraint_0 or "
            + "nfp.user like :constraint_0) or (nfp.pageOnly like :constraint_1 or nfp.page like :constraint_1 or "
            + "nfp.wiki like :constraint_1 or nfp.user like :constraint_1) or (nfp.pageOnly = :constraint_2 or "
            + "nfp.page = :constraint_2 or nfp.wiki = :constraint_2 or nfp.user = :constraint_2)) and "
            + "length(nfp.allEventTypes) = 0 and nfp.enabled = true and length(nfp.pageOnly) > 0 and "
            + "nfp.filterType = :filterType";

        Query query = mock(Query.class);
        when(this.queryManager.createQuery(queryString, Query.HQL)).thenReturn(query);

        DefaultQueryParameter queryParameter1 = startsWithParam("foo");
        DefaultQueryParameter queryParameter2 = containsParam("bar");
        DefaultQueryParameter queryParameter3 = equalsParam("buz");

        mockCommonBindings(query, owner, queryParameter1, queryParameter2, queryParameter3, wikiName);

        when(query.execute()).thenReturn(List.of(3L));
        assertEquals(3L, this.queryHelper.countTotalFilters(ldQuery, owner, wikiReference));
        verify(query).bindValue("owner", owner);
        verify(query).bindValue("constraint_0", queryParameter1);
        verify(query).bindValue("constraint_1", queryParameter2);
        verify(query).bindValue("constraint_2", queryParameter3);
        verify(query).bindValue("filterType", NotificationFilterType.INCLUSIVE);
        verify(query).setWiki(wikiName);
        verify(query, never()).setOffset(anyInt());
        verify(query, never()).setLimit(anyInt());
    }

    @Test
    void getFilterPreferencesFilterSort() throws QueryException, LiveDataException
    {
        String owner = "xwiki:XWiki.Foo";
        String wikiName = "foo";
        Long offset = 3L;
        int limit = 12;
        WikiReference wikiReference = new WikiReference(wikiName);

        LiveDataQuery ldQuery = createFilterQuery(offset, limit);
        when(ldQuery.getFilters()).thenReturn(createDefaultFilters());
        mockSortEntries(ldQuery);

        String queryString = "select nfp from DefaultNotificationFilterPreference nfp where owner = :owner "
            + "and ((nfp.pageOnly like :constraint_0 or nfp.page like :constraint_0 or nfp.wiki like :constraint_0 or "
            + "nfp.user like :constraint_0) or (nfp.pageOnly like :constraint_1 or nfp.page like :constraint_1 or "
            + "nfp.wiki like :constraint_1 or nfp.user like :constraint_1) or (nfp.pageOnly = :constraint_2 or "
            + "nfp.page = :constraint_2 or nfp.wiki = :constraint_2 or nfp.user = :constraint_2)) and "
            + "length(nfp.allEventTypes) = 0 and nfp.enabled = true and length(nfp.pageOnly) > 0 and "
            + "nfp.filterType = :filterType "
            + "order by nfp.pageOnly asc, nfp.page asc, nfp.wiki asc, nfp.user asc, nfp.enabled desc, "
            + "nfp.emailEnabled asc, nfp.alertEnabled desc";

        Query query = mock(Query.class);
        when(this.queryManager.createQuery(queryString, Query.HQL)).thenReturn(query);

        DefaultQueryParameter queryParameter1 = startsWithParam("foo");
        DefaultQueryParameter queryParameter2 = containsParam("bar");
        DefaultQueryParameter queryParameter3 = equalsParam("buz");

        mockCommonBindings(query, owner, queryParameter1, queryParameter2, queryParameter3, wikiName);
        when(query.setOffset(offset.intValue())).thenReturn(query);
        when(query.setLimit(limit)).thenReturn(query);

        List<Object> expectedList = List.of(
            mock(NotificationFilterPreference.class, "filterPref1"),
            mock(NotificationFilterPreference.class, "filterPref2"),
            mock(NotificationFilterPreference.class, "filterPref3")
        );
        when(query.execute()).thenReturn(expectedList);
        assertEquals(expectedList, this.queryHelper.getFilterPreferences(ldQuery, owner, wikiReference));
        verify(query).bindValue("owner", owner);
        verify(query).bindValue("constraint_0", queryParameter1);
        verify(query).bindValue("constraint_1", queryParameter2);
        verify(query).bindValue("constraint_2", queryParameter3);
        verify(query).bindValue("filterType", NotificationFilterType.INCLUSIVE);
        verify(query).setWiki(wikiName);
        verify(query).setOffset(offset.intValue());
        verify(query).setLimit(limit);
    }

    @Test
    void countFilterPreferencesFilterSort() throws QueryException, LiveDataException
    {
        String owner = "xwiki:XWiki.Foo";
        String wikiName = "foo";
        Long offset = 3L;
        int limit = 12;
        WikiReference wikiReference = new WikiReference(wikiName);

        LiveDataQuery ldQuery = createFilterQuery(offset, limit);
        when(ldQuery.getFilters()).thenReturn(createDefaultFilters());
        mockSortEntries(ldQuery);

        String queryString = "select count(nfp.id) from DefaultNotificationFilterPreference nfp where owner = :owner "
            + "and ((nfp.pageOnly like :constraint_0 or nfp.page like :constraint_0 or nfp.wiki like :constraint_0 or "
            + "nfp.user like :constraint_0) or (nfp.pageOnly like :constraint_1 or nfp.page like :constraint_1 or "
            + "nfp.wiki like :constraint_1 or nfp.user like :constraint_1) or (nfp.pageOnly = :constraint_2 or "
            + "nfp.page = :constraint_2 or nfp.wiki = :constraint_2 or nfp.user = :constraint_2)) and "
            + "length(nfp.allEventTypes) = 0 and nfp.enabled = true and length(nfp.pageOnly) > 0 and "
            + "nfp.filterType = :filterType";

        Query query = mock(Query.class);
        when(this.queryManager.createQuery(queryString, Query.HQL)).thenReturn(query);

        DefaultQueryParameter queryParameter1 = startsWithParam("foo");
        DefaultQueryParameter queryParameter2 = containsParam("bar");
        DefaultQueryParameter queryParameter3 = equalsParam("buz");

        mockCommonBindings(query, owner, queryParameter1, queryParameter2, queryParameter3, wikiName);

        when(query.execute()).thenReturn(List.of(3L));
        assertEquals(3L, this.queryHelper.countTotalFilters(ldQuery, owner, wikiReference));
        verify(query).bindValue("owner", owner);
        verify(query).bindValue("constraint_0", queryParameter1);
        verify(query).bindValue("constraint_1", queryParameter2);
        verify(query).bindValue("constraint_2", queryParameter3);
        verify(query).bindValue("filterType", NotificationFilterType.INCLUSIVE);
        verify(query).setWiki(wikiName);
        verify(query, never()).setOffset(anyInt());
        verify(query, never()).setLimit(anyInt());
    }
}