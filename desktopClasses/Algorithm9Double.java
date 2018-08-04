


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.ShortBuffer;
import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import javax.tools.Tool;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;



public class Algorithm9Double {
  private final String URI;
  // "file:///storage/emulated/0/Music/tamborine_20bpm_1-4time_21beats_stereo.mp3"
  private static final int SIZE_MP3 = 5765000;
  private static final String TAG = "ALG9";
  private static final int SECONDS_OF_SAMPLE_MAX = 10;
  private static final double THRESHOLD = 0.90;
  private final int MIN_FREQUENCY;
  private final int MAX_FREQUENCY;
  private static final int ROUND_TO_NEAREST = 1000;
  private static final int SAMPLE_RATE = 48000;
  private static final double MIN_BPM_CERTAINTY = 0.60;
  private static final int MIN_TRHESHOLD_DISTANCE_TO_NEXT = 5000;
  private double certainty;
  private static final int MONO = 1;
  private static final int STEREO = 2;
  private static final double MIN_LENGTH_LAST_SAMPLE_BLOCK_RELATIVE = 0.09;
  private MP3Decoder mp3Decoder;


  public Algorithm9Double(String URItoFile, int minFrequency, int maxFrequency) {
    this.URI = URItoFile;
    this.MAX_FREQUENCY = maxFrequency;
    this.MIN_FREQUENCY = minFrequency;
  }

  public static void main(String[] args) {
    String bpm60 =
        "C:\\Users\\Matthias\\StudioProjects\\Audio_Signal_Processing_Toolbox\\documents\\audioFiles\\tamborine_60bpm_1-4time_61beats_stereo.mp3";
    String sugar =
        "C:\\Users\\Matthias\\StudioProjects\\Audio_Signal_Processing_Toolbox\\documents\\audioFiles\\Maroon5_Sugar_drum_Cover_trimmed.mp3";
    String teddy =
        "C:\\Users\\Matthias\\StudioProjects\\Audio_Signal_Processing_Toolbox\\documents\\audioFiles\\TeddybearsSthlm_HeyBoy_trimmedToOneMinute.mp3";
    // String uriAndroid =
    // "C:\\Users\\Matthias\\Desktop\\tamborine_240bpm_1-4time_241beats_stereo.mp3";
    Algorithm9Double algorithm9Double = new Algorithm9Double(teddy, 0, 1500);
    int beat = algorithm9Double.calculateBPM();
    System.out.println(beat);

  }



