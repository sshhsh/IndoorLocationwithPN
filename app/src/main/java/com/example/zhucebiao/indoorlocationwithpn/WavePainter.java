package com.example.zhucebiao.indoorlocationwithpn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

/**
 * WavePainter
 * Created by zhucebiao on 17-11-27.
 */

public class WavePainter extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private Paint paintLine, paintText;

    private float[] waveData;
    private double[] rawWaveData;
    private final static int rate = 2;
    private int maxIndex;
    private int rawIndex;
    private int dataLength;
    private String time;

    private float yMin, yMax;

    public WavePainter(Context context) {
        super(context);
        init();
    }

    public WavePainter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WavePainter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
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

        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(40);
        paintText.setStrokeWidth(1);
        paintText.setAntiAlias(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mCanvas = mHolder.lockCanvas();
        drawBox();
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mCanvas = mHolder.lockCanvas();
        drawBox();
        drawWave();
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
     * draw the cross-correlation data details
     *
     * @param y     the entire cross-correlation data
     * @param index index of maximum data
     */
    public void giveWave(double[] y, int index) {

        rawWaveData = y;
        rawIndex = index;

        updateWaveData();

        mCanvas = mHolder.lockCanvas();
        if (mCanvas == null) return;
        drawBox();
        drawWave();
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    private void updateWaveData() {
        if (rawWaveData == null) return;
        int len = rawWaveData.length;
        int index_start = rawIndex - 500 / 2 / rate;
        if (index_start < 0) {
            index_start = 0;
        }
        int index_end = index_start + 500 / rate;
        if (index_end > len) {
            index_end = len;
            index_start = index_end - len;
            if (index_start < 0) index_start = 0;
        }

        if (waveData == null || waveData.length < index_end - index_start) {
            waveData = new float[index_end - index_start];
        }
        yMin = Float.MAX_VALUE;
        yMax = -Float.MAX_VALUE;
        for (int i = index_start; i < index_end; ++i) {
            waveData[i - index_start] = (float) rawWaveData[i];
            if (yMin > waveData[i - index_start]) yMin = waveData[i - index_start];
            if (yMax < waveData[i - index_start]) yMax = waveData[i - index_start];
        }

        maxIndex = rawIndex - index_start;
        dataLength = index_end - index_start;
        time = Double.toString((double) rawIndex / 48000);
    }

    private void drawWave() {
        if (waveData == null) return;
        if (mCanvas == null) return;
        int height = this.getHeight();
        int width = this.getWidth();

        yMin -= 10;
        yMax += 10;
        float slopeX = (float) width / dataLength;
        float slopeY = height / (yMax - yMin);
        mCanvas.drawLine(maxIndex * slopeX, 0, maxIndex * slopeX, height, paintLine);
        Path path = new Path();
        path.moveTo(0, height - (waveData[0] - yMin) * slopeY);
        for (int i = 1; i < dataLength - 1; ++i) {
            path.lineTo(i * slopeX, height - (waveData[i] - yMin) * slopeY);
        }
        path.setLastPoint((dataLength - 1) * slopeX, height - (waveData[dataLength - 1] - yMin) * slopeY);
        mCanvas.drawPath(path, paintLine);
        mCanvas.drawText(time, 0, 40, paintText);
    }
}
