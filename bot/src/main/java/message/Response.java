package main.java.message;

/**
 * Created by Dick Zhou on 4/7/2017.
 * General api response.
 */
public class Response<T> {

    private Status status;
    private T data;

    public Response(Status status, T data) {
        this.status = status;
        this.data = data;
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public enum Status {
        ok,
        unsupported,
        wait,
        error
    }
}
