package ch.zhaw.bait17.audio_signal_processing_toolbox.beat_detection;


public interface MyObservable {

    void registerObserver(MyObserver obs);
    void unregisterObserve(MyObserver obs);

}
