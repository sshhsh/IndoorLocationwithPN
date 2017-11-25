package com.example.zhucebiao.indoorlocationwithpn;

import android.support.test.runner.AndroidJUnit4;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by zhucebiao on 17-11-25.
 */
@RunWith(AndroidJUnit4.class)
public class RemoteTimeAndroidTest {

    @Test
    public void doInBackground() throws Exception {
        long[] r = new long[1], a = new long[1];
        r[0] = t();
        a[0] = System.currentTimeMillis();
        assertArrayEquals(a, r);
    }

    private long t() {
        try {
            URL url = new URL("http://192.168.10.222:3000");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(300);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();
                String res = IOUtils.toString(is, "ASCII");
                return Long.valueOf(res);
            } else {
                return -1;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }
}