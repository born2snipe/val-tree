package com.github.born2snipe.valtree.converter;

import com.badlogic.gdx.math.Vector2;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Vector2ConverterTest {
    private Vector2Converter converter;

    @Before
    public void setUp() throws Exception {
        converter = new Vector2Converter();
    }

    @Test(expected = UnableToConvertFromTextException.class)
    public void shouldThrowAnExceptionIfWeFailToConvertAValueFromAString() {
        converter.convertFromText("will.not.convert");
    }

    @Test
    public void shouldAllowWritingVector2ToStrings() {
        assertEquals("(1.0, 2.0)", converter.convertToText(new Vector2(1, 2)));
    }

    @Test
    public void shouldAllowReadingVector2FromStrings() {
        assertEquals(new Vector2(1, 2), converter.convertFromText("(1,2)"));
        assertEquals(new Vector2(1, 2), converter.convertFromText("( 1 , 2 )"));
        assertEquals(new Vector2(1, 2), converter.convertFromText("1, 2"));
        assertEquals(new Vector2(1.1f, 2.2f), converter.convertFromText("1.1, 2.2"));
    }
}