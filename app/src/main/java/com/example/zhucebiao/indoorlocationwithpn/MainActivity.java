package com.example.zhucebiao.indoorlocationwithpn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {
    static final int RATE_HZ = 48000;
    static final int BUFFER_SIZE = RATE_HZ;
    static final String REMOTE_URL = "192.168.1.100";

    private AudioRecord record;
    private long remoteTimeOffset = 0;

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
    private Button buttonStart;
    private EditText textX1, textX2, textX3, textY1, textY2, textY3;
    private ProgressBar progressBar;

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
        textX1 = findViewById(R.id.x1);
        textX2 = findViewById(R.id.x2);
        textX3 = findViewById(R.id.x3);
        textY1 = findViewById(R.id.y1);
        textY2 = findViewById(R.id.y2);
        textY3 = findViewById(R.id.y3);
        progressBar = findViewById(R.id.progressBar);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CalculationTask().execute();
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

        new RemoteTime().execute(REMOTE_URL);
        buttonStart.setEnabled(true);
    }

    /**
     * save the speakers' position data from input boxes
     */
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
            InputStream in = getResources().openRawResource(R.raw.data48000_18_20_200ms);
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

    @SuppressLint("StaticFieldLeak")
    private class RemoteTime extends AsyncTask<String, Integer, Long> {

        @Override
        protected Long doInBackground(String... strings) {
            NTPUDPClient client = new NTPUDPClient();
            client.setDefaultTimeout(300);
            Long offsetValue = 0L;
            try {
                client.open();
                InetAddress hostAddr = InetAddress.getByName(strings[0]);
                TimeInfo info = client.getTime(hostAddr);
                info.computeDetails(); // compute offset/delay if not already done
                offsetValue = info.getOffset();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return offsetValue;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            remoteTimeOffset = aLong;
            Log.e("timeDiff", Long.toString(remoteTimeOffset));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CalculationTask extends AsyncTask<Integer, Integer, Integer> {
        double[] correlationData;
        int[] resultIndex;
        double[] result;//location result

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            switch (values[0]) {
                case 0:
                    progressBar.setVisibility(View.GONE);
                    break;
                case 1:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    painterWave1.giveWave(correlationData, resultIndex[0]);
                    painterWave2.giveWave(correlationData, resultIndex[1]);
                    painterWave3.giveWave(correlationData, resultIndex[2]);
                    break;
            }
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            long timeNow = System.currentTimeMillis() % 1000;
            try {
                Thread.sleep((2000 - timeNow - remoteTimeOffset % 1000) % 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return -1;
            }
            Log.e("time", Long.toString(System.currentTimeMillis() + remoteTimeOffset));
            updateSpeakerPosition();
            record.startRecording();
            publishProgress(1);
            record.read(rawSound, 0, BUFFER_SIZE);
            publishProgress(0);
            record.stop();
            correlationData = correlation.getResult(rawSound, pnData);
            resultIndex = analyzer.cal(correlationData);
            publishProgress(2);

            dd[0] = -((double) (resultIndex[1] - resultIndex[0]) / RATE_HZ - 0.25) * 340;
            dd[1] = -((double) (resultIndex[2] - resultIndex[1]) / RATE_HZ - 0.25) * 340;
            result = locationCalculator.cal(dd);
            painterLocation.giveLocation(x, y, result[0], result[1]);
            return 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            buttonStart.setEnabled(false);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            buttonStart.setEnabled(true);
        }
    }
}
