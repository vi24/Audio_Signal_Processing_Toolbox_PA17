package ch.zhaw.bait17.audio_signal_processing_toolbox.beat_detection;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ch.zhaw.bait17.audio_signal_processing_toolbox.decoder.MP3Decoder;
import ch.zhaw.bait17.audio_signal_processing_toolbox.fft.FFT;
import ch.zhaw.bait17.audio_signal_processing_toolbox.util.PCMUtil;
import ch.zhaw.bait17.audio_signal_processing_toolbox.util.Util;

public class Algorithm9Double extends AsyncTask<String, String, HashMap<String, Integer>> implements MyObservable {
    private static final String TAG = "ALG9";
    private static final int SECONDS_OF_SAMPLE_MAX = 10;
    private static final double THRESHOLD = 0.90;
    private static final int MIN_FREQUENCY = 0;
    private static final int MAX_FREQUENCY = 1500;
    private static final double MIN_BPM_CERTAINTY = 0.60;
    private static final int MIN_THRESHOLD_DISTANCE_TO_NEXT = 5000;
    private static final int MONO = 1;
    private static final int STEREO = 2;
    private static final double MIN_LENGTH_LAST_SAMPLE_BLOCK_RELATIVE = 0.09;

    public static final String BPM_KEY = "bpm";
    public static final String OFFSET_KEY = "offset";
    public static final String SAMPLE_RATE_KEY = "sampleRate";
    public static final String CHANNELS_KEY = "channels";

