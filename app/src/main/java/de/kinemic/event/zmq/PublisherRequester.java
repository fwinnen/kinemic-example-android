package de.kinemic.event.zmq;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.zeromq.ZMQ;
import org.zeromq.ZSocket;

/**
 * This class is used to send requests back to the publisher.
 * Currently there is only one request, which resets the orientation reference for the airmouse.
 */
public class PublisherRequester {

    private HandlerThread mIOThread;
    private IOHandler mIOHandler;

    public void start() {
        start("localhost");
    }

    public void start(String ip) {
        if (mIOThread == null) {
            mIOThread = new HandlerThread("requesterIO");
            mIOThread.start();
            mIOHandler = new IOHandler(ip, mIOThread.getLooper());
            mIOHandler.obtainMessage(IOHandler.MSG_START).sendToTarget();
        }
    }

    public void stop() {
        if (mIOThread != null) {
            mIOHandler.obtainMessage(IOHandler.MSG_TERM).sendToTarget();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mIOThread.quitSafely();
            } else {
                mIOThread.quit();
            }
            mIOThread = null;
            mIOHandler = null;
        }
    }

    public void requestOrientationReset() {
        if (mIOThread != null) {
            mIOHandler.obtainMessage(IOHandler.MSG_REQUEST_ORIENTATION).sendToTarget();
        }
    }

    private static class IOHandler extends Handler {

        private static final int MSG_REQUEST_ORIENTATION = 1;
        private static final int MSG_TERM = 2;
        private static final int MSG_START = 3;

        private Handler mHandler;
        private ZSocket mPublisher;
        private final String mIP;

        IOHandler(String ip, Looper looper) {
            super(looper);
            mIP = ip;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MSG_START:
                    mPublisher = new ZSocket(ZMQ.PUB);
                    mPublisher.connect("tcp://" + mIP + ":9998");
                    break;
                case MSG_REQUEST_ORIENTATION:
                    try {
                        JSONObject request = new JSONObject();
                        request.put("type", "OrientationReset");
                        request.put("payload", null);
                        mPublisher.sendStringUtf8(request.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_TERM:
                    mPublisher.close();
                    break;
            }
        }
    }
}
