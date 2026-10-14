package log;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class LogWindowSourceTest {

    @Test
    void NonPositiveQueueLength() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new LogWindowSource(0)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new LogWindowSource(-1)
        );
    }

    @Test
    void NotExceedQueueLength() {
        LogWindowSource source = new LogWindowSource(3);

        source.append(null, "Message 1");
        source.append(null, "Message 2");
        source.append(null, "Message 3");
        source.append(null, "Message 4");

        assertEquals(3, source.size());
    }

    @Test
    void SourceContainsNoMessages() {
        LogWindowSource source = new LogWindowSource(5);

        assertEquals(0, source.size());
        assertFalse(source.all().iterator().hasNext());
    }

    @Test
    void RequestedNumberOfMessages() {
        LogWindowSource source = new LogWindowSource(10);

        source.append(null, "Message 1");
        source.append(null, "Message 2");
        source.append(null, "Message 3");

        Iterable<LogEntry> result = source.range(1, 1);

        int count = 0;
        for (LogEntry ignored : result) {
            count++;
        }

        assertEquals(1, count);
    }

    @Test
    void listenerNotifyMessageAdded() {
        LogWindowSource source = new LogWindowSource(10);
        List<Integer> notifications = new ArrayList<>();

        LogChangeListener listener = () -> notifications.add(1);
        source.registerListener(listener);

        source.append(null, "Test message");

        assertEquals(1, notifications.size());
    }

    @Test
    void unregisteredListenerNotNotified() {
        LogWindowSource source = new LogWindowSource(10);
        List<Integer> notifications = new ArrayList<>();

        LogChangeListener listener = () -> notifications.add(1);

        source.registerListener(listener);
        source.unregisterListener(listener);

        source.append(null, "Test message");

        assertEquals(0, notifications.size());
    }

    @Test
    void DuplicateNotifications() {
        LogWindowSource source = new LogWindowSource(10);
        List<Integer> notifications = new ArrayList<>();

        LogChangeListener listener = () -> notifications.add(1);

        source.registerListener(listener);
        source.registerListener(listener);

        source.append(null, "Test message");

        assertEquals(1, notifications.size());
    }
}

