package de.kinemic;

import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;

import de.kinemic.event.PublisherEvent;
import de.kinemic.event.broadcast.PublisherEventReceiver;

/**
 * This template activity uses the broadcast Publisher listener to receive events.
 * Override {@link #handleEvent(PublisherEvent)} to handle gesture events.
 */
public class GestureActivity extends AppCompatActivity {
    private final PublisherEventReceiver mReceiver = new PublisherEventReceiver() {
        @Override
        protected void handleEvent(PublisherEvent event) {
            GestureActivity.this.handleEvent(event);
        }
    };

    @Override
    protected void onResume() {
        registerReceiver(mReceiver, new IntentFilter(PublisherEventReceiver.ACTION));
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    /**
     * Called for every received event. Override this method to implement gestures.
     * @param event
     */
    protected void handleEvent(PublisherEvent event) {

    }
}
