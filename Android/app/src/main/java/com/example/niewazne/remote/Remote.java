package com.example.niewazne.remote;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Remote extends AppCompatActivity implements SettingsObj.OnFragmentInteractionListener {

    private static Receiver receiver;
    private static Remote remote;

    private static Joy joy;

    private static Sender sender;

//    public static SettingsObj settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.sid, new SettingsObj())
                .commit();
*/
        setContentView(R.layout.activity_remote);
//        settings = new SettingsObj();
        joy = findViewById(R.id.joy);
        remote = this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        receiver = new Receiver();
        receiver.start();
        sender = new Sender();
        sender.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        receiver.interrupt();
        sender.interrupt();
        try {
            sender.join();
            receiver.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        receiver = null;
        sender = null;

    }


    public static void SetDataToSend(byte[] data) {
        if(sender != null)
            sender.SetData(data);
    }

    public static void SetJoyColor(final boolean connected, final int temp) {
        remote.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                joy.SetColor(connected,temp);
            }
        });
    }

    public static void FatalError(final String msg) {
        /*
        remote.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(remote).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage(msg);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                remote.finishAffinity();
                            }
                        });
                alertDialog.show();
            }
        });

         */
    }

    public static void Toast(final String msg) {
        remote.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(joy.getContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    public void abc(View b) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }
}


