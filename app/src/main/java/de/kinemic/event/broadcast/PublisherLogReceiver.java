package de.kinemic.event.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;

import de.kinemic.event.PublisherLog;

public abstract class PublisherLogReceiver extends BroadcastReceiver {

    public static final String ACTION = PublisherLog.ACTION_LOG;

    @Override
    public final void onReceive(Context context, Intent intent) {
        if (PublisherLog.ACTION_LOG.equals(intent.getAction())) {
            final String json = intent.getStringExtra(PublisherLog.BROADCAST_JSON);
            try {
                handleLog(PublisherLog.fromJson(json));
            } catch (JSONException e) {
                // this should not happen, since we send the event
            }
        }
    }

    protected abstract void handleLog(PublisherLog event);
}
