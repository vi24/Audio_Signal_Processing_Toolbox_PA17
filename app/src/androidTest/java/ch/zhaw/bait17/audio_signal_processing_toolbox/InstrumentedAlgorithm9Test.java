package ch.zhaw.bait17.audio_signal_processing_toolbox;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import ch.zhaw.bait17.audio_signal_processing_toolbox.beat_detection.Algorithm9Double;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device (emulator).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 * @see <a href="http://www.vogella.com/tutorials/AndroidTesting/article.html#androidtesting">Developing Android unit and instrumentation tests</a>
 *
 * For this tests to work, the music files need to be in the music library of the phone with the exact name as the uri variable
 */

@RunWith(AndroidJUnit4.class)
public class InstrumentedAlgorithm9Test {
    private static final double MAX_REL_DIFF = 0.1;

    @Test
    public void algorithm9BPM20() throws ExecutionException, InterruptedException {
        int solutionBPM = 20;
        String uri = "file:///storage/emulated/0/Music/tamborine_20bpm_1-4time_21beats_stereo.mp3";
        int result = new Algorithm9Double().execute(uri).get().get(Algorithm9Double.BPM_KEY);

        Assert.assertEquals(solutionBPM, result);
    }

    @Test
    public void algorithm9BPM60() throws ExecutionException, InterruptedException {
        int solutionBPM = 60;
        String uri = "file:///storage/emulated/0/Music/tamborine_60bpm_1-4time_61beats_stereo.mp3";
        int result = new Algorithm9Double().execute(uri).get().get(Algorithm9Double.BPM_KEY);

        Assert.assertEquals(solutionBPM, result);
    }

    @Test
    public void algorithm9BPM100() throws ExecutionException, InterruptedException {
        int solutionBPM = 100;
        String uri = "file:///storage/emulated/0/Music/tamborine_100bpm_1-4time_101beats_stereo.mp3";
        int result = new Algorithm9Double().execute(uri).get().get(Algorithm9Double.BPM_KEY);

        Assert.assertEquals(solutionBPM, result);
    }
    @Test
    public void algorithm9BPM120() throws ExecutionException, InterruptedException {
        int solutionBPM = 120;
        String uri = "file:///storage/emulated/0/Music/tamborine_120bpm_1-4time_121beats_stereo.mp3";
        int result = new Algorithm9Double().execute(uri).get().get(Algorithm9Double.BPM_KEY);

        Assert.assertEquals(solutionBPM, result);
    }
    @Test
    public void algorithm9BPM200() throws ExecutionException, InterruptedException {
        int solutionBPM = 200;
        String uri = "file:///storage/emulated/0/Music/tamborine_200bpm_1-4time_201beats_stereo.mp3";
        int result = new Algorithm9Double().execute(uri).get().get(Algorithm9Double.BPM_KEY);

        Assert.assertEquals(solutionBPM, result);
    }

    @Test
    public void algorithm9BPM240() throws ExecutionException, InterruptedException {
        int solutionBPM = 240;
        String uri = "file:///storage/emulated/0/Music/tamborine_240bpm_1-4time_241beats_stereo.mp3";
        int result = new Algorithm9Double().execute(uri).get().get(Algorithm9Double.BPM_KEY);

        Assert.assertEquals(solutionBPM, result);
    }

    @Test
    public void algorithm9SongBPM200() throws ExecutionException, InterruptedException {
        int solutionBPM = 200;
        String uri = "file:///storage/emulated/0/Music/TeddybearsSthlm_HeyBoy_trimmedToOneMinute.mp3";
        int result = new Algorithm9Double().execute(uri).get().get(Algorithm9Double.BPM_KEY);

        Assert.assertTrue((result >= solutionBPM- MAX_REL_DIFF *solutionBPM) && (result <= solutionBPM+(MAX_REL_DIFF)*solutionBPM));
    }

    @Test
    public void algorithm9SongBPM120() throws ExecutionException, InterruptedException {
        int solutionBPM = 122;

        String uri = "file:///storage/emulated/0/Music/Maroon5_Sugar_drum_Cover_trimmed.mp3";
        int result = new Algorithm9Double().execute(uri).get().get(Algorithm9Double.BPM_KEY);

        Assert.assertTrue((result >= solutionBPM- MAX_REL_DIFF *solutionBPM) && (result <= solutionBPM+(MAX_REL_DIFF)*solutionBPM));
    }



}
