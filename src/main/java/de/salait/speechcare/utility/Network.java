package de.salait.speechcare.utility;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import de.salait.speechcare.data.Media;

/**
 * Created by Christian.Ramthun on 23.10.13.
 */
public class Network {
    public static final int STATUSCODE_200 = 200;
    public static final int STATUSCODE_400 = 400;
    public static final int STATUSCODE_404 = 404;
    public static final String DOMAIN_ADMIN = "http://admin.speechcare.de/";
    public static final String DOMAIN_PLUS = "https://plus.speechcare.de/";
    public static final String DOMAIN_PREVIEW = "https://preview.speechcare.de/";
    public static final String DOMAIN = DOMAIN_PLUS;
    /*public static final String authUsername = "speechcare";
    public static final String authPassword = "kugelblitz";*/
    public static final String authUsername = "speechcare";
    public static final String authPassword = "scportal";
    static final String COOKIES_HEADER = "Set-Cookie";
    private static final String secret = "g9od1m3uv.Z%$#R4KT3C!b7Ny@x=0&r!u4Y+T#]GCWko5<X_73TD9u8FWU%=7}v";
    private static final String URL_doHandshake = "handshake/begin";
    private static final String URL_verifyHandshake = "handshake/verify/?response=";
    private static final String URL_login = "user/login";
    private static final String URL_updateCollection = "service/UpdateCollection";
    private static final String URL_exportCollection = "service/ExportCollection";
    private static final String releaspackageUrl = "releasepackage/export/";
    private static final String mediaBaseUrl = "media/download/";
    static java.net.CookieManager msCookieManager = new java.net.CookieManager();
    private static String releasepackageID; // 6=AphasieLite , 1=AphasieVollVersion
    private final Activity activity;
    private final File directory;

