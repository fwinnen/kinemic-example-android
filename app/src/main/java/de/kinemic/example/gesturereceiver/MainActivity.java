package de.kinemic.example.gesturereceiver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView mSensorInfo;
    private TextView mStreamInfo;
    private TextView mLastInfo;
    private TextView mActiveInfo;
    private TextView mLastEventInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorInfo = (TextView) findViewById(R.id.meta_sensor_info);
        mStreamInfo = (TextView) findViewById(R.id.meta_stream_info);
        mLastInfo = (TextView) findViewById(R.id.meta_last_info);
        mActiveInfo = (TextView) findViewById(R.id.meta_active_info);
        mLastEventInfo = (TextView) findViewById(R.id.event_info);

        mReceiver.start();
    }

    @Override
    protected void onDestroy() {
        mReceiver.interrupt();
        super.onDestroy();
    }

    protected void updateMetaData(final String stream, final String sensor, int flags, final long last, final boolean active) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSensorInfo.setText(sensor);
                mStreamInfo.setText(stream);
                mLastInfo.setText("" + last);
                mActiveInfo.setText("" + active);
            }
        });
    }

    EventReceiver mReceiver = new EventReceiver() {

        @Override
        protected void handleEvent(final String json) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Date now = new Date();
                    try {
                        JSONObject event = new JSONObject(json);
                        JSONObject params = event.getJSONObject("parameters");
                        String type = event.getString("type");

                        if ("Heartbeat".equals(type)) {
                            boolean active = params.getBoolean("active");
                            int flags = params.getInt("flags");
                            long last = params.getLong("last");
                            String sensor = params.getString("sensor");
                            String stream = params.getString("stream");

                            Log.d("Events", "Heartbeat [active: " + active + ", last: " + last + ", stream: " + stream + ", sensor: " + sensor + "]");
                            updateMetaData(stream, sensor, flags, last, active);
                        } else if ("Gesture".equals(type)) {
                            String gesture = params.getString("name");
                            Log.d("Events", "Gesture: " + gesture);

                            mLastEventInfo.setText(now.toString() + " Gesture: " + gesture);
                        } else if ("Activation".equals(type)) {
                            boolean active = params.getBoolean("active");
                            Log.d("Events", "Activation: " + active);

                            mLastEventInfo.setText(now.toString() + "Activation: Stream " + (active ? "resumed" : "paused"));
                        }
                    } catch (JSONException e) {
                        Log.w("Events", "Could not parse json: " + json);
                    }
                }
            });
        }
    };
}
