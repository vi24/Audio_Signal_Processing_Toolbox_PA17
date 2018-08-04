package ch.zhaw.bait17.audio_signal_processing_toolbox.metronome;

import android.os.Environment;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.zhaw.bait17.audio_signal_processing_toolbox.androidlame.AndroidLame;
import ch.zhaw.bait17.audio_signal_processing_toolbox.androidlame.LameBuilder;
import ch.zhaw.bait17.audio_signal_processing_toolbox.androidlame.WaveReader;

/**
 * <p>
 * Encoding the metronome as a MP3-File. based on the <a href="http://lame.sourceforge.net/">LAME Encoder</a>
 * For changing the speed (BPM) of the existing WAV-File with metronome, which gets decoded by {@code WaveReader},
 * the sample-rate of the metronome gets multiplied by the proportion of the song-BPM and the BPM of the WAV-File metronome
 * The resulting sample-rate relates to the BPM of the song. The decoded WAV-File metronome will be encoded with the new sample-rate and thus
 * the MP3-File metronome has the BPM of the song.
 * </p>
 *
 *
 */

public class MP3Encoder {

    private BufferedOutputStream outputStreamBuffered;
    private static final int OUTPUT_STREAM_BUFFER = 8192;
    private WaveReader waveReader;
    private String fileName = "/audio_signal_processing_metronome/";

    private static MP3Encoder MP3Encoder = new MP3Encoder();

    private MP3Encoder(){}

    public static MP3Encoder getInstance(){
        return MP3Encoder;
    }

    /**
     * Encodes the WAV-File metronome to a MP3-File metronome with adjusted BPM
     * @param bpm BPM of the song
     * @param offset Offset of the song (offset=duration from start of the song until first beat)
     * @param inputStream {@code InputStream} for the WAV-File metronome
     * @throws Exception throws Exception if bpm isn't correct or if {@code InputStream} isn't set yet.
     */
    public void encode(int bpm, int offset, InputStream inputStream) throws Exception{
        if(bpm < 60 || bpm > 240){
            throw new Exception("BPM is invalid");
        }
        if(inputStream == null){
            throw new Exception("Inputstream is null!");
        }

        else{
            File input = null;
            File output = null;
            if(offset > 0){
                input = new File(Environment.getExternalStorageDirectory() + fileName +"empty.wav");
                output = new File(Environment.getExternalStorageDirectory() + fileName +"empty.mp3");
            }else{
                input = new File(Environment.getExternalStorageDirectory() + fileName +"bottle.wav");
                output = new File(Environment.getExternalStorageDirectory() + fileName +"encoded.mp3");
            }


            ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
            int size = 0;

            byte[] buffer = new byte[1024];
            while((size=inputStream.read(buffer,0,1024))>=0){
                outputStream.write(buffer,0,size);
            }
            inputStream.close();
            buffer=outputStream.toByteArray();

            FileOutputStream fos = new FileOutputStream(input);
            fos.write(buffer);
            fos.close();
            outputStream.close();


            int CHUNK_SIZE = 8192;


            waveReader = new WaveReader(input);

            try {
                waveReader.openWave();
            } catch (WaveReader.InvalidWaveException e) {
                e.printStackTrace();
            }


            AndroidLame androidLame = null;
            if(offset > 0){
                float ms = Math.round(offset/48000);
                float factor = 10000/ms;
                androidLame = new LameBuilder()
                        .setInSampleRate(Math.round((waveReader.getSampleRate()*factor)))
                        .setOutChannels(waveReader.getChannels())
                        .setOutBitrate(128)
                        .setOutSampleRate(waveReader.getSampleRate())
                        .setQuality(5)
                        .build();
            }else{
                androidLame = new LameBuilder()
                        .setInSampleRate(waveReader.getSampleRate() * bpm / 120)
                        .setOutChannels(waveReader.getChannels())
                        .setOutBitrate(128)
                        .setOutSampleRate(waveReader.getSampleRate())
                        .setQuality(5)
                        .build();
            }

            try {
                outputStreamBuffered = new BufferedOutputStream(new FileOutputStream(output), OUTPUT_STREAM_BUFFER);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int bytesRead = 0;

            short[] buffer_l = new short[CHUNK_SIZE];
            short[] buffer_r = new short[CHUNK_SIZE];
            byte[] mp3Buf = new byte[CHUNK_SIZE];

            int channels = waveReader.getChannels();


            while (true) {
                try {
                    if (channels == 2) {

                        bytesRead = waveReader.read(buffer_l, buffer_r, CHUNK_SIZE);

                        if (bytesRead > 0) {

                            int bytesEncoded = 0;
                            bytesEncoded = androidLame.encode(buffer_l, buffer_r, bytesRead, mp3Buf);

                            if (bytesEncoded > 0) {
                                try {

                                    outputStreamBuffered.write(mp3Buf, 0, bytesEncoded);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        } else break;
                    } else {

                        bytesRead = waveReader.read(buffer_l, CHUNK_SIZE);

                        if (bytesRead > 0) {
                            int bytesEncoded = 0;
                            bytesEncoded = androidLame.encode(buffer_l, buffer_l, bytesRead, mp3Buf);

                            if (bytesEncoded > 0) {
                                try {
                                    outputStreamBuffered.write(mp3Buf, 0, bytesEncoded);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else break;
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            int outputMp3buf = androidLame.flush(mp3Buf);


            if (outputMp3buf > 0) {
                try {
                    outputStreamBuffered.write(mp3Buf, 0, outputMp3buf);
                    outputStreamBuffered.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            input.delete();
        }

    }


}

