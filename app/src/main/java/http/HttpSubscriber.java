package http;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;

/**
 * Created by Seven on 2016/5/24.
 * 根据Http code反馈给用户的信息
 */
public abstract class HttpSubscriber<T> extends Subscriber<T> {

    //对应HTTP的状态码
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int REQUEST_TIMEOUT = 408;
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int GATEWAY_TIMEOUT = 504;

    @Override
    public void onError(Throwable throwable) {
        APiException ex;
        if (throwable instanceof HttpException) {
            HttpException exception = (HttpException) throwable;
            ex = new APiException(throwable, exception.code());
            switch (exception.code()) {
                case UNAUTHORIZED:
                case FORBIDDEN:
                    ex.setErrorMessage("获取数据失败，没有连接网络的权限");//可能用户禁止了网络连接的权限
                    onError(ex);
                    break;
                case NOT_FOUND:
                case REQUEST_TIMEOUT:
                case GATEWAY_TIMEOUT:
                case INTERNAL_SERVER_ERROR:
                case BAD_GATEWAY:
                case SERVICE_UNAVAILABLE:
                default:
                    ex.setErrorMessage("获取数据失败,请检查网络是否连接成功");  //均视为网络错误
                    onError(ex);
                    break;
            }
        } else if (throwable instanceof UnknownHostException) {
            ex = new APiException(throwable, APiException.UNKONOWNHOST);
            ex.setErrorMessage("获取数据失败,请检查网络是否连接成功");
            onError(ex);
        } else if (throwable instanceof SocketTimeoutException) {
            ex = new APiException(throwable, REQUEST_TIMEOUT);
            ex.setErrorMessage("请求超时，请检查网络是否连接成功");
            onError(ex);
        } else if (throwable instanceof IllegalStateException) {
            ex = new APiException(throwable, APiException.UNKNOWN);
            ex.setErrorMessage("网络连接超时");
            onError(ex);
        } else if (throwable instanceof NullPointerException) {
            ex = new APiException(throwable, APiException.UNKNOWN);
            ex.setErrorMessage(throwable.getMessage());
            throwable.printStackTrace();
            onError(ex);
        } else {
            ex = new APiException(throwable, APiException.UNKNOWN);
            ex.setErrorMessage("发生未知错误");
            ex.printStackTrace();
            onError(ex);
        }
    }

    /**
     * 错误回调
     */
    protected abstract void onError(APiException ex);

}
