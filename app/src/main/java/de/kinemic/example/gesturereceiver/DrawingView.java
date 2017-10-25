package de.kinemic.example.gesturereceiver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Fabian on 25.10.17.
 */

public class DrawingView extends View {

    public int width;
    public  int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    Context mContext;
    private Paint circlePaint;
    protected Path circlePath;
    private Paint mPaint;
    private int mColorIntex = 0;
    private final int[] mColors = new int[] {Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW, Color.MAGENTA, Color.CYAN};

    public DrawingView(Context context) {
        super(context);
        mContext=context;
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        touch_up();
        touch_move(w/2, h/2, false);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath( mPath,  mPaint);
        canvas.drawPath( circlePath,  circlePaint);
    }

    protected float mX;
    protected float mY;
    private static final float TOUCH_TOLERANCE = 4;
    private boolean up = false;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        up = false;
    }

    protected void touch_move(float x, float y, boolean pressed) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            if (pressed && !up) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            } else if (pressed && up) {
                touch_start(x, y);
            } else if (!pressed && !up) {
                touch_up();
            }
            mX = x;
            mY = y;

            circlePath.reset();
            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void touch_up() {
        up = true;
        mPath.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath,  mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    /* move the cursor (circle) with touch */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y, false);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                //touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public void nextColor() {
        mColorIntex = (mColorIntex + 1) % mColors.length;
        mPaint.setColor(mColors[mColorIntex]);
    }

    public void prevColor() {
        mColorIntex = (mColorIntex + mColors.length - 1) % mColors.length;
        mPaint.setColor(mColors[mColorIntex]);
    }
}
