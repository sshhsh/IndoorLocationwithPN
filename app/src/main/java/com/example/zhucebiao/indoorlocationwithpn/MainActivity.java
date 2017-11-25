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
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    static final int RATE_HZ = 48000;
    static final int BUFFER_SIZE = RATE_HZ * 2;

    private AudioRecord record;

    /**
     * variables for calculation
     */
    private short[] pnData; //the PN sequence data
    private short[] rawSound; //raw sound data
    private double[] x;
    private double[] y; //location of speakers
    private double[] dd;
    private LocationCalculator locationCalculator;
    private CrossCorrelation correlation;
    private CorrelationAnalyzer analyzer;

    /**
     * variables for display
     */
    private LocationPainter painterLocation, painterWave1, painterWave2, painterWave3;
    private Button buttonStart, buttonStop;
    private EditText textX1, textX2, textX3, textY1, textY2, textY3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getAudioPermission();

        //find view
        painterLocation = findViewById(R.id.locationView);
        painterWave1 = findViewById(R.id.waveView1);
        painterWave2 = findViewById(R.id.waveView2);
        painterWave3 = findViewById(R.id.waveView3);
        buttonStart = findViewById(R.id.buttonStart);
        buttonStop = findViewById(R.id.buttonStop);
        textX1 = findViewById(R.id.x1);
        textX2 = findViewById(R.id.x2);
        textX3 = findViewById(R.id.x3);
        textY1 = findViewById(R.id.y1);
        textY2 = findViewById(R.id.y2);
        textY3 = findViewById(R.id.y3);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        start();
                    }
                }).start();
            }
        });

        //init
        record = new AudioRecord(MediaRecorder.AudioSource.MIC, RATE_HZ, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE * 2);
        rawSound = new short[BUFFER_SIZE];
        locationCalculator = new LocationCalculator(3);
        analyzer = new CorrelationAnalyzer(3);
        correlation = new CrossCorrelation(BUFFER_SIZE);
        x = new double[3];
        y = new double[3];
        dd = new double[2];
        getPnDataFromRes();
    }

    private void start() {
        updateSpeakerPosition();
        record.startRecording();
        record.read(rawSound, 0, BUFFER_SIZE);
        double[] correlationData = correlation.getResult(rawSound, pnData);
        int[] resultIndex = analyzer.cal(correlationData);
        painterWave1.giveWave(correlationData, resultIndex[0]);
        painterWave2.giveWave(correlationData, resultIndex[1]);
        painterWave3.giveWave(correlationData, resultIndex[2]);

        dd[0] = ((double) (resultIndex[1] - resultIndex[0]) / RATE_HZ - 0.5) * 340;
        dd[1] = ((double) (resultIndex[2] - resultIndex[1]) / RATE_HZ - 0.5) * 340;
        double[] result = locationCalculator.cal(dd);
        painterLocation.giveLocation(x, y, result[0], result[1]);
    }

    private void updateSpeakerPosition() {
        x[0] = Double.valueOf(textX1.getText().toString());
        x[1] = Double.valueOf(textX2.getText().toString());
        x[2] = Double.valueOf(textX3.getText().toString());
        y[0] = Double.valueOf(textY1.getText().toString());
        y[1] = Double.valueOf(textY2.getText().toString());
        y[2] = Double.valueOf(textY3.getText().toString());
        locationCalculator.updatePosition(x, y);
    }

    /**
     * check audio permission
     */
    private void getAudioPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    998);//998 means nothing
        } else {
            Log.e("Permission", "Permission success");
        }
    }

    /**
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
