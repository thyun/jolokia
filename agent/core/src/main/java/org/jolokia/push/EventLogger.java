package org.jolokia.push;

import java.util.Map;

/**
 * EventLogger
 */
public interface EventLogger {
	public void logEvent(String event);
    public void logEvent(String event, Map<String, Object> producerConfig);
    public void shutdown();
}
