package com.nachtraben.tohsaka;

public enum ImageRating {

    SAFE("s"),
    QUESTIONABLE("q"),
    QANDE("[q|e]"),
    EXPLICIT("e");


    private String regex;

    ImageRating(String regex) {
        this.regex = regex;
    }

    public boolean matches(String rating) {
        return rating.matches(regex);
    }

}
