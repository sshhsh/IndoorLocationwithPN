package com.example.zhucebiao.indoorlocationwithpn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

/**
 * Created by zhucebiao on 17-8-18.
 */

public class PathPainter extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private Paint p, p2, pText;
    private int rate = 2;

    public PathPainter(Context context) {
        super(context);
        init();
    }

    public PathPainter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PathPainter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        p = new Paint();
        p.setColor(Color.GRAY);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1);
        pText = new Paint();
        pText.setColor(Color.BLACK);
        pText.setTextSize(40);
        pText.setStrokeWidth(1);
        p2 = new Paint();
        p2.setColor(Color.GRAY);
        p2.setStyle(Paint.Style.STROKE);
        p2.setStrokeWidth(10);
        mCanvas = holder.lockCanvas();
        mCanvas.drawColor(Color.WHITE);
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void Draw(Path path) {
        mCanvas = mHolder.lockCanvas();
        if (mCanvas == null) return;
        mCanvas.drawColor(Color.WHITE);
        mCanvas.drawPath(path, p);
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    public void DrawDetail(double[] y) {
        int w = this.getWidth();
        int h = this.getHeight();
        int len = y.length;
        double yMax = Double.MIN_VALUE;
        double yMin = Double.MAX_VALUE;
        int index_max = 0, index_start, index_end;
        for (int i = 0; i < len; ++i) {
            if (y[i] < yMin) yMin = y[i];
            if (y[i] > yMax) {
                yMax = y[i];
                index_max = i;
            }
        }
        yMin -= 10;
        yMax += 10;
        double yD = yMax - yMin;
        Log.e("xcorrmax", Double.toString(yMax));
        Log.e("xcorrmin", Double.toString(yMin));
        index_start = index_max - w / 2 / rate;
        if (index_start < 0) {
            index_start = 0;
        }
        index_end = index_start + w / rate;
        if (index_end > len) {
            index_end = len;
            index_start = index_end - len;
            if (index_start < 0) index_start = 0;
        }

        Path path = new Path();
        path.moveTo(0, (float) (yMax - (y[index_start]) / yD * h));
        for (int i = index_start + 1; i < index_end - 1; ++i) {
            path.lineTo(rate * (i - index_start), (float) ((yMax - y[i]) / yD * h));
        }
        path.setLastPoint(w - 1, (float) ((yMax - y[index_end - 1]) / yD * h));

        mCanvas = mHolder.lockCanvas();
        if (mCanvas == null) return;
        mCanvas.drawColor(Color.WHITE);
        mCanvas.drawPath(path, p);
        mCanvas.drawText(Double.toString((double) index_max / len * 44100 / len), 0, 40, pText);
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    public void DrawDetail(double[] y, int index) {
        int w = this.getWidth();
        int h = this.getHeight();
        int len = y.length;
        double yMax = Double.MIN_VALUE;
        double yMin = Double.MAX_VALUE;
        int index_start = index - w / 2 / rate;
        if (index_start < 0) {
            index_start = 0;
        }
        int index_end = index_start + w / rate;
        if (index_end > len) {
            index_end = len;
            index_start = index_end - len;
            if (index_start < 0) index_start = 0;
        }
        for (int i = index_start; i < index_end; ++i) {
            if (y[i] < yMin) yMin = y[i];
            if (y[i] > yMax) {
                yMax = y[i];
            }
        }
        yMin -= 10;
        yMax += 10;
        double yD = yMax - yMin;
        Log.e("xcorrmax", Double.toString(yMax));
        Log.e("xcorrmin", Double.toString(yMin));


        Path path = new Path();
        path.moveTo(0, (float) (yMax - (y[index_start]) / yD * h));
        for (int i = index_start + 1; i < index_end - 1; ++i) {
            path.lineTo(rate * (i - index_start), (float) ((yMax - y[i]) / yD * h));
        }
        path.setLastPoint(w - 1, (float) ((yMax - y[index_end - 1]) / yD * h));

        mCanvas = mHolder.lockCanvas();
        if (mCanvas == null) return;
        mCanvas.drawColor(Color.WHITE);
        mCanvas.drawPath(path, p);
        mCanvas.drawText(Double.toString((double) index / len * 48000 / len), 0, 40, pText);
        mCanvas.drawLine(w / 2, 0, w / 2, h, p);
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    public void Draw(double[] y) {
        int w = this.getWidth();
        int h = this.getHeight();
        int len = y.length;
        double yMax = Double.MIN_VALUE;
        double yMin = Double.MAX_VALUE;
        for (int i = 0; i < len; ++i) {
            if (y[i] < yMin) yMin = y[i];
            if (y[i] > yMax) yMax = y[i];
        }
        yMin -= 10;
        yMax += 10;
        double yD = yMax - yMin;
        Log.e("xcorrmax", Double.toString(yMax));
        Log.e("xcorrmin", Double.toString(yMin));
        Path path = new Path();
        path.moveTo(0, (float) ((y[0] - yMin) / yD * h));
        for (int i = 1; i < w - 1; ++i) {
            path.lineTo(i, (float) ((y[i * len / w] - yMin) / yD * h));
        }
        path.setLastPoint(w - 1, (float) ((y[len - 1] - yMin) / yD * h));

        mCanvas = mHolder.lockCanvas();
        if (mCanvas == null) return;
        mCanvas.drawColor(Color.WHITE);
        mCanvas.drawPath(path, p);
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    public void Draw(short[] y) {
        int w = this.getWidth();
        int h = this.getHeight();
        int len = y.length;
        float yMax = Float.MIN_VALUE;
        float yMin = Float.MAX_VALUE;
        for (int i = 0; i < len; ++i) {
            if (y[i] < yMin) yMin = y[i];
            if (y[i] > yMax) yMax = y[i];
        }
        yMin -= 10;
        yMax += 10;
        float yD = yMax - yMin;
        Path path = new Path();
        path.moveTo(0, (y[0] - yMin) / yD * h);
        for (int i = 1; i < w - 1; ++i) {
            path.lineTo(i, (y[i * len / w] - yMin) * h / yD);
        }
        path.setLastPoint(w - 1, (y[len - 1] - yMin) * h / yD);

        mCanvas = mHolder.lockCanvas();
        if (mCanvas == null) return;
        mCanvas.drawColor(Color.WHITE);
        mCanvas.drawPath(path, p);
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    public void Drawline(int[] time) {
        int w = this.getWidth();
        int h = this.getHeight();
        mCanvas = mHolder.lockCanvas();
        if (mCanvas == null) return;
        mCanvas.drawColor(Color.WHITE);
        mCanvas.drawLine((float) time[0] * w / 44100, 0, (float) time[0] * w / 44100, h, p);
        mCanvas.drawLine((float) time[1] * w / 44100, 0, (float) time[1] * w / 44100, h, p);
        mHolder.unlockCanvasAndPost(mCanvas);
    }
}
