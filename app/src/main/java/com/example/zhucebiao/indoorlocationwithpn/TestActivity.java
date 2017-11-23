package com.example.zhucebiao.indoorlocationwithpn;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = "AudioRecord";
    private static final int RATE_HZ = 48000;
    private static final int BUFFER_SIZE = 48000 * 1;

    private Button buttonStart;
    private AudioRecord mRecord;
    private short[] bufferTMP;
    private CrossCorrelation crossCorrelation;
    private CorrelationAnalyzer correlationAnalyzer;
    private short[] pnData;

    private PathPainter pathPainter_sound;
    private PathPainter pathPainter_Xcorr1;
    private PathPainter pathPainter_detail1;
    private PathPainter pathPainter_detail2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        buttonStart = findViewById(R.id.buttonStart);
        pathPainter_sound = findViewById(R.id.soundView);
        pathPainter_Xcorr1 = findViewById(R.id.xcorrView1);
        pathPainter_detail1 = findViewById(R.id.soundWaveDetail1);
        pathPainter_detail2 = findViewById(R.id.soundWaveDetail2);
        getAudioPermission();

        getPnDataFromRes();

        setButton();
        initRecord();
    }

    private void getAudioPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    998);//998 means nothing
        } else {
            Log.e(TAG, "Permission success");
        }
    }

    private void setButton() {
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mRecord.startRecording();
                        mRecord.read(bufferTMP, 0, BUFFER_SIZE);//找到所需的音频数据
                        mRecord.stop();
                        pathPainter_sound.Draw(bufferTMP);
                        double[] result1 = crossCorrelation.getResult(bufferTMP, pnData);
                        int[] result2 = correlationAnalyzer.cal(result1);
                        double time1 = result2[0] * 1000.0 / RATE_HZ;
                        double time2 = result2[1] * 1000.0 / RATE_HZ;
                        pathPainter_Xcorr1.Drawline(result2);
                        pathPainter_detail1.DrawDetail(result1, result2[0]);
                        pathPainter_detail2.DrawDetail(result1, result2[1]);
                        Log.e("result", Double.toString(time1) + "  " + Double.toString(time2));

                    }
                }).start();
            }
        });
    }

    private void initRecord() {
        mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RATE_HZ, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE * 2);
        if (mRecord == null) {
            Log.e(TAG, "Failed in initialization!");
            return;
        }
        bufferTMP = new short[BUFFER_SIZE];
        crossCorrelation = new CrossCorrelation(BUFFER_SIZE);
        correlationAnalyzer = new CorrelationAnalyzer(2);
    }

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
