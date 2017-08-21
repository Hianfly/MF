package com.hiandev.mf;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpFactory {

    private static final String TAG = HttpFactory.class.getName();

    public final static String[] doGet(String url) {
        String[] rs = new String[] { "", "" };
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        BufferedWriter bw = null;
        HttpURLConnection conn = null;
        String rl = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(25000);
            conn.setConnectTimeout(5000);
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();
            if (conn.getResponseCode() == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((rl = br.readLine()) != null) {
                    sb.append(rl).append("\n");
                }
            }
            rs[0] = sb.toString();
        } catch (Exception | Error e) {
            Log.e(TAG, Log.getStackTraceString(e));
            rs[1] = e.getMessage();
        } finally {
            close(br);
            close(bw);
            conn.disconnect();
        }
        return rs;
    }
    private final static void close(BufferedReader br) {
        try {
            br.close();
        } catch (Exception e) {
        }
    }
    private final static void close(BufferedWriter bw) {
        try {
            bw.close();
        } catch (Exception e) {
        }
    }

}
