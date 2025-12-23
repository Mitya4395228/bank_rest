package com.example.bankcards.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.MessageSourceResolvable;

/**
 * Used to handle exceptions in messages for
 * {@link com.example.bankcards.exception.handler.ErrorInfo}
 */

public final class ExceptionUtil {

    private ExceptionUtil() {
    }

    /**
     * Truncates Exception.printStackTrace to 10 lines
     * 
     * @param e {@code Exception}
     * @return {@code String} last 10 lines of stack trace
     */
    public static String getStackTraceAsString(Exception e) {
        var sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        var s = sw.toString();
        return s.contains("\n") ? s.lines().limit(10L).collect(Collectors.joining("\n")) + "\n\t..." : s;
    }

    /**
     * Get jointed messages from list of {@code MessageSourceResolvable}
     * 
     * @param messages list of {@code MessageSourceResolvable}
     * @return {@code String} jointed messages
     */
    public static String getJointedMessages(List<? extends MessageSourceResolvable> messages) {
        return messages.stream().map(m -> m.getDefaultMessage()).collect(Collectors.joining("; "));
    }

}
