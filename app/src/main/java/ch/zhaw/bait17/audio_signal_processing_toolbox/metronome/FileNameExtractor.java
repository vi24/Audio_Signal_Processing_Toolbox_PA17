package ch.zhaw.bait17.audio_signal_processing_toolbox.metronome;

import org.apache.commons.io.FilenameUtils;


/**
 * Extracts the filename from the path.
 * Also removes the data-extension like .mp3, .wav, .docx etc.
 */

public class FileNameExtractor {


    public FileNameExtractor() {}

    /**
     * Extracts the filename from the path and removes the data-extension
     * @param uri path
     * @return filename
     */
    public String extract (String uri){
        String filename = uri.substring(uri.lastIndexOf('/') + 1);
        filename = FilenameUtils.removeExtension(filename);
        return filename;
    }
}
