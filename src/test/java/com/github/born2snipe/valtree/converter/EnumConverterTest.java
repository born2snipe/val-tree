package com.github.born2snipe.valtree.converter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnumConverterTest {
    private EnumConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new EnumConverter(Trait.class);
    }

    @Test(expected = UnableToConvertFromTextException.class)
    public void shouldBlowUpWhenFailingToConvertFromTextToTheEnumValue() {
        converter.convertFromText("does.not.exist");
    }

    @Test
    public void shouldSupportConvertingToText() {
        assertEquals("HEARING", converter.convertToText(Trait.HEARING));
        assertEquals("READING MINDS", converter.convertToText(Trait.READING_MINDS));
    }

    @Test
    public void shouldSupportConvertingFromText() {
        assertEquals(Trait.HEARING, converter.convertFromText("HEARING"));
        assertEquals(Trait.HEARING, converter.convertFromText("hearing"));
        assertEquals(Trait.READING_MINDS, converter.convertFromText("reading minds"));
    }

    public enum Trait {
        HEARING, READING_MINDS
    }
}