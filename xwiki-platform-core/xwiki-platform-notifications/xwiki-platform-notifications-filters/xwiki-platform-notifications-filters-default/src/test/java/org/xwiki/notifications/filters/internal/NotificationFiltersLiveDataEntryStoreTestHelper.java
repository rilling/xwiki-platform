package org.xwiki.notifications.filters.internal;

import java.util.Map;

import org.xwiki.livedata.LiveData;
import org.xwiki.livedata.LiveDataException;
import org.xwiki.livedata.LiveDataQuery;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;

import com.xpn.xwiki.XWikiContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Helper methods shared by LiveData entry store tests.
 */
public final class NotificationFiltersLiveDataEntryStoreTestHelper
{
    private NotificationFiltersLiveDataEntryStoreTestHelper()
    {
    }

    @FunctionalInterface
    public interface ThrowingLiveDataGetter
    {
        LiveData get(LiveDataQuery query) throws LiveDataException;
    }

    public static LiveDataQuery mockQuery(Map<String, Object> parameters)
    {
        LiveDataQuery query = mock(LiveDataQuery.class);
        LiveDataQuery.Source source = mock(LiveDataQuery.Source.class);
        when(query.getSource()).thenReturn(source);
        when(source.getParameters()).thenReturn(parameters);
        return query;
    }

    public static void assertMissingTarget(ThrowingLiveDataGetter getter)
    {
        LiveDataQuery query = mockQuery(Map.of());
        LiveDataException exception =
            assertThrows(LiveDataException.class, () -> getter.get(query));
        assertEquals("The target source parameter is mandatory.", exception.getMessage());
    }

    public static void assertBadAuthorization(ThrowingLiveDataGetter getter,
        ContextualAuthorizationManager contextualAuthorizationManager, XWikiContext context)
        throws LiveDataException
    {
        DocumentReference userDoc = new DocumentReference("xwiki", "XWiki", "Foo");
        LiveData emptyLiveData = new LiveData();

        LiveDataQuery query = mockQuery(Map.of(
            "target", "wiki",
            "wiki", "foo"
        ));

        when(contextualAuthorizationManager.hasAccess(Right.ADMIN)).thenReturn(false);
        when(context.getUserReference()).thenReturn(userDoc);
        LiveDataException exception =
            assertThrows(LiveDataException.class, () -> getter.get(query));
        assertEquals("You don't have rights to access those information.", exception.getMessage());

        when(contextualAuthorizationManager.hasAccess(Right.ADMIN)).thenReturn(true);
        assertEquals(emptyLiveData, getter.get(query));

        query = mockQuery(Map.of(
            "target", "user",
            "user", "xwiki:XWiki.Bar"
        ));

        when(contextualAuthorizationManager.hasAccess(Right.ADMIN)).thenReturn(false);
        exception = assertThrows(LiveDataException.class, () -> getter.get(query));
        assertEquals("You don't have rights to access those information.", exception.getMessage());

        when(contextualAuthorizationManager.hasAccess(Right.ADMIN)).thenReturn(true);
        assertEquals(emptyLiveData, getter.get(query));

        query = mockQuery(Map.of(
            "target", "user",
            "user", "xwiki:XWiki.Foo"
        ));

        when(contextualAuthorizationManager.hasAccess(Right.ADMIN)).thenReturn(false);
        assertEquals(emptyLiveData, getter.get(query));
    }
}