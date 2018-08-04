package ch.zhaw.bait17.audio_signal_processing_toolbox.metronome;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SequenceInputStream;

import ch.zhaw.bait17.audio_signal_processing_toolbox.util.Util;

/**
 * Appends the generated MP3-File on the already generated MP3-File
 * (Concatenating the same generated MP3-File several times)
 * At the end the resulting MP3-File should have same duration as the song.
 */

public class MP3Appender {

    private static MP3Appender instance = new MP3Appender();
    private FileNameExtractor fileNameExtractor;
    private String folderName = "/audio_signal_processing_metronome/";

    private MP3Appender(){
        fileNameExtractor = new FileNameExtractor();
    }
    public static MP3Appender getInstance(){
        return instance;
    }


    /**
     * Appends the generated MP3-File on the already generated MP3-File
     * @param loop how many times the generated MP3-File should be appended on generated MP3-File (concatenating same file)
     * @param uri path of the song
     * @throws Exception throws Exception if the parameters aren't set.
     */
    public void appendMP3(int loop, String uri) throws Exception {

        if(loop == 0) throw new Exception("Amount of loops wasn't set yet");

        if(uri == null) throw new Exception("Path of the song wasn't set yet");

        FileInputStream fisToFinal = null;
        FileOutputStream fos = null;
        File mergedFile = new File(Environment.getExternalStorageDirectory() + folderName + fileNameExtractor.extract(uri)+"_METRONOME.mp3");
        File inputFile = new File(Environment.getExternalStorageDirectory() + folderName + "encoded.mp3");
        mergedFile.createNewFile();
        try {
            fos = new FileOutputStream(mergedFile);
            fisToFinal = new FileInputStream(mergedFile);
            for (int i = 1; i <= loop; i++) {
                FileInputStream fisSong = (FileInputStream) Util.getInputStreamFromURI("file:/storage/emulated/0" + folderName + "encoded.mp3");
                SequenceInputStream sis = new SequenceInputStream(fisToFinal, fisSong);
                byte[] buf = new byte[1024];
                int offset = 0;
                try {
                    for (int readNum; (readNum = fisSong.read(buf)) != -1; ){
                        fos.write(buf, offset, readNum);
                    }
                } finally {
                    if (fisSong != null) {
                        fisSong.close();
                    }
                    if (sis != null) {
                        sis.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                    inputFile.delete();
                }
                if (fisToFinal != null) {
                    fisToFinal.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




}
