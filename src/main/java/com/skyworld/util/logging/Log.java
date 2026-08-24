package com.skyworld.util.logging;

import com.skyworld.domain.enums.LoggingLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Log {

    private static void log(Logger logger, LoggingLevel loggingLevel, String message, Throwable throwable) {
        switch (loggingLevel) {
            case DEBUG:
                if (throwable != null) logger.debug(message, throwable);
                else logger.debug(message);
                break;

            case INFO:
                if (throwable != null) logger.info(message, throwable);
                else logger.info(message);
                break;

            case WARNING:
                if (throwable != null) logger.warn(message, throwable);
                else logger.warn(message);
                break;

            case ERROR:
                if (throwable != null) logger.error(message, throwable);
                else logger.error(message);
                break;
        }
    }

    public static void debug(Class<?> clazz, String methodName, Object message) {
        debug(clazz, methodName, message, null);
    }

    public static void info(Class<?> clazz, String methodName, Object message) {
        info(clazz, methodName, message, null);
    }

    public static void warning(Class<?> clazz, String methodName, Object message) {
        warning(clazz, methodName, message, null);
    }

    public static void error(Class<?> clazz, String methodName, Object message) {
        error(clazz, methodName, message, null);
    }

    public static void debug(Class<?> clazz, String methodName, Object message, Throwable throwable) {
        Logger logger = LoggerFactory.getLogger(clazz);
        log(logger, LoggingLevel.DEBUG, "." + methodName + "() - " + message, throwable);
    }

    public static void info(Class<?> clazz, String methodName, Object message, Throwable throwable) {
        Logger logger = LoggerFactory.getLogger(clazz);
        log(logger, LoggingLevel.INFO, "." + methodName + "() - " + message, throwable);
    }

    public static void warning(Class<?> clazz, String methodName, Object message, Throwable throwable) {
        Logger logger = LoggerFactory.getLogger(clazz);
        log(logger, LoggingLevel.WARNING, "." + methodName + "() - " + message, throwable);
    }

    public static void error(Class<?> clazz, String methodName, Object message, Throwable throwable) {
        Logger logger = LoggerFactory.getLogger(clazz);
        log(logger, LoggingLevel.ERROR, "." + methodName + "() - " + message, throwable);
    }

}


