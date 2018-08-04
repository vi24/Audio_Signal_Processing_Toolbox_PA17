

import java.io.InputStream;

/**
 * An interface representing an audio decoder.
 *
 * @author georgrem, stockan1
 */

public interface AudioDecoder {

    /**
     * Returns the next PCM sample block or null if end of stream is reached.
     *
     * @return a block of PCM samples
     */
    
    short[] getNextSampleBlock();

    /**
     * Returns the sample rate.
     *
     * @return the sample rate
     */
    int getSampleRate();

    /**
     * Returns the number of channels.
     *
     * @return the number of channels
     */
    int getChannels();

    /**
     * Sets the audio source to decode.
     *
     * @param is    an {@code InputStream}
     */
    void setSource( InputStream is);

    /**
     * Returns true if the decoder is ready for decoding.
     *
     * @return
     */
    boolean isInitialised();

}
