package vt.wurrego.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wurrego on 5/9/17.
 */
public class Logger {

    private static SimpleDateFormat simpleDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void log(String TAG, String message) {
        String timestamp = simpleDataFormat.format(new Date(System.currentTimeMillis()));

        System.out.println( timestamp + TAG + " - " + message);

    }

    public static void log(boolean seperator) {

        System.out.println( "\n======================================================================");

    }
}
