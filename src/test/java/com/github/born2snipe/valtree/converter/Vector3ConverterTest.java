package com.github.born2snipe.valtree.converter;

import com.badlogic.gdx.math.Vector3;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Vector3ConverterTest {
    private Vector3Converter converter;

    @Before
    public void setUp() throws Exception {
        converter = new Vector3Converter();
    }

    @Test
    public void shouldSupportConvertingAVectorToAString() {
        assertEquals("(1.0, 2.0, 3.0)", converter.convertToText(new Vector3(1, 2, 3)));
    }

    @Test(expected = UnableToConvertFromTextException.class)
    public void shouldBlowUpIfWeAreUnableToParseOutAVector() {
        converter.convertFromText("does.not.convert");
    }

    @Test
    public void shouldSupportConvertToVector3sFromAString() {
        Vector3 expectedVector = new Vector3(1, 2, 3);
        assertEquals(expectedVector, converter.convertFromText("(1,2,3)"));
        assertEquals(expectedVector, converter.convertFromText("(1.0,2.0,3.0)"));
        assertEquals(expectedVector, converter.convertFromText("( 1.0, 2.0 , 3.0 )"));
        assertEquals(expectedVector, converter.convertFromText("1.0,2.0,3.0"));
    }
}