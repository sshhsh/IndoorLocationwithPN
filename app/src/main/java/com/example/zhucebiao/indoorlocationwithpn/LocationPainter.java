package com.example.zhucebiao.indoorlocationwithpn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.Locale;

/**
 * LocationPainter
 * Created by sshhsh on 17-11-24.
 */

public class LocationPainter extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private Paint paintLine, paintCircle, paintText;

    /**
     * variables for location display
     */
    private float[] locationX;
    private float[] locationY;
    private float xMin, xMax, yMin, yMax;

    public LocationPainter(Context context) {
        super(context);
        init();
    }

    public LocationPainter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LocationPainter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mCanvas = mHolder.lockCanvas();
            drawBox();
            drawLocation(event.getX(), event.getY());
            mHolder.unlockCanvasAndPost(mCanvas);
            performClick();
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mCanvas = mHolder.lockCanvas();
            drawBox();
            drawLocation(event.getX(), event.getY());
            mHolder.unlockCanvasAndPost(mCanvas);
        }
        return super.onTouchEvent(event);
    }

    /**
     * init and set the paints
     */
    private void init() {
        setFocusable(true);
        mHolder = getHolder();
        mHolder.addCallback(this);

        paintLine = new Paint();
        paintLine.setColor(Color.GRAY);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(1);
        paintLine.setAntiAlias(false);

        paintCircle = new Paint();
        paintCircle.setColor(Color.BLUE);
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setStrokeWidth(2);
        paintCircle.setAntiAlias(true);

        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(40);
        paintText.setStrokeWidth(1);
        paintText.setAntiAlias(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mCanvas = mHolder.lockCanvas();
        if (mCanvas == null) return;
        drawBox();
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        mCanvas = mHolder.lockCanvas();
        if (mCanvas == null) return;
        drawBox();
        drawLocation();
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    /**
     * draw a box to show the display area
     */
    private void drawBox() {
        if (mCanvas == null) return;
        int height = this.getHeight();
        int width = this.getWidth();
        mCanvas.drawColor(0xFFFFFFF8);
        mCanvas.drawRect(0, 0, width - 1, height - 1, paintLine);
    }

    /**
     * draw locations
     *
     * @param x x
     * @param y y
     */
    public void giveLocation(double[] x, double[] y, double x0, double y0) {
        if (x.length != y.length) return;

        if (locationX == null || locationY == null || locationX.length != x.length) {
            locationX = new float[x.length + 1];
            locationY = new float[y.length + 1];
        }
        locationX[0] = (float) x0;
        locationY[0] = (float) y0;
        xMin = Float.MAX_VALUE;
        xMax = -Float.MAX_VALUE;
        yMin = Float.MAX_VALUE;
        yMax = -Float.MAX_VALUE;
        if (xMin > locationX[0]) xMin = locationX[0];
        if (xMax < locationX[0]) xMax = locationX[0];
        if (yMin > locationY[0]) yMin = locationY[0];
        if (yMax < locationY[0]) yMax = locationY[0];
        for (int i = 1; i < x.length + 1; ++i) {
            locationX[i] = (float) x[i - 1];
            locationY[i] = (float) y[i - 1];
            if (xMin > locationX[i]) xMin = locationX[i];
            if (xMax < locationX[i]) xMax = locationX[i];
            if (yMin > locationY[i]) yMin = locationY[i];
            if (yMax < locationY[i]) yMax = locationY[i];
        }
        mCanvas = mHolder.lockCanvas();
        if (mCanvas == null) return;
        drawBox();
        drawLocation();
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    private void drawLocation(float... floats) {
        if (mCanvas == null) return;
        if (locationX == null || locationY == null) return;
        int height = this.getHeight();
        int width = this.getWidth();
        float rangeX = xMax - xMin;
        if (rangeX == 0) rangeX = (float) 0.01;
        float rangeY = yMax - yMin;
        if (rangeY == 0) rangeY = (float) 0.01;

        float offsetX, offsetY, slopeX, slopeY;
        if (rangeX / width < rangeY / height) {
            offsetX = ((float) 0.9 * width - rangeX / rangeY * (float) 0.9 * height) / 2 + (float) 0.05 * width;
            slopeX = (float) 0.9 * height / rangeY;
            offsetY = (float) 0.05 * height;
            slopeY = (float) 0.9 * height / rangeY;
        } else {
            offsetX = (float) 0.05 * width;
            slopeX = (float) 0.9 * width / rangeX;
            offsetY = ((float) 0.9 * height - rangeY / rangeX * (float) 0.9 * width) / 2 + (float) 0.05 * height;
            slopeY = (float) 0.9 * width / rangeX;
        }
        float x0 = (locationX[0] - xMin) * slopeX + offsetX;
        float y0 = (locationY[0] - yMin) * slopeY + offsetY;
        y0 = height - 1 - y0;
        mCanvas.drawCircle(x0, y0, 15, paintCircle);
        mCanvas.drawLine(x0 - 20, y0, x0 + 20, y0, paintLine);
        mCanvas.drawLine(x0, y0 - 20, x0, y0 + 20, paintLine);

        double tmpD = Double.MAX_VALUE;
        int tmpIndex = -1;
        if (floats.length == 2) {
            tmpD = Math.sqrt(Math.pow(floats[0] - x0, 2) + Math.pow(floats[1] - y0, 2));
            tmpIndex = 0;
        }

        for (int i = 1; i < locationX.length; ++i) {
            float x = (locationX[i] - xMin) * slopeX + offsetX;
            float y = (locationY[i] - yMin) * slopeY + offsetY;
            y = height - 1 - y;
            mCanvas.drawCircle(x, y, 10, paintCircle);

            if (floats.length == 2) {
                double tmpD2 = Math.sqrt(Math.pow(floats[0] - x, 2) + Math.pow(floats[1] - y, 2));
                if (tmpD > tmpD2) {
                    tmpD = tmpD2;
                    tmpIndex = i;
                }
            }
        }
        if (floats.length == 2) {
            float x = (locationX[tmpIndex] - xMin) * slopeX + offsetX;
            float y = (locationY[tmpIndex] - yMin) * slopeY + offsetY;
            y = height - 1 - y;
            mCanvas.drawLine(floats[0] * 0.95f + x * 0.05f, floats[1] * 0.95f + y * 0.05f,
                    floats[0] * 0.05f + x * 0.95f, floats[1] * 0.05f + y * 0.95f, paintCircle);
            String s = String.format(Locale.getDefault(), "(%.2f, %.2f)", locationX[tmpIndex], locationY[tmpIndex]);
            mCanvas.drawText(s, floats[0], floats[1], paintText);
        }
    }
}