  public int calculateBPM() {
    ArrayList<HashMap<Integer, Double>> listOfBPMMaps = new ArrayList<>();

    HashMap<Integer, Double> firstMap = calculate10SecondBPMMap();
    printMap(firstMap, "firstMap");
    listOfBPMMaps.add(firstMap);
    int tempBestBeat = findBestBeat(firstMap);
    Double tempBestChance = firstMap.get(tempBestBeat);
    if (tempBestChance > MIN_BPM_CERTAINTY) {
      return tempBestBeat;
    }

    HashMap<Integer, Double> nextMap = calculate10SecondBPMMap();

    while (nextMap != null && nextMap.size() != 0) {
      printMap(nextMap, "nextmap");

      HashMap<Integer, Double> copy = new HashMap<>(nextMap);
      copy.putAll(nextMap);
      listOfBPMMaps.add(nextMap);
      tempBestBeat = findBestBeat(nextMap);
      tempBestChance = nextMap.get(tempBestBeat);
      nextMap = calculate10SecondBPMMap();
    }

    listOfBPMMaps.remove(listOfBPMMaps.size() - 1);
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
    printMap(summedUpBPM, "sumAll10Seconds");


    TreeMap<Integer, Double> sortedAllMultipleBPMs = new TreeMap<>();
    for (Integer key : summedUpBPM.keySet()) {
      int counter = 1;
      //int BPM = (((key + 5) / 10) * 10) * counter;
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
    Map<Integer, Double> weightedMap = weightPossibleBPM(20, 480, 1, sortedAllMultipleBPMs);
    printMap(weightedMap, "weightedMap");
    // TODO add divider map and possible round possible bpms beforehand
    // TODO add samplesd from last black when reading repeatedly
    System.out.println(findBestBeat(weightedMap));


    return -1;
  }

  private void printMap(Map<Integer, Double> myMap, String name) {
    System.out.println("##################");
    System.out.println(name);

    for (Integer key : myMap.keySet()) {
      System.out.println(key + " : " + myMap.get(key));
    }
  }
//
//  private void printMap(TreeMap<Integer, Double> myMap, String name) {
//    System.out.println("##################");
//    System.out.println(name);
//
//    for (Integer key : myMap.keySet()) {
//      System.out.println(key + " : " + myMap.get(key));
//    }
//  }
//

  private HashMap<Integer, Double> calculate10SecondBPMMap() {

    if (mp3Decoder == null) {
      mp3Decoder = prepareMP3Decoder();
    }
    if (mp3Decoder == null) {
      return null;
    }
    // Log.d(TAG, "channels: " + mp3Decoder.getChannels());

    short[] samples = prepareSamples();
    if (samples == null) {
      return null;
    }
    System.out.println("samples size: " + samples.length);
    double[] filteredIFFTSamples = caluclateRawValues(samples, mp3Decoder.getSampleRate());
    if (filteredIFFTSamples == null) {
      return null;
    }

    return processValues(filteredIFFTSamples);
  }


  public double getCertainty() {
    return certainty;
  }


  private double[] caluclateRawValues(short[] samples, int sampleRate) {

    // Log.d(TAG, "samples before shortening:" + samples.length);
    // samples = removeEveryOtherSignal(samples);
    double[] doubleSamples = PCMUtil.short2DoubleArray(samples);
    // Log.d(TAG, "samples after shortening:" + samples.length);
    int n = (int) Math.pow(2, Math.ceil(Math.log(doubleSamples.length) / Math.log(2)));
    if (n == 0) {
      return null;
    }
    // Log.d(TAG, "n: " + n);

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
        MIN_TRHESHOLD_DISTANCE_TO_NEXT);


  }

  private HashMap<Integer, Double> processValues(double[] filteredIFFTSamples) {
    Integer[] peakSamples = samplesOfPeaks(filteredIFFTSamples);
    Integer[] peakDistances = distanceBetweenPeaks(peakSamples);
    return calculateBPMChances(peakDistances);
  }


  private MP3Decoder prepareMP3Decoder() {
    try {
      InputStream inputStream = Util.getInputStreamFromURI(URI);
      MP3Decoder mp3Decoder = MP3Decoder.getInstance();
      mp3Decoder.setSource(inputStream);
      if (mp3Decoder.isInitialised()) {
        // Log.d(TAG, "mp3Decoder ready :D");
        return mp3Decoder;
      } else {
        // Log.d(TAG, "mp3Decoder not ready");
        return null;
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }


  private short[] prepareSamples() {
    int requiredSamples = SECONDS_OF_SAMPLE_MAX * mp3Decoder.getSampleRate();
    // Log.d(TAG, "Samples required for" + SECONDS_OF_SAMPLE_MAX + " seconds: " + requiredSamples);
    // Log.d(TAG, "sampleRate: " + mp3Decoder.getSampleRate());


    if (!mp3Decoder.isInitialised()) {
      // Log.d(TAG, "mp3decoder not init");
    }
    short[] samples = new short[SIZE_MP3];
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
    // Log.d(TAG, "Size of timeDomainSampes: " + shortenedSamples.length);

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
    // Log.d(TAG, "startedMethod applyBandPassFilter with: minFrequency: " + minFrequency + " ,
    // maxFrequency:" + maxFrequency);
    // Log.d(TAG, "length of fftYValues = " + fftYValues.length);
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

      } else {
        // Log.d(TAG, "outside of min and Max Index in Bandfilter");
      }

    }
    // Log.d(TAG, "minIndex: " + minIndex);
    // Log.d(TAG, "maxIndex: " + maxIndex);

    return bandFilteredArray;
  }

