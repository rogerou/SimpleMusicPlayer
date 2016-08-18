package http;


import com.rogerou.simpemusicplayer.SongList;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by Seven on 2016/5/4.
 * 使用Retrofit APi管理
 */
public interface OpenComApiService {


    @POST()
    @FormUrlEncoded
    Observable<SongList> getSongList(@Url String url, @Field("topid") int type, @Field("showapi_appid") String appid, @Field("showapi_sign") String sign);


}


    
