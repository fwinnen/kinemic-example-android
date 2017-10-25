package de.kinemic.event;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Java class which represents any kind of event from the publisher.
 * Use the {@link #asGesture()}, {@link #asActivation()}, ... methods depending on the Type
 * to get the specified event.
 */
public class PublisherEvent {
    public static final String ACTION_EVENT = "de.kinemic.publisher.ACTION.EVENT";
    public static final String BROADCAST_TYPE = "type";
    public static final String BROADCAST_JSON = "json";


    public static enum Type {
        Gesture("Gesture"), Writing("Writing"), MouseEvent("MouseEvent"), Activation("Activation"), WritingSegment("WritingSegment"), Heartbeat("Heartbeat");

        public final String jsonType;

        Type(String jsonType) {
            this.jsonType = jsonType;
        }
    }

    private static final Map<String, Type> sTypeMap;
    static {
        Map<String, Type> aMap = new HashMap<>();
        aMap.put("Gesture", Type.Gesture);
        aMap.put("Writing", Type.Writing);
        aMap.put("MouseEvent", Type.MouseEvent);
        aMap.put("MouseToggle", Type.MouseEvent);
        aMap.put("Activation", Type.Activation);
        aMap.put("WritingSegment", Type.WritingSegment);
        aMap.put("Heartbeat", Type.Heartbeat);
        sTypeMap = Collections.unmodifiableMap(aMap);
    }

    public final Type type;
    public final JSONObject parameters;

    PublisherEvent(Type type, JSONObject parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public static @NonNull
    PublisherEvent fromJson(@NonNull JSONObject json) throws JSONException {
        final String type_s = json.getString("type");
        final JSONObject params = json.getJSONObject("parameters");

        final Type type = sTypeMap.get(type_s);
        if (type != null) return new PublisherEvent(type, params);
        throw new JSONException("Unexpected type: " + type_s);
    }

    public static @NonNull
    PublisherEvent fromJson(@NonNull String json) throws JSONException {
        return fromJson(new JSONObject(json.replace("null", "{}")));
    }

    public JSONObject toJson() throws JSONException {
        return new JSONObject()
                .put("type", type.jsonType)
                .put("parameters", parameters);
    }

    public Gesture asGesture() throws JSONException {
        return Gesture.from(this);
    }

    public Writing asWriting() throws JSONException {
        return Writing.from(this);
    }

    public WritingSegment asWritingSegment() throws JSONException {
        return WritingSegment.from(this);
    }

    public Activation asActivation() throws JSONException {
        return Activation.from(this);
    }

    public Heartbeat asHeartbeat() throws JSONException {
        return Heartbeat.from(this);
    }

    public MouseEvent asMouseEvent() throws JSONException {
        return MouseEvent.from(this);
    }

    public static class Gesture {
        public final String name;

        private Gesture(String name) {
            this.name = name;
        }

        static Gesture from(PublisherEvent base) throws JSONException {
            return new Gesture(base.parameters.getString("name"));
        }
    }

    public static class Writing {
        public final String vocabulary;
        public final String hypothesis;
        public final boolean isFinal;

        private Writing(String vocabulary, String hypothesis, boolean isFinal) {
            this.vocabulary = vocabulary;
            this.hypothesis = hypothesis;
            this.isFinal = isFinal;
        }

        static Writing from(PublisherEvent base) throws JSONException {
            return new Writing(
                    base.parameters.getString("vocabulary"),
                    base.parameters.getString("hypothesis"),
                    base.parameters.getBoolean("final"));
        }
    }

    public static class WritingSegment {
        public final boolean started;

        private WritingSegment(boolean started) {
            this.started = started;
        }

        static WritingSegment from(PublisherEvent base) throws JSONException {
            return new WritingSegment(
                    base.parameters.getBoolean("started"));
        }
    }

    public static class Activation {
        public final boolean active;

        private Activation(boolean active) {
            this.active = active;
        }

        static Activation from(PublisherEvent base) throws JSONException {
            return new Activation(base.parameters.getBoolean("active"));
        }
    }

    public static class Heartbeat {
        public final boolean active;
        public final int flags;
        public final String stream;
        public final String sensor;
        public final long last;

        private Heartbeat(boolean active, int flags, String stream, String sensor, long last) {
            this.active = active;
            this.flags = flags;
            this.stream = stream;
            this.sensor = sensor;
            this.last = last;
        }

        static Heartbeat from(PublisherEvent base) throws JSONException {
            return new Heartbeat(
                    base.parameters.getBoolean("active"),
                    base.parameters.getInt("flags"),
                    base.parameters.getString("stream"),
                    base.parameters.getString("sensor"),
                    base.parameters.getLong("last"));
        }
    }

    public static class MouseEvent {
        public enum Type { Toggle, Move }
        public final Type type;
        public final double dx, dy;
        public final boolean palmVertical;

        public MouseEvent(Type type, double dx, double dy, boolean palmVertical) {
            this.type = type;
            this.dx = dx;
            this.dy = dy;
            this.palmVertical = palmVertical;
        }

        static MouseEvent from(PublisherEvent base) throws JSONException {
            if (!base.parameters.has("type")) return new MouseEvent(Type.Toggle, 0.0, 0.0, false);

            String type_s = base.parameters.getString("type");
            if ("move".equals(type_s)) {
                return new MouseEvent(Type.Move, base.parameters.getDouble("dx"), base.parameters.getDouble("dy"), base.parameters.getBoolean("down"));
            } else if ("toggle".equals(type_s)) {
                return new MouseEvent(Type.Toggle, 0.0, 0.0, false);
            } else {
                throw new JSONException("Invalid MouseEvent type: " + type_s);
            }
        }
    }
}
