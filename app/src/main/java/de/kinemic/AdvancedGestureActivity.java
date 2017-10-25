package de.kinemic;

import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;

import java.util.Arrays;
import java.util.List;

import de.kinemic.event.PublisherEvent;
import de.kinemic.event.PublisherLog;
import de.kinemic.event.zmq.PublisherListener;
import de.kinemic.event.zmq.PublisherRequester;

/**
 * This template activity uses the zmq PublisherListener to receive events.
 * Use {@link #getEventTypes()} to implement a custom event filter.
 * Override {@link #handleEvent(PublisherEvent)} to handle gesture events.
 */
public class AdvancedGestureActivity extends AppCompatActivity {
    private final PublisherListener mReceiver = new PublisherListener() {

        @Override
        protected void handleLog(PublisherLog log) {

        }

        @Override
        protected void handleEvent(PublisherEvent base) {
            try {
                AdvancedGestureActivity.this.handleEvent(base);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private final PublisherRequester mRequester = new PublisherRequester();

    /**
     * Event types to receive. Override this method to implement a custom filter.
     * @return Array of event types to receive. [""] for all
     */
    protected String[] getEventTypes() {
        return new String[] {""};
    }

    /**
     * Return the ip address as a string. (default: localhost)
     * Override this method to connect to a remote Publisher.
     * @return publisher ip as string (i.e. '127.0.0.1')
     */
    protected String getPublisherIP() {
        return "localhost";
    }

    @Override
    protected void onResume() {
        mReceiver.setEventTypes(getEventTypes());
        mReceiver.start(getPublisherIP());
        mRequester.start(getPublisherIP());
        super.onResume();
    }

    @Override
    protected void onPause() {
        mReceiver.stop();
        mRequester.stop();
        super.onPause();
    }

    protected void requestOrientationReset() {
        mRequester.requestOrientationReset();
    }

    /**
     * Called for every received event. Override this method to implement gestures.
     * @param event
     */
    protected void handleEvent(PublisherEvent event) throws JSONException {

    }
}
