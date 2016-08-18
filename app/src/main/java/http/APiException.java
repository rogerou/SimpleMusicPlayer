package http;

/**
 * Created by Seven on 2016/5/24.
 * 用户返回现实错误信息和Code
 */
public class APiException extends Exception {

    private int code;
    private String errorMessage;
    public final static int UNKNOWN = 777;
    public final static int UNKONOWNHOST = 888;
    public final static int DATABASEERROR = 999;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


    public APiException(Throwable throwable, int code) {
        super(throwable);
        this.code = code;
    }

    public APiException(Throwable throwable, String errorMessage) {
        super(throwable);
        this.errorMessage = errorMessage;
    }
}
