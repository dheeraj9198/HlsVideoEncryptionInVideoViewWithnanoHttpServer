package dheeraj.sachan.videoviewenc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by dheeraj on 10/9/15.
 */
public class CustomHttpStartedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String s = intent.getAction();
        if (intent.getAction() != null && intent.getAction().equals("dheeraj.sachan")) {
            synchronized (MainActivity.TAG) {
                MainActivity.TAG.notify();
            }
        }
    }
}
