package de.salait.speechcare.tasks;

import android.app.Activity;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import de.salait.speechcare.dao.MediaDataSource;
import de.salait.speechcare.data.Media;

public class TaskMediaDownload extends AsyncTask<Integer, Integer, Void> {
    public static final String authUsername = "speechcare";
    public static final String authPassword = "kugelblitz";
    static final String COOKIES_HEADER = "Set-Cookie";
    private static final String doHandshake = "handshake/begin/";
    private static final String verifyHandshake = "handshake/verify/?response=";
    private static final String mediaBaseUrl = "media/download/";
    private static final String secret = "g9od1m3uv.Z%$#R4KT3C!b7Ny@x=0&r!u4Y+T#]GCWko5<X_73TD9u8FWU%=7}v";
    static java.net.CookieManager msCookieManager = new java.net.CookieManager();
    private final Activity activity;
    File directory;
    private String DOMAIN_ONLY = "https://admin.speechcare.de/";
    private MediaDataSource mds;
    private List<Media> mediaList;


    public TaskMediaDownload(Activity c, List media) {
        activity = c;
        System.out.println(" <<< TaskMediaDownload");
        ContextWrapper contextWrapper = new ContextWrapper(activity);
        directory = contextWrapper.getFilesDir();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }
        MediaDataSource mds = null;
        mediaList = media;
        try {
            mds = new MediaDataSource(activity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Integer... integers) {

        try {
            String url = DOMAIN_ONLY + doHandshake;
            URL myURL = new URL(url);
            System.out.println("**** myURL " + myURL);
            HttpURLConnection httpConn = (HttpURLConnection) myURL.openConnection();
            System.out.println("**** httpConn ");
            String credentials = authUsername + ":" + authPassword;
            String basicAuth = "Basic " + new String(Base64.encode(credentials.getBytes(), Base64.DEFAULT));
            httpConn.setRequestProperty("Authorization", basicAuth);
            httpConn.setRequestMethod("GET");
            int statusCode = httpConn.getResponseCode();
            if (statusCode == 200) {


                BufferedReader in = new BufferedReader(
                        new InputStreamReader(httpConn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String entityStr = response.toString();

                Map<String, List<String>> headerFields = httpConn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);


                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    }
                }

                verifyHandshake(entityStr);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private void verifyHandshake(String response) {

        try {


            String subUrl = md5String(response + String.valueOf(quersumme(response)) + secret);
            URL myURL = new URL(DOMAIN_ONLY + verifyHandshake + subUrl);
            HttpURLConnection httpConn = (HttpURLConnection) myURL.openConnection();
            String credentials = authUsername + ":" + authPassword;
            String basicAuth = "Basic " + new String(Base64.encode(credentials.getBytes(), Base64.DEFAULT));
            httpConn.setRequestProperty("Authorization", basicAuth);
            httpConn.setRequestMethod("GET");


            if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                httpConn.setRequestProperty("Cookie",
                        TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
            }


            int statusCode = httpConn.getResponseCode();

            if (statusCode == 200) {

                Map<String, List<String>> headerFields = httpConn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);


                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    }
                }


                preparemediaDownload();
            } else {
                // TODO ERROR HANDLING
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    private void preparemediaDownload() {

        try {
            mds = new MediaDataSource(activity);
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println(" ** DLM mediaList.size() " + mediaList.size());
        for (int i = 0; i < mediaList.size(); i++) {

            if (isCancelled()) {
                break;
            }
            System.out.println(" ** DLM count " + i);
            Media m = mediaList.get(i);
            if (m.getFilname().length() == 0 || m.getFilname().equals("null")) {
                m.setFilname(m.getOriginalfile());
            }

            downloadMedia(m);

        }
    }

    private void downloadMedia(Media m) {

        try {

            URL myURL = new URL(DOMAIN_ONLY + mediaBaseUrl + m.getId());
            HttpURLConnection httpConn = (HttpURLConnection) myURL.openConnection();
            String credentials = authUsername + ":" + authPassword;
            String basicAuth = "Basic " + new String(Base64.encode(credentials.getBytes(), Base64.DEFAULT));
            httpConn.setRequestProperty("Authorization", basicAuth);
            httpConn.setRequestMethod("GET");


            if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                httpConn.setRequestProperty("Cookie",
                        TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
            }


            int statusCode = httpConn.getResponseCode();
            System.out.println("STATUSCODE MEDIADOWNLOAD " + String.valueOf(statusCode));
            if (statusCode == 200) {


                Map<String, List<String>> headerFields = httpConn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);


                if (cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    }
                }


                InputStream input = httpConn.getInputStream();
                OutputStream output = new FileOutputStream(directory + "/" + getFilname(m.getFilname()));

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                //Log.i("input.read(data)", String.valueOf(input.read(data)));
                while ((count = input.read(data)) != -1) {
                    //Log.i("WHILE SCHLEIFE",String.valueOf(count));
                    total += count;
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                if (mds == null) {
                    try {
                        mds = new MediaDataSource(activity);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mds.open();
                mds.saveSuccess(m);
                mds.close();
                System.out.println("** TMP save to path " + directory + "/" + getFilname(m.getFilname()));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getFilname(String pfad) {
        String[] splitResult = pfad.split("/");
        return splitResult[splitResult.length - 1];
    }

    private String md5String(String s) throws NoSuchAlgorithmException {
        String[] strArray = new String[]{s};
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        md5.update(strArray[0].getBytes());
        byte[] result = md5.digest();

        /* Ausgabe */
        StringBuffer hexString = new StringBuffer();

        for (int i = 0; i < result.length; i++) {
            if (result[i] <= 15 && result[i] >= 0) {
                hexString.append("0");
            }
            hexString.append(Integer.toHexString(0xFF & result[i]));
        }
        //System.out.println("MD5: " + hexString.toString());
        return hexString.toString();
    }


    private int quersumme(String hexString) {
        int qsumme = 0;

        int i = 0;
        while (i < hexString.length()) {
            String s = hexString.substring(i, i + 1);
            //Log.i("SUBSTRING", s);
            int zahl = Integer.parseInt(s, 16);
            //Log.i("ZAHL",String.valueOf(zahl));

            qsumme = qsumme + zahl;
            i++;
        }

        //Log.i("qsumme",String.valueOf(qsumme) );
        return qsumme;

    }
}

