package http;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


/**
 * Created by Seven on 2016/5/4.
 * 新的网络架构代替旧的Apache
 */
public class OkHttpUtil {
    private final static int DEFAULT_CONN_TIMEOUT = 45; //45s
    private final static int WRITE_TIMEOUT = 10 * 60; //10分钟
    private final static int CACHE_SIZE = 20 * 1024 * 1024;//20mb
    private static volatile OkHttpUtil Instance = null;


    private OkHttpClient client;

    private OkHttpUtil() {
//        File cacheDirectory = new File(MainApplication.getContext().getCacheDir(), "HttpResponseCache");
//        Cache cache = new Cache(cacheDirectory, CACHE_SIZE);
        client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONN_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_CONN_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
//                .addNetworkInterceptor(new StethoInterceptor())
//                .cache(cache)
                .build();
    }


    public static OkHttpUtil getInstance() {
        if (Instance == null) {
            synchronized (OkHttpUtil.class) {
                if (Instance == null) {
                    Instance = new OkHttpUtil();
                }
            }
        }
        return Instance;
    }

    public OkHttpClient getClient() {
        return client;
    }


}
