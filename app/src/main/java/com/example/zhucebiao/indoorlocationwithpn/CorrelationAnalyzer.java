package com.example.zhucebiao.indoorlocationwithpn;

import java.util.Arrays;
import java.util.LinkedList;
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
    private Queue<Integer> start;//start index
    private Queue<Integer> end;//end index

    /**
     * init
     *
     * @param amount the amount of the pn sequences
     */
    CorrelationAnalyzer(int amount) {
        result = new int[amount];
        start = new LinkedList<>();
        end = new LinkedList<>();
    }

    /**
     * find signal positions in time line
     *
     * @param correlationData correlation data
     * @return time when signal occurs
     */
    public int[] cal(double[] correlationData) {
        int count = 0;
        start.offer(0);
        end.offer(correlationData.length);
        while (!start.isEmpty() && !end.isEmpty() && (count < result.length)) {
            int leftIndex = start.poll();
            int rightIndex = end.poll();
            int iMax = findMax(correlationData, leftIndex, rightIndex);
            int iReal = findFirstPeak(
                    correlationData,
                    (iMax - window) > 0 ? (iMax - window) : 0,
                    iMax,
                    correlationData[iMax] / rate);
            if (iMax - gap > leftIndex) {
                start.offer(leftIndex);
                end.offer(iMax - gap);
            }
            if (iMax + gap < rightIndex) {
                start.offer(iMax + gap);
                end.offer(rightIndex);
            }
            result[count] = iReal;
            count++;
        }
        while (!start.isEmpty()) {
            start.poll();
        }
        while (!end.isEmpty()) {
            end.poll();
        }//flush
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
