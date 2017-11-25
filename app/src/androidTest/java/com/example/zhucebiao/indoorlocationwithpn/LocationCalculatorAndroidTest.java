package com.example.zhucebiao.indoorlocationwithpn;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by zhucebiao on 17-11-24.
 * test
 */
@RunWith(AndroidJUnit4.class)
public class LocationCalculatorAndroidTest {
    private LocationCalculator calculator;

    @Test
    public void test1() throws Exception {
        double[] dd = {0.5090, 1.5745};
        double[] result = calculator.cal(dd);
        double[] real = {1, 1};
        assertArrayEquals(real, result, 0.01);
    }

    @Test
    public void test2() throws Exception {
        double[] dd = {1.8541, 0};
        double[] result = calculator.cal(dd);
        double[] real = {0, 0};
        assertArrayEquals(real, result, 0.01);
    }

    @Test
    public void test3() throws Exception {
        double[] dd = {1.2385, 2.9759};
        double[] result = calculator.cal(dd);
        double[] real = {1.49, 0.01};
        assertArrayEquals(real, result, 0.01);
    }

    @Test
    public void test4() throws Exception {
        double[] dd = {2.9759, -2.9759};
        double[] result = calculator.cal(dd);
        double[] real = {-1.49, 0.01};
        assertArrayEquals(real, result, 0.01);
    }

    @Test
    public void test5() throws Exception {
        double[] dd = {3.1, -2.9759};
        double[] result = calculator.cal(dd);
        double[] real = {-1.49, 0.01};
        assertArrayEquals(real, result, 0.1);
    }

    @Before
    public void setUp() throws Exception {
        double[] x = {-1.5, -1.5, 1.5};
        double[] y = {3, 0, 0};
        calculator = new LocationCalculator(3);
        calculator.updatePosition(x, y);
    }

}