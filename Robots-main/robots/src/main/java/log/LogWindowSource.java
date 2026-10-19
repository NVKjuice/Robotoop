package log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * Что починить:
 * 1. Этот класс порождает утечку ресурсов (связанные слушатели оказываются
 * удерживаемыми в памяти)
 * 2. Этот класс хранит активные сообщения лога, но в такой реализации он 
 * их лишь накапливает. Надо же, чтобы количество сообщений в логе было ограничено 
 * величиной m_iQueueLength (т.е. реально нужна очередь сообщений 
 * ограниченного размера) 
 */
public class LogWindowSource
{
    private int iQueueLength;
    private final Queue<LogEntry> messages;
    private final List<LogChangeListener> listeners = new ArrayList<>();
    
    public LogWindowSource(int iQueueLength) 
    {
        if(iQueueLength <= 0) {
            throw new IllegalArgumentException(
                "Длина очереди должна быть положительна");
        }
        this.iQueueLength = iQueueLength;
        this.messages = new ArrayDeque<>(iQueueLength);
    }
    
    public synchronized void registerListener(LogChangeListener listener)
    {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public synchronized void unregisterListener(LogChangeListener listener) {
        listeners.remove(listener);
    }
    
     public void append(LogLevel logLevel, String message) {
        LogChangeListener[] activeListeners;

        synchronized (this) {
            if (messages.size() == iQueueLength) {
                messages.remove();
            }

            messages.add(new LogEntry(logLevel, message));
            activeListeners = listeners.toArray(new LogChangeListener[0]);
        }

        for (LogChangeListener listener : activeListeners) {
            listener.onLogChanged();
        }
    }
    
    public synchronized int size() {
        return messages.size();
    }

    public synchronized Iterable<LogEntry> range(int startFrom, int count) {
        if (startFrom < 0 || count <= 0 || startFrom >= messages.size()) {
            return Collections.emptyList();
        }

        List<LogEntry> snapshot = new ArrayList<>(messages);
        int end = (int) Math.min(
            (long) startFrom + count, snapshot.size());

        return new ArrayList<>(snapshot.subList(startFrom, end));
    }

    public synchronized Iterable<LogEntry> all() {
        return new ArrayList<>(messages);
    }
}
