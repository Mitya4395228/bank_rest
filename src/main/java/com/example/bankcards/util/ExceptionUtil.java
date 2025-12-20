package com.example.bankcards.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.MessageSourceResolvable;

public final class ExceptionUtil {

    private ExceptionUtil() {
    }

    public static String getStackTraceAsString(Exception e) {
        var sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        var s = sw.toString();
        return s.contains("\n") ? s.lines().limit(10L).collect(Collectors.joining("\n")) + "\n\t..." : s;
    }

    public static String getJointedMessages(List<? extends MessageSourceResolvable> messages) {
        return messages.stream().map(m -> m.getDefaultMessage()).collect(Collectors.joining("; "));
    }

}