    private MP3Decoder mp3Decoder;
    private List<MyObserver> observerList = new ArrayList<>();
    private HashMap<String, Integer> resultMap = new HashMap<>();
    private String URI;
    private boolean offsetSet = false;



    
    @Override
    protected HashMap<String, Integer> doInBackground(String ... uri){
        if(uri == null ||uri.length == 0){
            throw new IllegalArgumentException("Only one argument allowed:\n" + "arguemtns: " + uri.length +" : " + uri);
        }
        this.URI = uri[0];
        Integer bpm = calculateBPM();

        resultMap.put(BPM_KEY, bpm);
        return resultMap;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if(values != null && values.length > 0){
            notifyObserversOnProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(HashMap<String, Integer> resultMap) {
        notifyObserversOnResult(resultMap);

    }

    public int getOffSet() {
        int offset = resultMap.get(Algorithm9Double.OFFSET_KEY);
        return offset;
    }

    private void notifyObserversOnResult(HashMap<String, Integer> resultMap) {
        Log.d(TAG, "Start updating listener with result");
        for(MyObserver obs: observerList){
            obs.updateOnResult(resultMap);
        }
    }

    private void notifyObserversOnProgress(String notifyValue) {
        Log.d(TAG, "Start updating listener with progress");

        for(MyObserver obs: observerList){
            obs.updateOnProgress(notifyValue);
        }
    }

    private Integer calculateBPM() {
        ArrayList<HashMap<Integer, Double>> listOfBPMMaps = new ArrayList<>();
        mp3Decoder = prepareMP3Decoder();
        if(mp3Decoder == null){
            return null;
        }

        publishProgress("calculating BPM for first 10 seconds");

        HashMap<Integer, Double> firstMap = calculate10SecondBPMMap();
        printMap(firstMap, "firstMap");
        listOfBPMMaps.add(firstMap);
        int tempBestBeat = findBestBeat(firstMap);
        Double tempBestChance = firstMap.get(tempBestBeat);
        if (tempBestChance > MIN_BPM_CERTAINTY) {
            return tempBestBeat;
        }
        publishProgress("calculating BPM for second 10 seconds");

        CalculateNext10SecondMap(listOfBPMMaps);

        HashMap<Integer, Double> summedUpBPM = unifyAll10SecondsMaps(listOfBPMMaps);
        TreeMap<Integer, Double> sortedAllMultipleBPMs = countAllOccuringBeatsWithMultiplication(summedUpBPM);

        return findBestBeat(sortedAllMultipleBPMs);
    }

    // Calculates the possible BPM Maps all remaining 10 second parts of the song
    // Important: It adds a map the the given arrayList
    private void CalculateNext10SecondMap(ArrayList<HashMap<Integer, Double>> listOfBPMMaps) {
        HashMap<Integer, Double> nextMap = calculate10SecondBPMMap();
        while (nextMap != null && nextMap.size() != 0) {

            printMap(nextMap, "nextmap");

            HashMap<Integer, Double> copy = new HashMap<>(nextMap);
            copy.putAll(nextMap);
            listOfBPMMaps.add(nextMap);
            publishProgress("calculating BPM for the" + (listOfBPMMaps.size()+1) +". 10 second part");

            nextMap = calculate10SecondBPMMap();
        }
    }

    // Unifies the List of BPM maps by adding all possible BPMs and their adjusted probability to a new Map
    @NonNull
    private HashMap<Integer, Double> unifyAll10SecondsMaps(ArrayList<HashMap<Integer, Double>> listOfBPMMaps) {
        HashMap<Integer, Double> summedUpBPM = new HashMap<>();
        for (HashMap<Integer, Double> myMap : listOfBPMMaps) {
            for (Integer key : myMap.keySet()) {
                if (summedUpBPM.containsKey(key)) {
                    summedUpBPM.put(key, summedUpBPM.get(key) + myMap.get(key) * 1.0 / listOfBPMMaps.size());
                } else {
                    summedUpBPM.put(key, myMap.get(key) * 1.0 / listOfBPMMaps.size());
                }
            }
        }
        return summedUpBPM;
    }

    // BPM = key, occurences of BPM = value
    // Takes all BPMs in the Map and multiplies it up to the value of 480 BPM. for each occurence of the BPM key, add one the its value
    @NonNull
    private TreeMap<Integer, Double> countAllOccuringBeatsWithMultiplication(HashMap<Integer, Double> summedUpBPM) {
        TreeMap<Integer, Double> sortedAllMultipleBPMs = new TreeMap<>();
        for (Integer key : summedUpBPM.keySet()) {
            int counter = 1;
            int BPM = key*counter;
            while (BPM < 480) {
                if (sortedAllMultipleBPMs.containsKey(BPM)) {
                    sortedAllMultipleBPMs.put(BPM, sortedAllMultipleBPMs.get(BPM) + 1.0);
                } else {
                    sortedAllMultipleBPMs.put(BPM, 1.0);
                }
                counter++;
                BPM = counter * key;
            }

        }
        printMap(sortedAllMultipleBPMs, "allMultiple");
        return sortedAllMultipleBPMs;
    }

    private void printMap(Map<Integer, Double> myMap, String name) {
        Log.d(TAG,"##################");
        Log.d(TAG,name);

        for (Integer key : myMap.keySet()) {
            Log.d(TAG, key + " : " + myMap.get(key));
        }
    }


    private HashMap<Integer, Double> calculate10SecondBPMMap() {


        if (mp3Decoder == null) {
            return null;
        }

        resultMap.put(SAMPLE_RATE_KEY, mp3Decoder.getSampleRate());
        resultMap.put(CHANNELS_KEY, mp3Decoder.getChannels());

        short[] samples = prepareSamples();
        if (samples == null) {
            return null;
        }
        Log.d(TAG,"samples size: " + samples.length);
        double[] filteredIFFTSamples = calculateRawValues(samples, mp3Decoder.getSampleRate());
        if (filteredIFFTSamples == null) {
            return null;
        }

        return processValues(filteredIFFTSamples);
    }

    private double[] calculateRawValues(short[] samples, int sampleRate) {

        double[] doubleSamples = PCMUtil.short2DoubleArray(samples);
        int n = (int) Math.pow(2, Math.ceil(Math.log(doubleSamples.length) / Math.log(2)));
        if (n == 0) {
            return null;
        }

        doubleSamples = Util.absoluteArray(doubleSamples);

        FFT fft = new FFT(n);
        double[] fftSamples = fft.getForwardTransformNoWindow(doubleSamples);

        double[] fftXValues = createfftXArray(sampleRate, fftSamples.length);
        double[] bandFilteredFFTSamples =
                applyBandPassFilter(fftXValues, fftSamples, MIN_FREQUENCY, MAX_FREQUENCY);
        double[] bandFilteredIFFTSamples = fft.getIFFT(bandFilteredFFTSamples);
        bandFilteredIFFTSamples = Util.absoluteArray(bandFilteredIFFTSamples);
        double[] normalizedIFFTSamples = Util.normalizeArray(bandFilteredIFFTSamples);
        return Util.valuesAboveThreshold(normalizedIFFTSamples, THRESHOLD,
                MIN_THRESHOLD_DISTANCE_TO_NEXT);


    }

    private HashMap<Integer, Double> processValues(double[] filteredIFFTSamples) {
        Integer[] peakSamples = samplesOfPeaks(filteredIFFTSamples);
        Integer[] peakDistances = distanceBetweenPeaks(peakSamples);
        if(!offsetSet){
            if(mp3Decoder.getChannels() == 1) {
                resultMap.put(OFFSET_KEY, peakDistances[0]);
            } else {
                resultMap.put(OFFSET_KEY, peakDistances[0]/2);
            }
        }
        return calculateBPMChances(peakDistances);
    }


    private MP3Decoder prepareMP3Decoder() {
        try {
            InputStream inputStream = Util.getInputStreamFromURI(URI);
            MP3Decoder mp3Decoder = MP3Decoder.getInstance();
            mp3Decoder.setSource(inputStream);
            if (mp3Decoder.isInitialised()) {

                return mp3Decoder;
            } else {
                return null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Generates Samples from mp3File
    private short[] prepareSamples() {
        int requiredSamples = SECONDS_OF_SAMPLE_MAX * mp3Decoder.getSampleRate();



        if (!mp3Decoder.isInitialised()) {
            Log.d(TAG, "mp3decoder not init");
        }
        short[] samples = new short[requiredSamples*2];
        int pointer = 0;
        short[] tempSamples = mp3Decoder.getNextSampleBlock();
        while (tempSamples != null && pointer <= requiredSamples) {
            tempSamples = filterSamplesPerChannel(tempSamples, mp3Decoder.getChannels());

            pointer += tempSamples.length;
            // Log.d(TAG, "currentLength: " + pointer);
            System.arraycopy(tempSamples, 0, samples, pointer, tempSamples.length);
            tempSamples = mp3Decoder.getNextSampleBlock();
        }


        short[] shortenedSamples = new short[pointer];

        System.arraycopy(samples, 0, shortenedSamples, 0, pointer);

        if (shortenedSamples.length < MIN_LENGTH_LAST_SAMPLE_BLOCK_RELATIVE * requiredSamples) {
            return null;
        }
        return shortenedSamples;
    }

    private short[] filterSamplesPerChannel(short[] samples, int mode) {
        short[] tempSampleBlock = null;

        if (mode == MONO) {
            int length = samples.length;
            int newLength = length / 2;
            tempSampleBlock = new short[newLength];
            System.arraycopy(samples, 0, tempSampleBlock, 0, newLength);
        } else if (mode == STEREO) {

            tempSampleBlock = samples;
        }
        return tempSampleBlock;
    }

    private double[] applyBandPassFilter(double[] fftXValues, double[] fftYValues, int minFrequency,
                                         int maxFrequency) {

        int minIndex = 0;
        int maxIndex = 0;
        double[] bandFilteredArray = new double[fftYValues.length];

        for (int i = 0; i < fftXValues.length; i++) {
            if (fftXValues[i] > minFrequency) {
                minIndex = i;
                break;
            }
        }
        for (int i = minIndex; i < fftXValues.length; i++) {
            if (fftXValues[i] > maxFrequency) {
                maxIndex = i;
                break;
            }
        }
        for (int i = 0; i < bandFilteredArray.length; i++) {
            if (i < minIndex) {
                bandFilteredArray[i] = 0;

            } else if (i >= minIndex && i <= maxIndex) {
                bandFilteredArray[i] = fftYValues[i];

            } else if (i > maxIndex) {
                bandFilteredArray[i] = 0;

            }

        }


        return bandFilteredArray;
    }

    private double[] createfftXArray(int samplesPerSecond, int nextPowerOfTwoOfFFTYArray) {
        double xMax = samplesPerSecond / 2 - samplesPerSecond / nextPowerOfTwoOfFFTYArray;
        double xMin = 0;
        double step = samplesPerSecond / (double) nextPowerOfTwoOfFFTYArray;


        int countSteps = (int) Math.floor(xMax / step);
        double[] xFFTArray = new double[countSteps];

        for (int i = 0; i <= countSteps - 1; i++) {
            xFFTArray[i] = xMin + i * step;
        }
        return xFFTArray;
    }

    //Takes filtered samples and collects the indexes of peaks.
    private Integer[] samplesOfPeaks(double[] samples) {
        ArrayList<Integer> peaks = new ArrayList<>();

        for (int i = 0; i < samples.length; i++) {
            if (samples[i] != 0) {
                peaks.add(i);
            }
        }

        return peaks.toArray(new Integer[] {});
    }

    //Takes indexes of peak samples and calculates their distance to the next one
    private Integer[] distanceBetweenPeaks(Integer[] peakSamples) {
        ArrayList<Integer> peakList = new ArrayList<>();
        peakList.add(peakSamples[0]);
        for (int i = 1; i < peakSamples.length; i++) {
                peakList.add(peakSamples[i] - peakSamples[i - 1]);
        }

        return peakList.toArray(new Integer[] {});
    }

    //Takes distances between peaks and calculates the possible BPM Values
    private HashMap<Integer, Double> calculateBPMChances(Integer[] peakDistances) {
        Integer[] peaksWithoutFirst = new Integer[peakDistances.length - 1];
        System.arraycopy(peakDistances, 1, peaksWithoutFirst, 0, peaksWithoutFirst.length);
        HashMap<Integer, Double> bpmProbabilites = new HashMap<>();
        for (int i = 0; i < peaksWithoutFirst.length; i++) {

            Integer bpmKey = Math.round(Float.valueOf(48000) / peaksWithoutFirst[i] * 60);

            if (bpmProbabilites.containsKey(bpmKey)) {
                bpmProbabilites.put(bpmKey, bpmProbabilites.get(bpmKey) + 1);
            } else {
                bpmProbabilites.put(bpmKey, 1.0);
            }
        }

        double part = 1.0 / peaksWithoutFirst.length;
        for (Integer key : bpmProbabilites.keySet()) {
            bpmProbabilites.put(key, part * bpmProbabilites.get(key));

        }
        return bpmProbabilites;
    }

    private int findBestBeat(Map<Integer, Double> possibleBeats) {

        Integer keyWithMaxValue = (Integer) possibleBeats.keySet().toArray()[0];

        for (Integer key : possibleBeats.keySet()) {
            if (possibleBeats.get(key) > possibleBeats.get(keyWithMaxValue)) {
                keyWithMaxValue = key;
            }
        }

        return keyWithMaxValue;
    }



    @Override
    public void registerObserver(MyObserver obs) {
        observerList.add(obs);
    }

    @Override
    public void unregisterObserve(MyObserver obs) {
        observerList.remove(obs);
    }
}
