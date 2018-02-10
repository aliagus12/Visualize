package android.aliagushutapea.com.visualizerpreference;

/**
 * Created by ali on 10/02/18.
 */

import android.content.Context;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Build;


/**
 * {@link AudioInputReader} sets up and tears down the {@link MediaPlayer} and {@link Visualizer}
 */

public class AudioInputReader {

    private final VisualizerView mVisualizerView;
    private final Context mContext;
    private Uri uri;
    private MediaPlayer mPlayer;
    private Visualizer mVisualizer;


    public AudioInputReader(VisualizerView visualizerView, Context context) {
        this.mVisualizerView = visualizerView;
        this.mContext = context;
    }

    public void initVisualizer() {
        // Setup media player
        if (uri != null) {
            mPlayer = MediaPlayer.create(mContext, uri);
        } else {
            mPlayer = MediaPlayer.create(mContext, R.raw.htmlthesong);
        }
        mPlayer.setLooping(true);

        // Setup the Visualizer
        // Connect it to the media player
        mVisualizer = new Visualizer(mPlayer.getAudioSessionId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mVisualizer.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS);
            mVisualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
        }

        // Set the size of the byte array returned for visualization
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
        // Whenever audio data is available, update the visualizer view
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {

                        // Do nothing, we are only interested in the FFT (aka fast Fourier transform)
                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] bytes, int samplingRate) {
                        // If the Visualizer is ready and has data, send that data to the VisualizerView
                        if (mVisualizer != null && mVisualizer.getEnabled()) {
                            mVisualizerView.updateFFT(bytes);
                        }

                    }
                },
                Visualizer.getMaxCaptureRate(), false, true);

        // Start everything
        mVisualizer.setEnabled(true);
        mPlayer.start();
    }

    public void shutdown(boolean isFinishing) {
        if (mPlayer != null) {
            mPlayer.pause();
            if (isFinishing) {
                mVisualizer.release();
                mPlayer.release();
                mPlayer = null;
                mVisualizer = null;
            }
        }

        if (mVisualizer != null) {
            mVisualizer.setEnabled(false);
        }
    }

    public void restart(Uri uri) {
        this.uri = uri;
        if (mPlayer != null) {
            mPlayer.stop();
        }
        initVisualizer();
    }
}
