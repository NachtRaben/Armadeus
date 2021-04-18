package dev.armadeus.core.util;

/**
 * @author NachtRaben
 * @since September 25, 2018
 */
public class ThreadDumpUtils {

    public static void format(StackTraceElement[] elements, StringBuilder builder) {
        for (StackTraceElement stack : elements) {
            builder.append("\n    ").append(stack);
        }
    }

}
