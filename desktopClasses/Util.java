


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * <p>
 *     A general utility class.
 * </p>
 *
 * @author georgrem, stockan1
 */

public class Util {

    /**
     * Returns an InputStream of the resource specified in the parameter.
     *
     * @param uri                       the resource you want to read from
     * @return                          an InputStream
     * @throws FileNotFoundException    Throws an exception if the file cannot be found
     */
    public static InputStream getInputStreamFromURI( String uri)
            throws FileNotFoundException {
      File file = new File(uri);
        return new FileInputStream(file);
    }

    /**
     * Returns a ByteArrayInputStream containing the passed byte array as its internal buffer.
     *
     * @param data  a byte array
     * @return      InputStream
     */
    public static InputStream getInputStreamFromByteArray(byte[] data) {
        return new ByteArrayInputStream(data);
    }

    /**
     * Computes and returns the greatest common divisor (gcd) of two 32-bit integers.
     *
     * @param n     first integer
     * @param m     second integer
     * @return      gcd of n and m
     */
    public static int gcd(int n, int m) {
        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        } else if (n < m) {
            return gcd(m % n, n);
        } else {
            return gcd(n % m, m);
        }
    }

    /**
     * Computes the font size based on the text appearance.
     *
     * @param idTextAppearance  a text appearance value
     * @return                  font size expressed as a {@code float} value
     */
    /**
    public static float getFontSize(int idTextAppearance) {
        TypedValue typedValue = new TypedValue();
        ApplicationContext.getAppContext().getTheme().resolveAttribute(
                idTextAppearance, typedValue, true);
        int[] textSizeAttr = new int[]{android.R.attr.textSize};
        TypedArray arr = ApplicationContext.getAppContext().obtainStyledAttributes(
                typedValue.data, textSizeAttr);
        float fontSize = arr.getDimensionPixelSize(0, -1);
        arr.recycle();
        return fontSize;
    }
    
    **/

    /**
     * Returns an array whose values are normalized to an intervall of [-1, 1]
     * @param samplesToNormalize array to normalize, not null
     * @return normalized array of parameter
     */
    public static float[] normalizeArray(float[] samplesToNormalize){
        float[] normalizedSamples = new float[samplesToNormalize.length];
        System.arraycopy(samplesToNormalize, 0, normalizedSamples, 0, samplesToNormalize.length);
        float maxValue = getMaxValue(normalizedSamples);
        float minValue = getMinValue(normalizedSamples);
        float maxDistanceToZero = (Math.abs(maxValue) > Math.abs(minValue)) ? maxValue : minValue;
        maxDistanceToZero = Math.abs(maxDistanceToZero);

        for(int i = 0 ; i < normalizedSamples.length; i++){
            if(normalizedSamples[i] != 0) {
                normalizedSamples[i] = normalizedSamples[i] / maxDistanceToZero;
            }
        }

        return normalizedSamples;
    }

    /**
     * Returns an array whose values are normalized to an intervall of [-1, 1]
     * @param samplesToNormalize array to normalize, not null
     * @return normalized array of parameter
     */
    public static double[] normalizeArray(double[] samplesToNormalize){
        double[] normalizedSamples = new double[samplesToNormalize.length];
        System.arraycopy(samplesToNormalize, 0, normalizedSamples, 0, samplesToNormalize.length);
        double maxValue = getMaxValue(normalizedSamples);
        double minValue = getMinValue(normalizedSamples);
        double maxDistanceToZero = (Math.abs(maxValue) > Math.abs(minValue)) ? maxValue : minValue;
        maxDistanceToZero = Math.abs(maxDistanceToZero);

        for(int i = 0 ; i < normalizedSamples.length; i++){
            if(normalizedSamples[i] != 0) {
                normalizedSamples[i] = normalizedSamples[i] / maxDistanceToZero;
            }
        }

        return normalizedSamples;
    }


    /**
     * Returns max value of given array
     * @param samples array whose max value should be searched and returned, not null
     * @return max value of input
     */
    public static float getMaxValue(float[] samples){

        float max = samples[0];

        for (int i = 0 ; i < samples.length; i++){
            float currentSample = samples[i];
            if(currentSample > max){
                max = currentSample;
            }
        }
        return max;
    }

    /**
     * Returns max value of given array
     * @param samples array whose max value should be searched and returned, not null
     * @return max value of input
     */
    public static double getMaxValue(double[] samples){

        double max = samples[0];

        for (int i = 0 ; i < samples.length; i++){
            double currentSample = samples[i];
            if(currentSample > max){
                max = currentSample;
            }
        }
        return max;
    }

    /**
     * Returns the min value of a given float array
     * @param samples array whose min value should be calculated, not null
     * @return min value of provided array
     */
    public static float getMinValue(float[] samples){

        float min = samples[0];
        for (int i = 0 ; i < samples.length; i++){
            float currentSample = samples[i];
            if(currentSample < min){
                min = currentSample;
            }
        }
        return min;
    }

    /**
     * Returns the min value of a given float array
     * @param samples array whose min value should be calculated, not null
     * @return min value of provided array
     */
    public static double getMinValue(double[] samples){

        double min = samples[0];
        for (int i = 0 ; i < samples.length; i++){
            double currentSample = samples[i];
            if(currentSample < min){
                min = currentSample;
            }
        }
        return min;
    }

    /**
     * Takes an array and sets all values under the threshold to zero, leaves the others. Given array does not get changed
     * @param valuesToFilter array to filter
     * @return filtered copy of given array
     */
    public static float[] valuesAboveThreshold(float[] valuesToFilter, double threshold){
        float[] copyOfValuesToFilter = new float[valuesToFilter.length];
        System.arraycopy(valuesToFilter,0, copyOfValuesToFilter, 0, valuesToFilter.length);

        for(int i = 0 ; i < copyOfValuesToFilter.length; i++){
            float currentValue = copyOfValuesToFilter[i];
            if(currentValue < threshold){
                copyOfValuesToFilter[i] = 0;
            }
        }
        return copyOfValuesToFilter;
    }

    /**
     *  Filters an array with a threshold value. Done in a copy of the given array.Above threshold keeps value, below threshold will get set to zero.
     * @param valuesToFilter values to Filter
     * @param threshold min value to keep value
     * @param jumpAfterfind if set to non zero, after found a value above threshold will set the following jumpAfterFind elements to zero without checking
     * @return filtered array
     */
    public static double[] valuesAboveThreshold(double[] valuesToFilter, double threshold, int jumpAfterfind){
        double[] copyOfValuesToFilter = new double[valuesToFilter.length];



        for(int i = 0 ; i < copyOfValuesToFilter.length; i++){
            double currentValue = valuesToFilter[i];
            if(currentValue > threshold){
                copyOfValuesToFilter[i] = currentValue;
                i = i + jumpAfterfind;
            } else {
                copyOfValuesToFilter[i] = 0;

            }
        }
        return copyOfValuesToFilter;
    }

    /**
     * Calculates the absolute value of an array for each element
     * @param samples samples to calculate the absolute value from, not null
     * @return array containing the absolute values of input array
     */
    public static float[] absoluteArray(float[] samples) {
        float[] absSamples = new float[samples.length];
        for (int i = 0; i < samples.length; i++) {
            absSamples[i] = (float) Math.abs(samples[i]);
        }
        return absSamples;
    }

    /**
     * Calculates the absolute value of an array for each element
     * @param samples samples to calculate the absolute value from, not null
     * @return array containing the absolute values of input array
     */
    public static double[] absoluteArray(double[] samples) {
        double[] absSamples = new double[samples.length];
        for (int i = 0; i < samples.length; i++) {
            absSamples[i] = (double) Math.abs(samples[i]);
        }
        return absSamples;
    }

    /**
     * Calculates the absolute value of an array for each element
     * @param samples samples to calculate the absolute value from, not null
     * @return array containing the absolute values of input array
     */
    private short[] absoluteArray(short[] samples) {
        short[] absSamples = new short[samples.length];
        for (int i = 0; i < samples.length; i++) {
            absSamples[i] = (short) Math.abs(samples[i]);
        }
        return absSamples;
    }

}
