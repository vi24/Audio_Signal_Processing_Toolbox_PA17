package ch.zhaw.bait17.audio_signal_processing_toolbox.metronome;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.InputStream;

import ch.zhaw.bait17.audio_signal_processing_toolbox.R;

/**
 * Creates {@code AsyncTask}s for encoding the MP3-File and appending the MP3-Files
 */

public class AsyncMP3Generator {


    private AsyncMP3Generator(){
    }

    private static AsyncMP3Generator asyncMP3Generator = new AsyncMP3Generator();

    public static AsyncMP3Generator getInstance(){
        return asyncMP3Generator;
    }

    public void generateMp3File(int bpm, Context context,int offset, TextView beatView,  int loop, String uri){
        EncodeMP3Task encodeMP3 = new EncodeMP3Task();
        encodeMP3.execute(bpm,context,offset, beatView,loop,uri);
    }

    private static class EncodeMP3Task extends AsyncTask<Object, Void, Void> {


        TextView beatView;
        int loop;
        String uri;
        @Override
        protected Void doInBackground(Object... params) {
            MP3Encoder encoder = MP3Encoder.getInstance();
            int bpm = (Integer) params[0];
            Context context = (Context) params[1];
            int offset = (Integer) params [2];
            beatView = (TextView) params[3];
            loop = (Integer)params[4];
            uri = (String) params[5];

            InputStream input1 = context.getResources().openRawResource(R.raw.bottle_120bpm_wav);
            //InputStream input2 = context.getResources().openRawResource(R.raw.empty);

            try {
                //encoder.encode(bpm,offset,input2); offset isn't working yet
                encoder.encode(bpm,0, input1);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            beatView.setText("Generating mp3-File...");
        }

        @Override
        protected void onPostExecute(Void avoid) {
            MP3AppenderTask mp3AppenderTask = new MP3AppenderTask();
            mp3AppenderTask.execute(loop,beatView,uri);
        }
    }

    private static class MP3AppenderTask extends AsyncTask<Object, Void, Void> {

        TextView beatView;
        @Override
        protected Void doInBackground(Object... objects) {
            MP3Appender mp3Appender = MP3Appender.getInstance();
            int loop = (Integer) objects[0];
            beatView = (TextView) objects[1];
            String uri = (String) objects[2];
            try {
                mp3Appender.appendMP3(loop,uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            beatView.setText("Appending mp3-Files...");
        }

        @Override
        protected void onPostExecute(Void avoid) {
            beatView.setText("Finished generating mp3-File!");
        }
    }

}
