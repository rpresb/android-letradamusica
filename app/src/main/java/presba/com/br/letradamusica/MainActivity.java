package presba.com.br.letradamusica;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity {
    TextView status = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");

            Log.d("Music", cmd + " : " + action);

            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");
            boolean playing = intent.getBooleanExtra("playing", false);

            Log.d("Music", artist + " : " + album + " : " + track);

            if (!playing) {
                status.setText("Nenhuma música tocando");
            } else {
                status.setText(artist + "\n" + album + "\n" + track);

                String uri = Uri.parse("http://api.vagalume.com.br/search.php")
                        .buildUpon()
                        .appendQueryParameter("mus", track)
                        .appendQueryParameter("art", artist)
                        .build().toString();

                if (uri != null) {
                    new VagalumeAsyncTask().execute(uri);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter iF = new IntentFilter();
        iF.addAction("com.android.music.musicservicecommand");
        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.android.music.playstatechanged");
        iF.addAction("com.android.music.updateprogress");

        this.status = (TextView) this.findViewById(R.id.status);

        registerReceiver(mReceiver, iF);

        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (!manager.isMusicActive()) {
            status.setText("Nenhuma música tocando");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
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

    private class VagalumeAsyncTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONParser jParser = new JSONParser();
            return jParser.getJSONFromUrl(params[0]);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            super.onPostExecute(json);

            try {
                JSONObject mus = (JSONObject) json.getJSONArray("mus").get(0);
                status.setText(mus.getString("text"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
