public final class NotificationTestEventFactory
{
    private NotificationTestEventFactory()
    {
    }
     private Event createMockedEvent()
    {
        Event event = mock(Event.class);
        when(event.getDate()).thenReturn(new Date(1L));
        return event;
    }

    public static Event createMockedEvent(String type, DocumentReference user, DocumentReference doc, Date date,
        String groupId)
    {
        Event event = mock(Event.class);
        when(event.getDate()).thenReturn(date);
        when(event.getDocument()).thenReturn(doc);
        when(event.getUser()).thenReturn(user);
        when(event.getType()).thenReturn(type);
        when(event.getGroupId()).thenReturn(groupId);

        when(event.toString()).thenReturn(String.format("[%s] Event [%s] on document [%s] by [%s] on [%s]",
            groupId, type, doc, user, date.toString()));
        return event;
    }
}
