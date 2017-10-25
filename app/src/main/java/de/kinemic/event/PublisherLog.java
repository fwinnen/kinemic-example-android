package de.kinemic.event;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Java class which represents a log message received from the publisher.
 */
public class PublisherLog {
    public static final String ACTION_LOG = "de.kinemic.publisher.ACTION.LOG";
    public static final String BROADCAST_LEVEL = "level";
    public static final String BROADCAST_JSON = "json";

    public final String who;
    public final String level;
    public final String message;
    public final long timestamp;

    private PublisherLog(String who, String level, String message, long timestamp) {
        this.who = who;
        this.level = level;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static PublisherLog fromJson(JSONObject json) throws JSONException {
        JSONObject parameters = json.getJSONObject("parameters");
        return new PublisherLog(parameters.getString("who"), parameters.getString("level"), parameters.getString("message"), parameters.getLong("timestamp"));
    }

    public static PublisherLog fromJson(String json) throws JSONException {
        return fromJson(new JSONObject(json));
    }
}
