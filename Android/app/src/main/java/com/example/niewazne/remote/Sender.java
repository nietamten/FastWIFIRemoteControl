package com.example.niewazne.remote;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sender extends Thread {

    private byte[] data;

    @Override
    public void run() {
        data = null;
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(9999);
            ds.setBroadcast(true);
        } catch (Exception e) {
            Remote.FatalError(e.toString() + e.getMessage());
        }

        while (!interrupted() && ds != null) {
            SendData(ds);
            try {
                sleep(50);
            } catch (InterruptedException e) {
                ds.close();
                return;
            }
        }
        if(ds != null)
            ds.close();
    }

    synchronized private void SendData(DatagramSocket ds) {
        if (data != null)
            try {
                DatagramPacket dp = new DatagramPacket(data, data.length);
                dp.setAddress(InetAddress.getByName("255.255.255.255"));
                dp.setPort(9999);
                ds.send(dp);
                Log.d("send", dp.toString());
            } catch (Exception e) {
                Remote.Toast(e.toString() + e.getMessage());
                try {
                    sleep(500);
                } catch (InterruptedException e1) {
                }
                Remote.SetJoyColor(false,0);
            }

    }

    synchronized public void SetData(byte[] data) {
        this.data = data;
    }
}
