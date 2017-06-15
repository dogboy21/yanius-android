package ml.dogboy.yanius.upload;

public class UploadedItem {

    private String fileName;
    private int fileSize;
    private String url;
    private boolean succeeded;
    private boolean uploading;

    public UploadedItem(String fileName, int fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.succeeded = false;
        this.uploading = false;
    }

    public String getFileName() {
        return this.fileName;
    }

    public int getFileSize() {
        return this.fileSize;
    }

    public boolean isSucceeded() {
        return this.succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public boolean isUploading() {
        return this.uploading;
    }

    public void setUploading(boolean uploading) {
        this.uploading = uploading;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
