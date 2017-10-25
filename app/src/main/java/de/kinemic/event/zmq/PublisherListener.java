package de.kinemic.event.zmq;

import android.util.Log;

import org.json.JSONException;

import de.kinemic.event.PublisherEvent;
import de.kinemic.event.PublisherLog;

/**
 * This class can be used to receive Publisher events over the network (also locally).
 * It wraps the zmq json receiver and delivers java object events.
 */
public abstract class PublisherListener extends PublisherJsonListener {
    private static final String TAG = PublisherListener.class.getSimpleName();

    protected abstract void handleLog(PublisherLog log);
    protected abstract void handleEvent(PublisherEvent base);

    @Override
    protected void handleLog(String level, String json) {
        try {
            handleLog(PublisherLog.fromJson(json));
        } catch (JSONException e) {
            Log.w(TAG, "Could not parse json event: " + json, e);
        }
    }

    @Override
    protected void handleEvent(String type, String json) {
        try {
            handleEvent(PublisherEvent.fromJson(json));
        } catch (JSONException e) {
            Log.w(TAG, "Could not parse json event: " + json, e);
        }
    }
}
