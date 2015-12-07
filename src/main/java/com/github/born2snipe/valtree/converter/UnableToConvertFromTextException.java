package com.github.born2snipe.valtree.converter;

public class UnableToConvertFromTextException extends RuntimeException {
    public UnableToConvertFromTextException(String text, Class clazz) {
        super("Unable to convert text=[" + text + "] into type " + clazz.getName());
    }

    public UnableToConvertFromTextException(String text, Class clazz, Throwable cause) {
        super("Unable to convert text=[" + text + "] into type " + clazz.getName(), cause);
    }
}
