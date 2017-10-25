package de.kinemic.event.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;

import de.kinemic.event.PublisherEvent;

public abstract class PublisherEventReceiver extends BroadcastReceiver {

    public static final String ACTION = PublisherEvent.ACTION_EVENT;

    @Override
    public final void onReceive(Context context, Intent intent) {
        if (PublisherEvent.ACTION_EVENT.equals(intent.getAction())) {
            final String json = intent.getStringExtra(PublisherEvent.BROADCAST_JSON);
            try {
                handleEvent(PublisherEvent.fromJson(json));
            } catch (JSONException e) {
                // this should not happen, since we send the event
            }
        }
    }

    protected abstract void handleEvent(PublisherEvent event);
}
