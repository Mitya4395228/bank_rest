package com.example.bankcards.exception.handler;

import java.util.List;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatusCode;

import com.example.bankcards.util.ExceptionUtil;

public record ErrorInfo(HttpStatusCode httpStatus, Class exceptionClass, String message, String printStackTrace) {

    public ErrorInfo(HttpStatusCode httpStatus, Exception ex) {
        this(httpStatus, ex.getClass(), ex.getMessage(), ExceptionUtil.getStackTraceAsString(ex));
    }

    public ErrorInfo(HttpStatusCode httpStatus, Exception ex, List<? extends MessageSourceResolvable> messages) {
        this(httpStatus, ex.getClass(), ExceptionUtil.getJointedMessages(messages),
                ExceptionUtil.getStackTraceAsString(ex));
    }

}
