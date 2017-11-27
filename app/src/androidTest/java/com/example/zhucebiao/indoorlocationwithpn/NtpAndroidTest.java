package com.example.zhucebiao.indoorlocationwithpn;

import android.support.test.runner.AndroidJUnit4;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.Assert.assertEquals;

/**
 * Created by zhucebiao on 17-11-27.
 */
@RunWith(AndroidJUnit4.class)
public class NtpAndroidTest {
    @Test
    public void name() throws Exception {
        assertEquals(timeOffset(), 244);
    }

    private long timeOffset() {
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(300);
        Long offsetValue = 0L;
        try {
            client.open();
            InetAddress hostAddr = InetAddress.getByName("192.168.1.100");
            TimeInfo info = client.getTime(hostAddr);
            info.computeDetails(); // compute offset/delay if not already done
            offsetValue = info.getOffset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return offsetValue;
    }
}
