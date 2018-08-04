package ch.zhaw.bait17.audio_signal_processing_toolbox.metronome;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import ch.zhaw.bait17.audio_signal_processing_toolbox.R;
import ch.zhaw.bait17.audio_signal_processing_toolbox.util.Util;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;


/**
 * Implementation of a metronome with features like play, stop and changing the bpm
 * based on the android-classes {@code SoundPool} and {@code MediaPlayer}
 */

public class Metronome {

    private SoundPool mSoundPool;
    private static final String TAG = "METRNOME";
    private int soundID;
    private int [] soundIDs;
    private int streamID;
    private int bpm = 0;
    private float offset;
    private float sampleRate;
    private int loops = 0;
    private static Context context;
    private FileNameExtractor fileNameExtractor;
    private MediaPlayer mediaPlayerSong;
    private MediaPlayer mediaPlayerGenerated;

    private static Metronome metronome = null;

    private Metronome (Context context){
        this.context = context;
        mediaPlayerSong = new MediaPlayer();
        mediaPlayerGenerated = new MediaPlayer();
        fileNameExtractor = new FileNameExtractor();
        mediaPlayerGenerated.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayerGenerated.start();
            }
        });

        mediaPlayerGenerated.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayerGenerated.release();
                mediaPlayerGenerated = null;
            }
        });

        mediaPlayerSong.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int ms;
                ms = Math.round(offset/sampleRate*1000);
                Log.d(TAG, "Offset ms: " +ms);
                //mediaPlayerSong.seekTo(ms);
                mediaPlayerSong.start();
            }
        });
        mediaPlayerSong.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                Log.i("Completion Listener","Song Complete");
                mediaPlayerSong.release();
                mediaPlayerSong = null;
            }
        });
    }

    public static Metronome getInstance(Context context){
        if(metronome == null){
            metronome = new Metronome(context);
        }
        return metronome;
    }

    /**
     * Initiates the {@code SoundPool} and loads the WAV-File with the metronome in it.
     */
    public void loadSoundPool(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .build();
        } else {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        }
        soundID = mSoundPool.load(context, R.raw.bottle_120bpm_wav, 1);
    }

    /**
     * Releases the {@code SoundPool}
     */
    public void releaseSoundPool(){
        mSoundPool.release();
        mSoundPool = null;
    }


    public void setBPM(int bpm) throws Exception{
        if(bpm < 20 || bpm > 240){
            throw new Exception("Tempo ungültig!");
        }

        else{
            this.bpm = bpm;
        }
    }

    public int getBPM(){
        return bpm;
    }


    public void setLoops(int loops) {
        this.loops = loops;
    }

    public void playMetronomeToSong(){

        float rate;

        if(bpm != 0 && loops != 0 && mSoundPool != null ){
            rate = (float) bpm/120;
            streamID = mSoundPool.play(soundID,1,1,1,loops-1,rate);
        }else{
            Toast.makeText(context,"Tempo wurde nicht gesetzt", Toast.LENGTH_LONG);
            if(mSoundPool != null) {
                bpm = 120;
                streamID = mSoundPool.play(soundID, 1, 1, 1, 15, 1);
            }
        }

    }

    /**
     * Plays a metronome to the duration of the song
     * @param uri path of the song
     * @param bpm BPM of the analyzed song
     * @throws Exception throws Exception if BPM isn't set yet or if SoundPool wasn't loaded yet
     */
    public void playMetronomeToSong (String uri, int bpm) throws Exception {
        float rate;
        int loop;
        if(bpm != 0 && mSoundPool != null){
            loop = calculateLoop(uri);
            rate = (float) bpm/120;
            streamID =mSoundPool.play(soundID,1,1,1,loop,rate);
            Log.d(TAG, "Playing metronome");
        }else{
            throw new Exception("Song wasn't analyzed yet!");
        }
    }

    /**
     * Plays a metronome with song.
     * @param songURI Path of the song to play
     * @param offset Defines the duration from start of the song until first beat in amount of samples
     * @param sampleRate Sample-Rate of the song
     * @throws Exception throws Exception, if Song-File was not found or if File couldn't loaded into {@code InputStream}
     */
    public void playMetronomeWithSong (String songURI, int offset, int sampleRate) throws Exception {

        if(mediaPlayerSong == null || mediaPlayerGenerated == null){
            mediaPlayerSong = new MediaPlayer();
            mediaPlayerGenerated = new MediaPlayer();
        }
        this.offset = offset;
        this.sampleRate = sampleRate;
        FileInputStream fis = null;
        String genUri = Environment.getExternalStorageDirectory() + "/audio_signal_processing_metronome/" + fileNameExtractor.extract(songURI) + "_METRONOME.mp3";
        Uri uri = Uri.fromFile(new File(genUri));
        Log.d(TAG, "" + genUri);
        try {
            fis = (FileInputStream) Util.getInputStreamFromURI(songURI);
            mediaPlayerSong.setDataSource(fis.getFD());
            mediaPlayerGenerated.setDataSource(context,uri);
            mediaPlayerSong.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayerGenerated.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayerSong.prepareAsync();
            mediaPlayerGenerated.prepareAsync();
        }   finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    }

    /**
     * Stops playing the song and metronome in {@code MediaPlayer}
     */
    public void stopMetronomeWithSong(){
        if(mediaPlayerSong.isPlaying()){
            mediaPlayerSong.stop();
            mediaPlayerGenerated.stop();
            mediaPlayerSong.reset();
            mediaPlayerGenerated.reset();
        }
    }

    /**
     * releases the {@code MediaPlayer} instances
     */
    public void releaseMediaPlayer(){
        if(mediaPlayerSong.isPlaying()){
            mediaPlayerSong.stop();
            mediaPlayerSong.release();
            mediaPlayerSong = null;
            mediaPlayerGenerated.stop();
            mediaPlayerGenerated.release();
            mediaPlayerSong = null;
        }
    }


    /**
     * stops playing the metronome in {@code SoundPool}
     */
    public void stopMetronome(){
        mSoundPool.stop(streamID);
    }


    /**
     * Calculates how many times the metronome needs to be looped, in order to have same duration as the song.
     * @param uri Path of the song
     * @return returns the amount of loops.
     */
    public int calculateLoop(String uri){
        float duration, amountOfBeats;
        int loop;
        duration = getTotalTime(uri);
        amountOfBeats = bpm/60*duration;
        loop = (int)(amountOfBeats/4);
        return loop;
    }

    private float getTotalTime(String uri){
        Bitstream bitstream;
        Header header= null;
        FileInputStream file = null;
        try {
            file = (FileInputStream) Util.getInputStreamFromURI(uri);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        bitstream = new Bitstream(file);
        try {
            header = bitstream.readFrame();

        } catch (BitstreamException ex) {
            ex.printStackTrace();
        }
        long tn = 0;
        try {
            tn = file.getChannel().size();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //System.out.println("Chanel: " + file.getChannel().size());
        int min = header.min_number_of_frames(500);
        Log.d(TAG, "" + min);
        Log.d(TAG, "" + header.total_ms((int) tn)/1000);
        return header.total_ms((int) tn)/1000;
    }

}
