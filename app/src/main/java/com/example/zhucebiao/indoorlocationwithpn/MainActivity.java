package com.example.zhucebiao.indoorlocationwithpn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    static final int RATE_HZ = 48000;
    static final int BUFFER_SIZE = RATE_HZ * 1;

    private short[] pnData; //the PN sequence data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /***********************
     * 将伪随机序列从raw中读取出来
     */
    private void getPnDataFromRes() {
        try {
            InputStream in = getResources().openRawResource(R.raw.data48_16_18);
            int length = in.available();
            byte[] tmp = new byte[length];
            in.read(tmp);
            in.close();
            pnData = new short[length / 2];
            for (int i = 0; i < pnData.length; ++i) {
                int tmp1 = tmp[2 * i] << 8;
                int tmp2 = tmp[2 * i + 1] & 0x000000ff;
                pnData[i] = (short) (tmp1 + tmp2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
