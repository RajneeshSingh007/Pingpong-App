package com.pingpong.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pingpong.Config;
import com.pingpong.utils.MyAppClass;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    public static final String API_URL_EXTENSION = "/v100/";
    private static Retrofit retrofit;
    private static Retrofit mlmRetrofit;
    private static Retrofit otherRetrofit;

    public static OkHttpClient.Builder getUnsafeOkHttpClient(boolean addCustom)  {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder client = new OkHttpClient.Builder()
                        .readTimeout(40, TimeUnit.SECONDS)
                //.addInterceptor(logging)
                .connectTimeout(40, TimeUnit.SECONDS);
        //if(addCustom){
          //client.addInterceptor(new BasicAuthInterceptor(API_USER_NAME, API_PASSWORD));
        //}
        try {
            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", addCustom ? MyAppClass.getVipsCertificate() : MyAppClass.getCertificate());

            // Create a TrustManager that trusts the CAs in our KeyStore.
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
            trustManagerFactory.init(keyStore);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            X509TrustManager x509TrustManager = (X509TrustManager) trustManagers[0];

            // Create an SSLSocketFactory that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            client .sslSocketFactory(sslSocketFactory,x509TrustManager);
        }catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return client;
    }

    public static OkHttpClient.Builder getUnsafeOkHttpClientMlm(boolean addCustom)  {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .readTimeout(40, TimeUnit.SECONDS)
                 //.addInterceptor(logging)
                .connectTimeout(40, TimeUnit.SECONDS);
        //if(addCustom){
        //client.addInterceptor(new BasicAuthInterceptor(API_USER_NAME, API_PASSWORD));
        //}
        try {
            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", MyAppClass.getVipsCertificate());

            // Create a TrustManager that trusts the CAs in our KeyStore.
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
            trustManagerFactory.init(keyStore);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            X509TrustManager x509TrustManager = (X509TrustManager) trustManagers[0];

            // Create an SSLSocketFactory that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            //client .sslSocketFactory(sslSocketFactory,x509TrustManager);
        }catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return client;
    }

    public static Retrofit getRetrofitInstance() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Config.API_SERVER_URL + API_URL_EXTENSION)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(getUnsafeOkHttpClient(false).build())
                   // .client(client)
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getRetrofitMLMInstance() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        if (mlmRetrofit == null) {
            mlmRetrofit = new Retrofit.Builder()
                    .baseUrl(Config.MLM_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(getUnsafeOkHttpClientMlm(true).build())
                    // .client(client)
                    .build();
        }
        return mlmRetrofit;
    }

    public static Retrofit getOtherRetrofitInstance() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        if (otherRetrofit == null) {
            otherRetrofit = new Retrofit.Builder()
                    .baseUrl(Config.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(getUnsafeOkHttpClient(false).build())
                    .build();
        }
        return otherRetrofit;
    }



}
