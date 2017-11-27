package com.example.zhucebiao.indoorlocationwithpn;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by zhucebiao on 17-11-27.
 */

public class NtpTest {
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
            InetAddress hostAddr = InetAddress.getByName("127.0.0.1");
            TimeInfo info = client.getTime(hostAddr);
            info.computeDetails(); // compute offset/delay if not already done
            offsetValue = info.getOffset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return offsetValue;
    }
}
