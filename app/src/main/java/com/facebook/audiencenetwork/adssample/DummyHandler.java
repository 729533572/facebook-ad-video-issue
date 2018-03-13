package com.facebook.audiencenetwork.adssample;

import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Created by jitesh on 10/03/18.
 */

public class DummyHandler implements URLStreamHandlerFactory {

    private static final String TAG = "DUMMY_HANDLER";

    private URLStreamHandler originalHttpHanlder;
    private URLStreamHandler originalHttpsHanlder;
    private Method originalHttpOpenConnection;
    private Method originalHttpsOpenConnection;

    public DummyHandler() {
        setOriginalHandlers();
        Class<?>[] parameters = {URL.class};
        try {
            this.originalHttpOpenConnection = originalHttpHanlder.getClass()
                    .getDeclaredMethod("openConnection", parameters);
            this.originalHttpOpenConnection.setAccessible(true);

            this.originalHttpsOpenConnection = originalHttpsHanlder.getClass()
                    .getDeclaredMethod("openConnection", parameters);

            this.originalHttpsOpenConnection.setAccessible(true);

        } catch (NoSuchMethodException e) {
            if (this.originalHttpOpenConnection != null) {
                this.originalHttpsOpenConnection = this.originalHttpOpenConnection;
            }
        }
    }

    private static URLStreamHandler getURLStreamHandler(String url) {
        try {
            String fieldName = "streamHandler";
            if (Build.VERSION.CODENAME.equalsIgnoreCase("N") || Build.VERSION.SDK_INT > 23) {
                fieldName = "handler";
            }

            URL urlObject = new URL(url);
            Field f = urlObject.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (URLStreamHandler) f.get(urlObject);
        } catch (Exception e) {
            Log.e(TAG, "couldn't get URLStreamHandler for: " + url);
            return null;
        }
    }

    private void setOriginalHandlers() {
        originalHttpHanlder = getURLStreamHandler("http://www.google.com");
        System.out.println("set original http handler to ["
                + originalHttpHanlder.getClass().getCanonicalName() + "]");

        originalHttpsHanlder = getURLStreamHandler("https://www.google.com");
        System.out.println("set original https handler to ["
                + originalHttpsHanlder.getClass().getCanonicalName() + "]");

        if (originalHttpHanlder == null || originalHttpsHanlder == null) {
            throw new RuntimeException("failed to set original handlers");
        }
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equalsIgnoreCase("http")
                || protocol.equalsIgnoreCase("https")) {
            return new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL url) throws IOException {
                    try {
//                        Thread.sleep(1000); // introduce delay
                        boolean isHTTPS = url.toString().toLowerCase(java.util.Locale.ENGLISH).startsWith("https");
                        URLConnection fallback = null;
                        fallback = isHTTPS ? (URLConnection) originalHttpsOpenConnection.invoke(originalHttpsHanlder, url)
                                : (URLConnection) originalHttpOpenConnection.invoke(originalHttpHanlder, url);
//                        Thread.sleep(1000); // introduce delay
                        return fallback;
                    } catch (Exception e) {
                        throw new IOException(e);
                    }
                }
            };
        }
        return null;
    }
}
