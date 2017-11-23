package com.example.zhucebiao.indoorlocationwithpn;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by zhucebiao on 17-11-23.
 */
public class DataAreaTest {
    private CorrelationAnalyzer.DataArea dataArea1, dataArea2;

    @Test
    public void compareTo() throws Exception {
        assertEquals(1, dataArea1.compareTo(dataArea2));
    }

    @Before
    public void setUp() throws Exception {
        CorrelationAnalyzer correlationAnalyzer = new CorrelationAnalyzer(4);
        dataArea1 = correlationAnalyzer.new DataArea(0, 1, 2333, 2333);
        dataArea2 = correlationAnalyzer.new DataArea(0, 1, 2333, 2444);
    }
}