package com.example.niewazne.remote;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

//http://tutorials.jenkov.com/java-nio/datagram-channel.html


public class Receiver extends Thread {

    @Override
    public void run() {

        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(9998);
            ds.setBroadcast(true);
            ds.setSoTimeout(2000);
        } catch (Exception e) {
            Remote.FatalError(e.toString() + e.getMessage());
        }

        Date lastMsg = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();

        boolean connected = false;

        while (!interrupted() && ds != null) {
            byte[] buf = new byte[16];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            Date now = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();

            try {
                ds.receive(dp);

                if (connected && dp.getAddress() == null && ((now.getTime() - lastMsg.getTime()) > 1000)) {
                    connected = false;
                    Remote.SetJoyColor(false,0);
                } else if (!connected && dp.getAddress() != null && (buf[0] == 111)) {
                    lastMsg = now;
                    connected = true;
                    int temp = 0;//buf[1]<<1;//|(buf[2]<<8))<<1;

                    int firstByte = (0x000000FF & ((int)buf[1]));
                    int secondByte = (0x000000FF & ((int)buf[2]));

                    temp  = (char) (secondByte << 8 | firstByte);

                    Remote.SetJoyColor(true,temp);
                }
                if (connected) {




                    if (dp.getAddress() != null && (buf[0] == 111)) {

                        int temp = 0;

                        int firstByte = (0x000000FF & ((int)buf[1]));
                        int secondByte = (0x000000FF & ((int)buf[2]));

                        temp  = (char) (secondByte << 8 | firstByte);

                        Remote.SetJoyColor(true,temp);
                    }


                    sleep(100);
                }
            } catch (Exception e) {
                //Remote.Toast(e.toString()+e.getMessage());
                try {
                    if (((now.getTime() - lastMsg.getTime()) > 8000)) {
                        connected = false;
                        Remote.SetJoyColor(false,0);
                    }
                    sleep(100);
                } catch (InterruptedException e1) {
                    ds.close();
                    return;
                }
            }
        }
        if (ds != null)
            ds.close();
    }
}

