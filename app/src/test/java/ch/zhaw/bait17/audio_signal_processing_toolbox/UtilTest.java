package ch.zhaw.bait17.audio_signal_processing_toolbox;

import org.junit.*;

import ch.zhaw.bait17.audio_signal_processing_toolbox.util.Util;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;


/**
 * Created by Matthias on 07.11.2017.
 */

public class UtilTest {
    @Before
    public void setUp() {

    }

    @Test
    public void MaxValueOneElement() {
        float[] testArray = {1};
        float max = Util.getMaxValue(testArray);
        assertEquals( (float) 1,max);
    }

    @Test
    public void MaxValueTwoElementsLastBigger() {
        float[] testArray = {1, 5};
        float max = Util.getMaxValue(testArray);
        assertEquals((float) 5,max);
    }

    @Test
    public void MaxValueTwoSameValues() {
        float[] testArray = {1, 1, 1 / 2};
        float max = Util.getMaxValue(testArray);
        assertEquals( (float) 1,max);
    }

    @Test
    public void MaxValueTwoNegativeValues() {
        float[] testArray = {-1, -2};
        float max = Util.getMaxValue(testArray);
        assertEquals((float) -1,max);
    }

    @Test
    public void MaxValueNegativeandPositive() {
        float[] testArray = {-5, 1};
        float max = Util.getMaxValue(testArray);
        assertEquals( (float) 1,max);
    }

    @Test
    public void MaxValueNegativeandZero() {
        float[] testArray = {-5, 0};
        float max = Util.getMaxValue(testArray);
        assertEquals( (float) 0,max);
    }

    @Test(expected = NullPointerException.class)
    public void MaxValueNull() {
        float max = Util.getMaxValue(null);
    }

    @Test(expected = NullPointerException.class)
    public void MaxValueArrayWithNull() {
        float[] testArray = {new Float(null)};
        float max = Util.getMaxValue(testArray);
    }

    @Test
    public void MinValueOneElementNegative() {
        float[] testArray = {-5};
        float min = Util.getMinValue(testArray);
        assertEquals((float) -5,min);
    }

    @Test
    public void MinValueOneElementPositive() {
        float[] testArray = {5};
        float min = Util.getMinValue(testArray);
        assertEquals( (float) 5,min);
    }

    @Test
    public void MinValueOneElementZero() {
        float[] testArray = {0};
        float min = Util.getMinValue(testArray);
        assertEquals( (float) 0,min);
    }

    @Test(expected = NullPointerException.class)
    public void MinValueNull() {
        float min = Util.getMinValue(null);
    }

    @Test(expected = NullPointerException.class)
    public void MinValueArrayWithNull() {
        float[] testArray = {new Float(null)};
        float min = Util.getMinValue(testArray);
    }

    @Test
    public void MinValueTwoElementsLastBigger() {
        float[] testArray = {-1, -5};
        float min = Util.getMinValue(testArray);
        assertEquals( (float) -5,min);
    }

    @Test
    public void MinValueTwoSameValues() {
        float[] testArray = {-1, -1, (float)0.5};
        float min = Util.getMinValue(testArray);
        assertEquals((float) -1,min);
    }

    @Test
    public void MinValueTwoNegativeValues() {
        float[] testArray = {-1, -2};
        float min = Util.getMinValue(testArray);
        assertEquals((float) -2,min);
    }

    @Test
    public void MinValueNegativeandPositive() {
        float[] testArray = {-5, 1};
        float min = Util.getMinValue(testArray);
        assertEquals((float)-5,min );
    }

    @Test
    public void MinValueNegativeandZero() {
        float[] testArray = {5, 0};
        float min = Util.getMinValue(testArray);
        assertEquals((float) 0,min);
    }

    @Test
    public void MinValueAbsPosHigher() {
        float[] testArray = {-5, 7};
        float min = Util.getMinValue(testArray);
        assertEquals( (float) -5,min);
    }

    @Test
    public void normalizeSamplesZero(){
        float[] solutionArray = {0};
        float[] testArray = {0};
        float[] normalized = Util.normalizeArray(testArray);
        assertArrayEquals(solutionArray, normalized, 0);
    }

    @Test(expected = NullPointerException.class)
    public void normalizeSamplesNull(){
        float[] normalized = Util.normalizeArray(null);
    }

    @Test
    public void normalizeSamplesPositiveNoChange(){
        float[] solutionArray = {0, (float)0.5, 1};
        float[] testArray = {0,(float)0.5, 1};
        float[] normalized = Util.normalizeArray(testArray);
        assertArrayEquals(solutionArray, normalized, 0);
    }

    @Test
    public void normalizeSamplesPositiveAndNegativeSame(){
        float[] solutionArray = {-1,(float)-0.5,0, (float)1.0/2, 1};
        float[] testArray = {-2,-1,0,1, 2};
        float[] normalized = Util.normalizeArray(testArray);
        assertArrayEquals(solutionArray, normalized, 0);
    }

    @Test
    public void normalizeSamplesPositiveAndNegativeDifferent(){
        float[] solutionArray = {(float)-0.02,(float)-0.01,0,(float)0.01, 1};
        float[] testArray = {-2,-1,0,1, 100};
        float[] normalized = Util.normalizeArray(testArray);
        assertArrayEquals(solutionArray, normalized, 0);
    }





}