    public Network(Activity act) {
        this.activity = act;
        if (msCookieManager.getCookieStore().getCookies().size() == 0) {
            handShake();
        }
        ContextWrapper contextWrapper = new ContextWrapper(activity);
        directory = contextWrapper.getFilesDir();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * prueft, ob das Geraet eine Netzwerkverbindung herstellen kann
     *
     * @param context
     * @return
     */
    public static boolean isNetworkOnline(Context context) {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;
    }

    public boolean downloadMedia(Media m) {
        try {


            URL myURL = new URL(DOMAIN + mediaBaseUrl + m.getId());
            HttpURLConnection httpConn = (HttpURLConnection) myURL.openConnection();
            String credentials = authUsername + ":" + authPassword;
            String basicAuth = "Basic " + new String(Base64.encode(credentials.getBytes(), Base64.DEFAULT));
            httpConn.setRequestProperty("Authorization", basicAuth);
            httpConn.setRequestMethod("GET");
            addCookieToGetRequest(httpConn);


            int statusCode = httpConn.getResponseCode();
            if (statusCode == Network.STATUSCODE_200) {


                InputStream input = httpConn.getInputStream();
                OutputStream output = new FileOutputStream(directory + "/" + getFilname(m.getFilname()));

                byte data[] = new byte[1024];
                int count;

                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                return true;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    public HttpURLConnection exportCollection(String collectionID) {

        String url = DOMAIN + URL_exportCollection + collectionID;
        HttpURLConnection httpConn = null;
        try {

            URL myURL = new URL(url);
            httpConn = (HttpURLConnection) myURL.openConnection();
            String credentials = authUsername + ":" + authPassword;
            String basicAuth = "Basic " + new String(Base64.encode(credentials.getBytes(), Base64.DEFAULT));
            httpConn.setRequestProperty("Authorization", basicAuth);
            httpConn.setRequestMethod("GET");
            addCookieToGetRequest(httpConn);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return httpConn;
    }

    /**
     * asks for updated collection info on user<br>
     * - AppId<br>
     * - Device UUID<br>
     * - Username
     *
     * @param updateColObj
     * @return Statuscode of transmission
     */
    public HttpURLConnection updateCollection(JSONObject updateColObj) {
        return post(updateColObj, DOMAIN + URL_updateCollection);
    }

    /**
     * loggs in user<br>
     * - Username<br>
     * - Password<br>
     * - Device<br>
     * - - Devicename<br>
     * - - OS<br>
     * - - Description<br>
     * {"Password":"Ihr Passwort","Username":"Ihr Benutzername","Device":{"Devicename":"29341625-2AA7-4B53-8764-97AC1D0C7DB0","OS":"iPad 6.1.3","Description":""}}<br>
     *
     * @return Statuscode of transmission
     **/
    public int loginUser(JSONObject jsonLoginObj) {

        HttpURLConnection httpConn = post(jsonLoginObj, DOMAIN + URL_login);
        try {
            return httpConn.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 400;
    }

    private HttpURLConnection post(JSONObject postObj, String url) {
        HttpURLConnection httpConn = null;
        try {
            URL myURL = new URL(url);
            httpConn = (HttpURLConnection) myURL.openConnection();
            String credentials = authUsername + ":" + authPassword;
            String basicAuth = "Basic " + new String(Base64.encode(credentials.getBytes(), Base64.DEFAULT));
            httpConn.setRequestProperty("Authorization", basicAuth);

            httpConn.setRequestProperty("Accept-Encoding", "gzip");
            httpConn.setRequestProperty("Connection", "close");
            httpConn.setRequestMethod("POST");
            addCookieToGetRequest(httpConn);
            httpConn.setDoOutput(true);

            String se = postObj.toString();
            OutputStream os = httpConn.getOutputStream();
            byte[] input = se.getBytes("utf-8");
            os.write(input, 0, input.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return httpConn;
    }

    public void handShake() {
        System.out.println("<< NETWORK METHOD handShake ");
        try {
            doHandshake();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doHandshake() throws IOException, NoSuchAlgorithmException {


        String url = DOMAIN + URL_doHandshake;
        URL myURL = new URL(url);
        System.out.println("**** myURL " + myURL);
        HttpURLConnection httpConn = (HttpURLConnection) myURL.openConnection();
        System.out.println("**** httpConn ");
        String credentials = authUsername + ":" + authPassword;
        String basicAuth = "Basic " + new String(Base64.encode(credentials.getBytes(), Base64.DEFAULT));
        httpConn.setRequestProperty("Authorization", basicAuth);
        httpConn.setRequestMethod("GET");
        int statusCode = httpConn.getResponseCode();

        System.out.println("<< NETWORK doHandshake StatusCode " + statusCode);
        if (statusCode == 200) {

            Map<String, List<String>> headerFields = httpConn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
            setCookie(cookiesHeader);


            BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpConn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String entityStr = response.toString();
            String hexs = md5String(entityStr + quersumme(entityStr) + secret);
            verifyHandshake(hexs);
        } else {
            // TODO ERROR HANDLINGLRS
        }

    }

    private void setCookie(List<String> cookiesHeader) {

        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
            }
        }
    }

    private void addCookieToGetRequest(HttpURLConnection httpConn) {

        if (msCookieManager.getCookieStore().getCookies().size() > 0) {
            // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
            httpConn.setRequestProperty("Cookie",
                    TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
        }

    }

    public void addCookieToPostRequest(HttpURLConnection httpConn) {

        if (msCookieManager.getCookieStore().getCookies().size() > 0) {
            // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
            httpConn.setRequestProperty("Cookie",
                    TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
        }
    }

    public HttpURLConnection postRequestWithCookie(HttpURLConnection httpConn) {
        if (msCookieManager.getCookieStore().getCookies().size() > 0) {
            // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
            httpConn.setRequestProperty("Cookie",
                    TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
        }
        return httpConn;
    }

    private void verifyHandshake(String hexstring) throws IOException {


        try {

            URL myURL = new URL(DOMAIN + URL_verifyHandshake + hexstring);
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

            } else {
                // TODO ERROR HANDLING
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        return hexString.toString();
    }

    private int quersumme(String hexString) {
        int qsumme = 0;
        int i = 0;

        while (i < hexString.length()) {
            String s = hexString.substring(i, i + 1);
            int zahl = Integer.parseInt(s, 16);
            qsumme = qsumme + zahl;
            i++;
        }
        //Log.i("qsumme",String.valueOf(qsumme) );
        return qsumme;
    }

    private String getFilname(String pfad) {
        String[] splitResult = pfad.split("/");
        /*String filename =splitResult[splitResult.length-1];
        //Log.i("filname ",filename);
        return filename;*/
        return splitResult[splitResult.length - 1];
    }
}
