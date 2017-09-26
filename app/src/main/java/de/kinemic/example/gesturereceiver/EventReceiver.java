package de.kinemic.example.gesturereceiver;

import org.zeromq.ZContext;
import org.zeromq.ZLoop;
import org.zeromq.ZMQ;


import static org.zeromq.ZSocket.UTF8;

public abstract class EventReceiver extends Thread {

    private ZContext mContext;
    private final String mEventStream;

    public EventReceiver() {
        this("tcp://127.0.0.1:9999");
    }

    public EventReceiver(String eventStream) {
        super("EventReceiver");
        setDaemon(true);

        mEventStream = eventStream;

        mContext = new ZContext();
    }

    @Override
    public void run() {
        ZLoop loop = new ZLoop(mContext);

        ZMQ.Socket eventSub = mContext.createSocket(ZMQ.SUB);
        eventSub.connect(mEventStream);
        eventSub.subscribe("".getBytes(UTF8));

        ZMQ.PollItem eventPoller = new ZMQ.PollItem(eventSub, ZMQ.Poller.POLLIN);
        loop.addPoller(eventPoller, mEventHandler, null);

        loop.start();

        loop.removePoller(eventPoller);
    }

    public void interrupt(long timeout) {
        Thread closer = new Thread() {
            @Override
            public void run() {
                mContext.close();
                EventReceiver.super.interrupt();
            }
        };
        closer.start();
        try {
            closer.join(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void interrupt() {
        interrupt(1000);
    }

    public void interruptAsync() {
        Thread t = new Thread() {
            @Override
            public void run() {
                EventReceiver.this.interrupt();
            }
        };
        t.start();
    }

    private ZLoop.IZLoopHandler mEventHandler = new ZLoop.IZLoopHandler() {
        @Override
        public int handle(ZLoop loop, ZMQ.PollItem item, Object arg) {
            final String jsonEvent = item.getSocket().recvStr().replace("null", "{}");;
            handleEvent(jsonEvent);

            return 0;
        }
    };

    protected abstract void handleEvent(String event);

}
