package dheeraj.sachan.videoviewenc;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private VideoView videoView;
    private Button button;
    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*        final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Toast.makeText(MainActivity.this, "exception : " + (ex.getMessage() == null ? "null" : ex.getMessage()), Toast.LENGTH_LONG).show();
                uncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });*/

        setContentView(R.layout.activity_main);
        videoView = (VideoView) findViewById(R.id.video_view);
        mediaController = new MediaController(MainActivity.this);
        mediaController.setAnchorView(videoView);
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            Intent intent = new Intent(MainActivity.this, HttpFileServerService.class);
                            stopService(intent);
                            intent.putExtra("path", new File(Environment.getExternalStorageDirectory(), "Download" + File.separator + "dheeraj").getAbsolutePath());
                            startService(intent);

                            synchronized (TAG) {
                                try {
                                    TAG.wait();
                                } catch (Exception e) {

                                }
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            try {
/*
                                videoView.setVideoURI(Uri.parse("http://localhost:" + FileServer.port + "/playlist.m3u8"));
*/
                                videoView.setVideoURI(Uri.parse("http://localhost:" + FileServer.port + "/bideo.mp4"));
                                videoView.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, "", e);
                            } finally {
                                videoView.requestFocus();
                            }
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                 /*   videoView.setVideoURI(Uri.parse("http://playertest.longtailvideo.com/adaptive/oceans_aes/oceans_aes.m3u8"));
                    videoView.requestFocus();*/
                } catch (Exception e) {
                    Log.e("", "");
                }
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                String data = what + "" + extra;
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
