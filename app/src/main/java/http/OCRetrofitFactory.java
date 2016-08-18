package http;

/**
 * Created by Seven on 2016/5/4.
 * <p/>
 * 一个获取Api的工厂类
 */
public class OCRetrofitFactory {

    static volatile OpenComApiService openComApiService = null;

    /**
     * 获取基本数据内容
     *
     * @return
     */
    public static OpenComApiService getOpenComApiService() {
        if (openComApiService == null) {
            synchronized (OCRetrofitFactory.class) {
                if (openComApiService == null) {
                    openComApiService = new RestApiDataResource().getmOpenComApi();
                }
            }

        }
        return openComApiService;
    }

}