package com.github.born2snipe.valtree.converter;

public class IntegerConverter extends ValueConverter<Integer> {
    @Override
    protected Integer fromText(String text) {
        return Integer.valueOf(text);
    }

    @Override
    protected String toText(Integer value) {
        return value.toString();
    }
}
