package ml.dogboy.yanius.upload;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadTask extends AsyncTask<UploadJob, Integer, String> {

    private final String url;
    private final String apikey;
    private final String boundary = "SwA"+Long.toString(System.currentTimeMillis())+"SwA";

    public UploadTask(String url, String apikey) {
        this.url = url;
        this.apikey = apikey;
    }

    @Override
    protected String doInBackground(UploadJob... params) {
        try {
            URL url = new URL(this.url + "/api/upload");

            for (UploadJob job : params) {
                if ("http://ss.example.com".equals(this.url) && "changeme".equals(this.apikey)) {
                    job.errorCallback.run(new Exception("No instance selected"));
                    continue;
                }
                if (this.apikey.length() != 64) {
                    job.errorCallback.run(new Exception("Invalid api key"));
                    continue;
                }

                Log.d("ml.dogboy.yanius", "Uploading " + job.filename);
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("User-Agent", "YaniusAndroidClient");
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                    OutputStream outputStream = connection.getOutputStream();

                    outputStream.write( ("--" + boundary + "\r\n").getBytes());
                    outputStream.write( "Content-Type: text/plain\r\n".getBytes());
                    outputStream.write( ("Content-Disposition: form-data; name=\"apikey\"\r\n").getBytes());
                    outputStream.write( ("\r\n" + this.apikey + "\r\n").getBytes());

                    outputStream.write( ("--" + boundary + "\r\n").getBytes());
                    outputStream.write( ("Content-Disposition: form-data; name=\"file\"; filename=\"" + job.filename + "\"\r\n"  ).getBytes());
                    outputStream.write( ("Content-Type: application/octet-stream\r\n"  ).getBytes());
                    outputStream.write( ("Content-Transfer-Encoding: binary\r\n"  ).getBytes());
                    outputStream.write("\r\n".getBytes());
                    outputStream.write(UploadTask.toBytes(job.stream));
                    outputStream.write("\r\n".getBytes());

                    outputStream.write( ("--" + boundary + "--\r\n").getBytes());

                    final int status = connection.getResponseCode();
                    Log.d("ml.dogboy.yanius", "Upload Status: " + status);
                    final InputStream stream = connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream();
                    String response = new String(UploadTask.toBytes(stream));
                    Log.d("ml.dogboy.yanius", "Upload Response: " + response);
                    if (status == 201) {
                        JsonObject responseObject = new Gson().fromJson(response, JsonElement.class).getAsJsonObject();
                        if (responseObject.has("url")) {
                            String uploadUrl = responseObject.get("url").getAsString();
                            job.successCallback.run(uploadUrl);
                        } else {
                            job.errorCallback.run(new Exception("No URL returned"));
                        }
                    } else {
                        job.errorCallback.run(new Exception(response));
                    }
                } catch (Exception e) {
                    Log.e("ml.dogboy.yanius", "Error during file upload", e);
                    job.errorCallback.run(e);
                }
            }
        } catch (Exception e) {
            Log.e("ml.dogboy.yanius", "Error initializing upload task", e);
        }
        return null;
    }

    private static byte[] toBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[1024];

        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

}
