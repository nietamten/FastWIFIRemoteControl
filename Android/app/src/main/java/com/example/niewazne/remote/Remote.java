package com.example.niewazne.remote;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class Remote extends AppCompatActivity {

    private static Receiver receiver;
    private static Remote remote;

    private static Joy joy;

    private static Sender sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
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
    }

    public static void Toast(final String msg) {
        remote.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(joy.getContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}


