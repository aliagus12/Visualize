package android.aliagushutapea.com.visualizerpreference;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class VisualizerActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

    private static final int MY_PERMISSION_RECORD_AUDIO_REQUEST_CODE = 101;
    private static final int REQUEST_FILE_CODE = 101;
    private VisualizerView mVisualizerView;
    private AudioInputReader mAudioInputReader;
    ImageView mImageView;
    TextView textView;
    private Uri uri;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizer);
        mVisualizerView = findViewById(R.id.activity_visualizer);
        mImageView = findViewById(R.id.img_play);
        textView = findViewById(R.id.txtTitle);
        sharedPreferences = getSharedPreferences("file", MODE_PRIVATE);
        mImageView.setOnClickListener(this);
        setupSharedPreferences();
        setupPermissions();
    }

    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mVisualizerView.setShowBass(sharedPreferences.getBoolean(getString(R.string.pref_show_bass_key),
                getResources().getBoolean(R.bool.pref_show_bass_default)));
        mVisualizerView.setShowMid(sharedPreferences.getBoolean(getString(R.string.pref_show_mid_range_key),
                getResources().getBoolean(R.bool.pref_show_mid_range_default)));
        mVisualizerView.setShowTreble(sharedPreferences.getBoolean(getString(R.string.pref_show_treble_key),
                getResources().getBoolean(R.bool.pref_show_treble_default)));
        loadColorFromPreferences(sharedPreferences);
        loadSizeFromSharedPreferences(sharedPreferences);

        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void loadColorFromPreferences(SharedPreferences sharedPreferences) {
        mVisualizerView.setColor(sharedPreferences.getString(getString(R.string.pref_color_key),
                getString(R.string.pref_color_red_value)));
    }

    private void loadSizeFromSharedPreferences(SharedPreferences sharedPreferences) {
        float minSize = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_size_key),
                getString(R.string.pref_size_default)));
        mVisualizerView.setMinSizeScale(minSize);
    }


    // Updates the screen if the shared preferences change. This method is required when you make a
    // class implement OnSharedPreferenceChangedListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_show_bass_key))) {
            mVisualizerView.setShowBass(sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_show_bass_default)));
        } else if (key.equals(getString(R.string.pref_show_mid_range_key))) {
            mVisualizerView.setShowMid(sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_show_mid_range_default)));
        } else if (key.equals(getString(R.string.pref_show_treble_key))) {
            mVisualizerView.setShowTreble(sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_show_treble_default)));
        } else if (key.equals(getString(R.string.pref_color_key))) {
            loadColorFromPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_size_key))) {
            float minSize = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_size_key), "1.0"));
            mVisualizerView.setMinSizeScale(minSize);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Methods for setting up the menu
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our visualizer_menu layout to this menu */
        inflater.inflate(R.menu.visualizer_menu, menu);
        /* Return true so that the visualizer_menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Below this point is code you do not need to modify; it deals with permissions
     * and starting/cleaning up the AudioInputReader
     **/

    /**
     * onPause Cleanup audio stream
     **/
    @Override
    protected void onPause() {
        super.onPause();
        if (mAudioInputReader != null) {
            mAudioInputReader.shutdown(isFinishing());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAudioInputReader != null) {
            String uriString = sharedPreferences.getString("uri", "");
            if (!uriString.equals("")) {
                textView.setSelected(true);
                textView.setText(uriString);
                uri = Uri.parse(uriString);
            }
            mAudioInputReader.restart(uri);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAudioInputReader != null) {
            mAudioInputReader.shutdown(isFinishing());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onStop();
    }

    /**
     * App Permissions for Audio
     **/
    private void setupPermissions() {
        // If we don't have the record audio permission...
        boolean isGrantedAudio = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean isGrantedExternal = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!isGrantedAudio && !isGrantedExternal) {
            // And if we're on SDK M or later...
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Ask again, nicely, for the permissions.
                String[] permissionsWeNeed = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissionsWeNeed, MY_PERMISSION_RECORD_AUDIO_REQUEST_CODE);
            }
        } else {
            // Otherwise, permissions were granted and we are ready to go!
            mAudioInputReader = new AudioInputReader(mVisualizerView, this);
            //onResume();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_RECORD_AUDIO_REQUEST_CODE: {
                    boolean isPermitted = true;
                    for (int granted : grantResults) {
                        if (granted == PackageManager.PERMISSION_DENIED) {
                            isPermitted = false;
                        }
                    }
                // If request is cancelled, the result arrays are empty.
                if (isPermitted) {
                    // The permission was granted! Start up the visualizer!
                    mAudioInputReader = new AudioInputReader(mVisualizerView, this);
                    //onResume();
                } else {
                    Toast.makeText(this, "Permission for audio not granted. Visualizer can't run.", Toast.LENGTH_LONG).show();
                    finish();
                    // The permission was denied, so we can show a message why we can't run the app
                    // and then close the app.
                }
            }
            // Other permissions could go down here

        }
    }

    @Override
    public void onClick(View view) {
        if (mAudioInputReader != null) {
            mAudioInputReader.shutdown(isFinishing());
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_FILE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_FILE_CODE:
                if (data != null) {
                    uri = data.getData();
                    sharedPreferences.edit()
                            .putString("uri", String.valueOf(uri))
                            .apply();
                    onResume();
                }
                break;
        }
    }
}