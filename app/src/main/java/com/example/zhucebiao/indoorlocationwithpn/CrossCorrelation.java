package com.example.zhucebiao.indoorlocationwithpn;

import org.jtransforms.fft.DoubleFFT_1D;

public class CrossCorrelation {
    private static final String TAG = "CrossCorrelation";
    private double a[];
    private double b[];
    private double c[];
    private double r[];
    private int len;
    private DoubleFFT_1D fft;

    /**
     * init
     *
     * @param length the length of the longData
     */
    public CrossCorrelation(int length) {
        len = length;
        fft = new DoubleFFT_1D(length);

        a = new double[len * 2];
        b = new double[len * 2];
        c = new double[len * 2];
        r = new double[len];
    }

    /**
     * Calculate the cross-correlation between longData and sequence
     *
     * @param longData the raw sound data you get
     * @param sequence your PN sequence
     * @return result with the same length as longData
     */
    public double[] getResult(short[] longData, short[] sequence) {
        if (len != longData.length)
            return null;
        for (int i = 0; i < len; ++i) {
            a[2 * i] = longData[i];
            a[2 * i + 1] = 0;
            if (i < sequence.length) b[2 * i] = sequence[i];
            else b[2 * i] = 0;
            b[2 * i + 1] = 0;
        }
        fft.complexForward(a);
        //filt(a);
        fft.complexForward(b);
        for (int i = 0; i < len; ++i) {
            b[2 * i + 1] = -b[2 * i + 1];
        }
        for (int i = 0; i < len; ++i) {
            c[2 * i] = a[2 * i] * b[2 * i] - a[2 * i + 1] * b[2 * i + 1];
            c[2 * i + 1] = a[2 * i] * b[2 * i + 1] + a[2 * i + 1] * b[2 * i];
        }
        fft.complexInverse(c, false);
        for (int i = 0; i < len; ++i) {
            r[i] = c[2 * i] * c[2 * i] + c[2 * i + 1] * c[2 * i + 1];
        }
        return r;
    }

    /**
     * ideal frequency filter
     * @param a raw sound data
     */
    private void filt(double[] a) {
        int t1 = 17000 * len / 44100;
        int t2 = 22000 * len / 44100;
        for (int i = 0; i < 2 * t1; ++i) {
            a[2 * t1] = 0;
            a[2 * t1 + 1] = 0;
        }
        for (int i = 2 * t2; i < 2 * (len - t2); ++i) {
            a[2 * t1] = 0;
            a[2 * t1 + 1] = 0;
        }
        for (int i = 2 * (len - t1); i < 2 * len; ++i) {
            a[2 * t1] = 0;
            a[2 * t1 + 1] = 0;
        }
    }
}
