package ch.zhaw.bait17.audio_signal_processing_toolbox.beat_detection;

import java.util.HashMap;

/**
 * Created by Matthias on 02.12.2017.
 */

public interface MyObserver {

    void updateOnResult(HashMap<String, Integer> value);
    void updateOnProgress(String value);
}
