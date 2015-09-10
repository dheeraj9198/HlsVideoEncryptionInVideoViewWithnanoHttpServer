package dheeraj.sachan.videoviewenc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class HttpFileServerService extends Service {
    public HttpFileServerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FileServer.stopServer();
        String data = intent.getStringExtra("path");
        FileServer.startServer(HttpFileServerService.this,data);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        FileServer.stopServer();
        super.onDestroy();
    }
}
