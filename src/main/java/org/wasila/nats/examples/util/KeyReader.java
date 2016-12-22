package org.wasila.nats.examples.util;

import java.io.IOException;

/**
 * Created by adam on 22.12.16.
 */
public class KeyReader {

    public static void waitForEnter() {
        try {
            System.in.read();
        } catch (IOException e) {
        }
    }

}
