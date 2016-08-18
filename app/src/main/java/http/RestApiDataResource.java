package http;


import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Seven on 2016/5/4.
 * 主要网络请求 Rxjava + retrofit+gson
 */
public class RestApiDataResource {

    private OpenComApiService mOpenComApi;

    public RestApiDataResource() {
        Retrofit retrofitAdapter = new Retrofit.Builder()
                .baseUrl("http://route.showapi.com/213-4/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(OkHttpUtil.getInstance().getClient())
                .build();
        mOpenComApi = retrofitAdapter.create(OpenComApiService.class);
    }

    public OpenComApiService getmOpenComApi() {
        return mOpenComApi;
    }


}
