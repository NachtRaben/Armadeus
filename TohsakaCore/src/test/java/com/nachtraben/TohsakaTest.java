package com.nachtraben;

import com.nachtraben.tohsaka.Tohsaka;

public class TohsakaTest {

    private Tohsaka tohsaka;

    public TohsakaTest(String[] args) {
        tohsaka = new Tohsaka(args, true);
    }

    public static void main(String[] args) {
        new TohsakaTest(args);
    }

}
