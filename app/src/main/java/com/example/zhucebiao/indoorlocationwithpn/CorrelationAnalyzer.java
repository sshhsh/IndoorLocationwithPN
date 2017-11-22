package com.example.zhucebiao.indoorlocationwithpn;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by sshhsh on 17-11-22.
 * <p>
 * CorrelationAnalyzer
 * find signal positions in time line
 */

public class CorrelationAnalyzer {
    static final private int window = 200; //window width for multi path analyze
    static final private int rate = 10; //rate between largest correlation and the smallest accessible one
    static final private int gap = 48000 / 1000 * 400; //minimum gap between two signals 400ms

    private int[] result;
    private Queue<DataArea> dataAreas;

    private class DataArea implements Comparable<DataArea> {
        int startIndex;
        int endIndex;
        int maxValueIndex;
        double maxValue;

        DataArea(int startIndex, int endIndex, int maxValueIndex, double maxValue) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.maxValueIndex = maxValueIndex;
            this.maxValue = maxValue;
        }

        @Override
        public int compareTo(@NonNull DataArea dataArea) {
            if (maxValue > dataArea.maxValue) {
                return -1;
            } else if (maxValue < dataArea.maxValue) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * init
     *
     * @param amount the amount of the pn sequences
     */
    CorrelationAnalyzer(int amount) {
        result = new int[amount];
        for (int i = 0; i < amount; ++i) {
            result[i] = -1;
        }
        dataAreas = new PriorityQueue<>();
    }

    /**
     * find signal positions in time line
     *
     * @param correlationData correlation data
     * @return time when signal occurs
     */
    public int[] cal(double[] correlationData) {
        int count = 0;
        int iMax = findMax(correlationData, 0, correlationData.length);
        DataArea d = new DataArea(0, correlationData.length, iMax, correlationData[iMax]);
        dataAreas.offer(d);
        while (!dataAreas.isEmpty() && count < result.length) {
            DataArea tmp = dataAreas.poll();
            int iReal = findFirstPeak(correlationData, tmp.maxValueIndex - window, tmp.maxValueIndex, tmp.maxValue / rate);
            result[count] = iReal;
            count++;
            if (tmp.maxValueIndex - gap > tmp.startIndex) {
                int iMax1 = findMax(correlationData, tmp.startIndex, tmp.maxValueIndex - gap);
                DataArea tmp1 = new DataArea(tmp.startIndex, tmp.maxValueIndex - gap, iMax1, correlationData[iMax1]);
                dataAreas.offer(tmp1);
            }
            if (tmp.maxValueIndex + gap < tmp.endIndex) {
                int iMax2 = findMax(correlationData, tmp.maxValueIndex + gap, tmp.endIndex);
                DataArea tmp2 = new DataArea(tmp.maxValueIndex + gap, tmp.endIndex, iMax2, correlationData[iMax2]);
                dataAreas.offer(tmp2);
            }
        }
        while (!dataAreas.isEmpty()) {
            dataAreas.poll();
        }
        Arrays.sort(result);
        return result;
    }

    /**
     * find the index of maximum value among data
     *
     * @param data       data
     * @param startIndex start index
     * @param endIndex   end index
     * @return the index of maximum value among data
     */
    private int findMax(double[] data, int startIndex, int endIndex) {
        double tmp = 0;
        int index = -1;
        for (int i = startIndex; i < endIndex; ++i) {
            if (data[i] > tmp) {
                tmp = data[i];
                index = i;
            }
        }
        return index;
    }

    /**
     * find the index of first peak among data
     *
     * @param data       data
     * @param startIndex start index
     * @param endIndex   end index
     * @return the index of first peak among data
     */
    private int findFirstPeak(double[] data, int startIndex, int endIndex, double accessibleValue) {
        int index = -1;
        for (int i = startIndex; i < endIndex; ++i) {
            if (data[i] > accessibleValue) {
                for (int j = i; j < endIndex; ++j) {
                    if (data[j - 1] > data[j]) {
                        index = j - 1;
                        break;
                    }
                }
                break;
            }
        }
        return index;
    }
}
