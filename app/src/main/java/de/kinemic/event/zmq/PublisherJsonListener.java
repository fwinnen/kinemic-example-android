package de.kinemic.event.zmq;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.zeromq.ZActor;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZPoller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.kinemic.event.PublisherEvent;

import static org.zeromq.ZSocket.UTF8;

/**
 * This class can be used to receive Publisher events over the network (also locally).
 * It is a low level class, normally {@link PublisherListener} should be used.
 */
abstract class PublisherJsonListener {

    // accessed from the ioThread
    private ZActor mActor;
    private ZMQ.Socket mPipe;

    // FIXME: currently accessed from 2 threads, but readonly access after thread started
    private ArrayList<String> mTopics;
    private ArrayList<String> mLogLevels;

    // accessed from main thread (or calling thread)
    private HandlerThread mIOThread;
    private Handler mIOHandler;

    public PublisherJsonListener() {
        mTopics = new ArrayList<>();
        mTopics.add("");
        mLogLevels = new ArrayList<>();
    }

    // TODO: move this method inside handler
    /**
     * Set which log levels should be received.
     * Has to be called before {@link #start()}
     * Default is to receive no log events.
     * @param levels list of log levels ("verbose", "debug", "info", "warn", "error")
     */
    public void setLogLevels(String... levels) {
        if (mActor == null) {
            mLogLevels.clear();
            Collections.addAll(mLogLevels, levels);
        }
    }

    // TODO: move this method inside handler
    /**
     * Set the event types to listen for.
     * Has to be called before {@link #start()}
     * Default is to accept all event types.
     * @param types list of event types ("Gesture", "Writing", "MouseEvent", "Activation", "Heartbeat")
     */
    public void setEventTypes(String... types) {
        if (mActor == null) {
            mTopics.clear();
            Collections.addAll(mTopics, types);
        }
    }

    public void start(String ip) {
        if (mIOThread == null) {
            mIOThread = new HandlerThread("EventListenerIO-helper");
            mIOThread.start();
            mIOHandler = new IOHandler(mIOThread.getLooper());
            Message m = mIOHandler.obtainMessage(IOHandler.MSG_START);
            m.obj = ip;
            m.sendToTarget();
        }
    }

    public void start() {
        start("localhost");
    }

    public void stop() {
        if (mIOThread != null) {
            mIOHandler.obtainMessage(IOHandler.MSG_STOP).sendToTarget();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mIOThread.quitSafely();
            } else {
                mIOThread.quit();
            }
            mIOThread = null;
            mIOHandler = null;
        }
    }

    private ZActor.Actor acting = new ZActor.SimpleActor() {
        ZMQ.Socket mLogSub = null;
        ZMQ.Socket mEventSub = null;

        @Override
        public List<ZMQ.Socket> createSockets(ZContext ctx, Object[] args) {
            Log.d("Actor", "create sockets");

            mLogSub = ctx.createSocket(ZMQ.SUB);
            mEventSub = ctx.createSocket(ZMQ.SUB);

            return Arrays.asList(mLogSub, mEventSub);
        }

        @Override
        public boolean backstage(ZMQ.Socket pipe, ZPoller poller, int events) {
            String cmd = pipe.recvStr();
            if ("$TERM".equals(cmd)) {
                // end of the actor
                return false;
            } else if ("START".equals(cmd)) {
                String ip = "localhost";
                if (pipe.hasReceiveMore()) ip = pipe.recvStr();
                if (mLogLevels.size() > 0) {
                    mLogSub.connect("tcp://" + ip + ":9996");
                    for (String level : mLogLevels) {
                        mLogSub.subscribe(level.getBytes(UTF8));
                    }
                    poller.register(mLogSub, ZMQ.Poller.POLLIN);
                }

                mEventSub.connect("tcp://" + ip + ":9999");
                for (String level : mTopics) {
                    mEventSub.subscribe(level.getBytes(UTF8));
                }
                poller.register(mEventSub, ZMQ.Poller.POLLIN);
                return true;
            }
            return true;
        }

        @Override
        public boolean stage(ZMQ.Socket socket, ZMQ.Socket pipe, ZPoller poller, int events) {
            if (socket == mLogSub && (events & ZMQ.Poller.POLLIN) != 0) {
                // message contains 2 parts
                final String level = socket.recvStr();
                if (socket.hasReceiveMore()) {
                    final String jsonLog = socket.recvStr();
                    handleLog(level, jsonLog);
                }
            } else if (socket == mEventSub && (events & ZMQ.Poller.POLLIN) != 0) {
                final String part1 = socket.recvStr();
                if (socket.hasReceiveMore()) {
                    // two part messages indicate first part is topic
                    final String jsonEvent = socket.recvStr().replace("null", "{}");
                    handleEvent(part1, jsonEvent);
                } else {
                    try {
                        PublisherEvent base = PublisherEvent.fromJson(part1);
                        if (mTopics.contains(base.type.jsonType) || mTopics.contains("")) {
                            handleEvent(base.type.jsonType, part1);
                        }
                    } catch (JSONException e) {
                        Log.w("Events", "Could not parse json: " + part1, e);
                    }
                }
            }

            return true;
        }
    };

    protected abstract void handleLog(String level, String json);

    protected abstract void handleEvent(String type, String json);

    /* this thread is used to call pipes' send message from outside the main thread.
     * This seams to only be a problem on wear and glass
     */
    private class IOHandler extends Handler {
        private static final int MSG_START = 1;
        private static final int MSG_STOP = 2;

        public IOHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // this will run on non main thread, so its ok to access network here
            switch (msg.what) {
                case MSG_START:
                    if (mActor == null) {
                        mActor = new ZActor(acting, "EventListenerIO");
                        mPipe = mActor.pipe();

                        if (msg.obj != null) {
                            mPipe.send("START", ZMQ.SNDMORE);
                            mPipe.send((String) msg.obj);
                        } else {
                            mPipe.send("START");
                        }
                    }
                    break;
                case MSG_STOP:
                    if (mActor != null) {
                        mPipe.send("$TERM");
                        try {
                            mActor.exit().await();
                        } catch (InterruptedException e) {
                            Log.w("Event", e.getMessage(), e);
                        }
                        Log.d("Actor", "stopped");
                        mActor = null;
                    }
                    break;
            }
        }
    }
}