  private double[] createfftXArray(int samplesPerSecond, int nextPowerOfTwoOfFFTYArray) {
    // Log.d(TAG, "startedMethod createFFTXArray with: samplesPersond: " + samplesPerSecond + " ,
    // nextPowerOFTwoOFFtArray:" + nextPowerOfTwoOfFFTYArray);
    double xMax = samplesPerSecond / 2 - samplesPerSecond / nextPowerOfTwoOfFFTYArray;
    double xMin = 0;
    double step = samplesPerSecond / (double) nextPowerOfTwoOfFFTYArray;
    // Log.d(TAG, "xMax: " + xMax);
    // Log.d(TAG, "step: " + step);

    int countSteps = (int) Math.floor(xMax / step);
    // Log.d(TAG, "countSteps for fftXValues: " + countSteps);
    double[] xFFTArray = new double[countSteps];

    for (int i = 0; i <= countSteps - 1; i++) {
      xFFTArray[i] = xMin + i * step;
    }
    return xFFTArray;
  }


  private Integer[] samplesOfPeaks(double[] samples) {
    ArrayList<Integer> peaks = new ArrayList<>();

    for (int i = 0; i < samples.length; i++) {
      if (samples[i] != 0) {
        peaks.add(i);
      }
    }

    // Log.d(TAG, "amountOfPeaks: " + peaks.size());
    for (int i = 0; i < peaks.size(); i++) {
      // Log.d(TAG, "peaksample: " + peaks.get(i));
    }

    return peaks.toArray(new Integer[] {});

  }


  private Integer[] distanceBetweenPeaks(Integer[] peakSamples) {
    ArrayList<Integer> peakList = new ArrayList<>();
    peakList.add(peakSamples[0]);
    for (int i = 1; i < peakSamples.length; i++) {
      peakList.add(peakSamples[i] - peakSamples[i - 1]);
    }

    return peakList.toArray(new Integer[] {});
  }


  private HashMap<Integer, Double> calculateBPMChances(Integer[] peaks) {
    Integer[] peaksWithoutFirst = new Integer[peaks.length - 1];
    System.arraycopy(peaks, 1, peaksWithoutFirst, 0, peaksWithoutFirst.length);
    HashMap<Integer, Double> bpmProbabilites = new HashMap<>();
    for (int i = 0; i < peaksWithoutFirst.length; i++) {

      Integer bpmKey = Math.round(Float.valueOf(SAMPLE_RATE) / peaksWithoutFirst[i] * 60);

      if (bpmProbabilites.containsKey(bpmKey)) {
        bpmProbabilites.put(bpmKey, bpmProbabilites.get(bpmKey) + 1);
      } else {
        bpmProbabilites.put(bpmKey, 1.0);
      }
    }

    double part = 1.0 / peaksWithoutFirst.length;
    for (Integer key : bpmProbabilites.keySet()) {
      bpmProbabilites.put(key, part * bpmProbabilites.get(key));

      // Log.d(TAG, "BPM: " + key + " probability: " + bpmProbabilites.get(key));
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

    this.certainty = possibleBeats.get(keyWithMaxValue);
    return keyWithMaxValue;
  }

  private TreeMap<Integer, Double> weightPossibleBPM(int minBPM, int maxBPM, double base,
      Map<Integer, Double> mapToApplyWindow) {
    TreeMap<Integer, Double> copy = new TreeMap<>(mapToApplyWindow);
    copy.putAll(mapToApplyWindow);
    double pi = Math.PI;

    int newDistance = maxBPM - minBPM;
    double quot = newDistance / pi;
    double factor = 1 / quot;

    for (Integer key : copy.keySet()) {
      if (key < minBPM || key > maxBPM) {
          copy.put(key, copy.get(key)*base);
      } else {
        copy.put(key, copy.get(key)*(Math.sin(factor)+base));
      } 
    }


    return copy;
  }


}
