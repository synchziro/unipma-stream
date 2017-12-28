package kp.tif.stream;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.moraleboost.streamscraper.ScrapeException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import kp.tif.stream.helpers.ShoutcastParser;
import kp.tif.stream.helpers.ModifiedWebView;

public class MainActivity extends Activity implements View.OnClickListener {

    private MediaPlayer mp;
    private Button buttonSendGreetings;
    private Button buttonSeeSchedule;
    private Spinner spinnerRaioChannel;
    private HashMap<String, String> channelSCAddressHash = new HashMap<String, String>();
    private String chosenChannel = "Deptics";
    private ImageView imageViewPlayPause;
    private Integer playerState = 0; // 0 = Stopped; 1 = Playing; 2 = Paused (currently not used)
    private HeadphoneUnplugReceiver mHeadphoneUnplugReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinnerRaioChannel = (Spinner) findViewById(R.id.spinnerRaioChannel);

        imageViewPlayPause = (ImageView) findViewById(R.id.imageViewPlayPause);
        buttonSendGreetings = (Button) findViewById(R.id.buttonSendGreetings);
        buttonSeeSchedule = (Button) findViewById(R.id.buttonSeeSchedule);

        imageViewPlayPause.setOnClickListener(this);
        buttonSendGreetings.setOnClickListener(this);
        buttonSeeSchedule.setOnClickListener(this);
        setButtonsState();
        setupShoutcastAddresses();

        // Listen for headphone unplug
        IntentFilter headphoneUnplugIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        mHeadphoneUnplugReceiver = new HeadphoneUnplugReceiver();
        registerReceiver(mHeadphoneUnplugReceiver, headphoneUnplugIntentFilter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //unregister headphone unplug
        this.unregisterReceiver(mHeadphoneUnplugReceiver);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (playerState==1) {
            stopRadio();
            Toast.makeText(this, getString(R.string.backToExit),
                    Toast.LENGTH_SHORT).show();
        } else {
            stopRadio();
            super.onBackPressed();
        }
    }

    public void onClick(View v) {
        if (v == imageViewPlayPause && playerState == 0) {
            startRadio();
        } else if (v == imageViewPlayPause && playerState == 1) {
            stopRadio();
        } else if (v == buttonSendGreetings) {
            startGreetingsWebView();
        } else if (v == buttonSeeSchedule) {
            startScheduleWebView();
        }
    }

    private void initializeMP() {
        mp = new MediaPlayer();
        try {
            mp.setDataSource(channelSCAddressHash.get(chosenChannel));
        } catch (IOException e) {
            Log.e("Deptics", "Gagal memutar audio. " + channelSCAddressHash.get(chosenChannel));
            e.printStackTrace();
        }
    }


    private void startRadio() {

        Log.d("Deptics", "Siaran dimulai.");

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle(getString(R.string.buffering));
        progress.setMessage(getString(R.string.pleaseWait));
        progress.show();
        initializeMP();
        mp.prepareAsync();
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            public void onPrepared(MediaPlayer mp) {
                MainActivity.this.mp.start();
                progress.dismiss();

            }
        });
        playerState = 1;
        Log.d("PlayerState", playerState.toString());
        setButtonsState();

    }

    private void stopRadio() {
        Log.d("Deptics", "Siaran berhenti.");

        try {
            playerState = 0;
            setButtonsState();
            mp.stop();
            mp.release();
            Log.d("PlayerState", playerState.toString());

        } catch (Exception e) {
            Log.d("Deptics", "Media telah ditindak lanjuti.");
        }

    }

    private void setupShoutcastAddresses() {
        channelSCAddressHash.put("Deptics", "http://streaming.shoutcast.com/Unstream-UNIPMAStreamNow");
        List<String> channelList = new ArrayList(channelSCAddressHash.keySet());
        Collections.sort(channelList, Collections.reverseOrder());
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, R.layout.custom_spinner, channelList);
        spinnerRaioChannel.setAdapter(dataAdapter);
        addListenerOnSpinnerItemSelection();

    }

    public void addListenerOnSpinnerItemSelection() {

        spinnerRaioChannel = (Spinner) findViewById(R.id.spinnerRaioChannel);
        spinnerRaioChannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                chosenChannel = parent.getItemAtPosition(position).toString();
                //stop radio if playing
                stopRadio();
                initializeMP();
                setButtonsState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    private void setButtonsState() {

        if (playerState == 0) {
            imageViewPlayPause.setImageResource(R.drawable.play);
        } else if (playerState == 1) {
            imageViewPlayPause.setImageResource(R.drawable.pause);

        }

    }

    private void startGreetingsWebView() {
        Log.d("Deptics", "Bagikan");

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.sendGreetings) + " " + getString(R.string.channel) + " " + chosenChannel);

        ModifiedWebView wv = new ModifiedWebView(this);
        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (chosenChannel.equals("Deptik")) {
            wv.loadUrl("http://tif.unipma.ac.id/daftar_agenda");
        }
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }
        });

        alert.setView(wv);
        alert.show();
    }
    private void startScheduleWebView() {
        Log.d("Deptics", "Jadwal");

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.Schedule) + " " + getString(R.string.channel) + " " + chosenChannel);

        ModifiedWebView wv = new ModifiedWebView(this);
        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (chosenChannel.equals("Deptik")) {
            wv.loadUrl("http://tif.unipma.ac.id/daftar_agenda");
        }
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }
        });

        alert.setView(wv);
        alert.show();
    }

    public class HeadphoneUnplugReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (playerState==1 && AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                Log.v("Deptics", "Headphones dilepas. Pemutaran dihentikan.");
                stopRadio();
            }
        }
    }


}
