package dheeraj.sachan.videoviewenc;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.BindException;
import java.util.Map;

import dheeraj.sachan.videoviewenc.nanoHTTPServer.NanoHTTPD;
import dheeraj.sachan.videoviewenc.nanoHTTPServer.Status;

/**
 * Created by dheeraj on 25/2/15.
 */
public class FileServer extends NanoHTTPD {
    private static final String TAG = FileServer.class.getSimpleName();
    public static int port = 50000;
    private HttpFileServerService httpFileServerService;
    private String data;
    private static FileServer fileServer;


    private FileServer() {
        super(port);
    }

/*    public static class Mystream extends FileInputStream {

        private FileInputStream fileInputStream;

        public Mystream(String s) throws FileNotFoundException {
            super(s);
            fileInputStream = new FileInputStream(s);
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            byte[] bytes = new byte[b.length];
            int k = fileInputStream.read(bytes, off, len);
            if (k != -1) {
                for (int j = 0; j < k; j++) {
                    if (bytes[j] == -128) {
                        b[j] = bytes[j];
                    } else {
                        b[j] = (byte) ~bytes[j];
                    }
                }
            }
            return k;
        }
    }*/


    @Override
    public Response serve(String uri, Method method,
                          Map<String, String> header, Map<String, String> parameters,
                          Map<String, String> files) {
        //Toast.makeText(context,"got request = "+uri,Toast.LENGTH_LONG).show();
        InputStream fis = null;
        try {
            fis = new FileInputStream(new File(data + File.separator + uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // video/mp2t
        if (uri.contains("file")) {
            Response response = new NanoHTTPD.Response(Status.OK, "video/mp2t", fis);
            return response;
        } else {
            Response response = new NanoHTTPD.Response(Status.OK, "vnd.apple.mpegurl", fis);
            return response;
        }

      /*  InputStream fis = null;

        try {
            fis = new Mystream(fileToServe);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new NanoHTTPD.Response(Status.OK, "application/dash-xml", fis);*/
    }

    public static void startServer(HttpFileServerService httpFileServerService, String data) {
        fileServer = new FileServer();
        fileServer.httpFileServerService = httpFileServerService;
        fileServer.data = data;
        while (true) {
            try {
                fileServer.start();
                Intent intent = new Intent();
                intent.setAction("dheeraj.sachan");
                httpFileServerService.sendBroadcast(intent);
                break;
            } catch (Exception e) {
                Log.e(TAG, "unable to start http server", e);
                if (e instanceof BindException) {
                    port = port + 1;
                } else {
                    break;
                }
            }
        }
    }

    public static void stopServer() {
        if (fileServer != null) {
            fileServer.stop();
        }
    }
}
