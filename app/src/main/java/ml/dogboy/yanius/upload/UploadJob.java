package ml.dogboy.yanius.upload;

import java.io.InputStream;

public class UploadJob {

    protected String filename;
    protected InputStream stream;
    protected Callback<String> successCallback;
    protected Callback<Exception> errorCallback;

    public UploadJob(String filename, InputStream stream, Callback<String> successCallback, Callback<Exception> errorCallback) {
        this.filename = filename;
        this.stream = stream;
        this.successCallback = successCallback;
        this.errorCallback = errorCallback;
    }

    public interface Callback<T> {
        void run(T param);
    }

}
