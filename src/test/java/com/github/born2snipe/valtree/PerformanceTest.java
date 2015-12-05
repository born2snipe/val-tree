package com.github.born2snipe.valtree;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PerformanceTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    private ValTree data;

    @Before
    public void setUp() throws Exception {
        data = new ValTree();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            String key = "key-" + i;
            data.addChild(key, i);
            ValTree child = data.getChild(key);

            for (int j = 0; j < 1000; j++) {
                key = "child-" + j;
                child.addChild(key, j);
                child = child.getChild(key);
            }
        }
        System.out.println("Data setup in: " + (System.currentTimeMillis() - start) + " millis");
    }

    @Test
    public void shouldBePerformantAtParsing() throws IOException {
        File tmpFile = tmp.newFile();
        long start = System.currentTimeMillis();
        data.save(new FileOutputStream(tmpFile));
        System.out.println("Test file size: " + tmpFile.length() / 1024 / 1024 + " Mb");
        System.out.println("Write elapsed: " + (System.currentTimeMillis() - start) + " millis");

        start = System.currentTimeMillis();
        data.parse(new FileInputStream(tmpFile));
        System.out.println("Read elapsed: " + (System.currentTimeMillis() - start) + " millis");
    }
}
