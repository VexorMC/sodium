package me.jellysquid.mods.sodium.client.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallback;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL43.*;

/**
 * A utility class for routing OpenGL debug messages to the Log4J loggers.
 */
public final class GLDebugLogger {
    private static final Logger API_LOG = createDebugLogger("OpenGL API");
    private static final Logger WINDOW_SYSTEM_LOG = createDebugLogger("OpenGL Window System");
    private static final Logger SHADER_COMPILER_LOG = createDebugLogger("OpenGL Shader Compiler");
    private static final Logger THIRD_PARTY_LOG = createDebugLogger("OpenGL Third Party");
    private static final Logger APPLICATION_LOG = createDebugLogger("OpenGL Application");
    private static final Logger OTHER_LOG = createDebugLogger("OpenGL Other");
    private static final Logger UNKNOWN_LOG = createDebugLogger("OpenGL Unknown");

    private static GLDebugMessageCallback callback;

    private GLDebugLogger() {}

    /**
     * Initializes the OpenGL debug logger.
     */
    public static void init() {
        enableDebugOutput();
        setDebugMessageCallback();
        enableAllDebugMessages();
    }

    /**
     * Creates a Logger and tries to set it to the debug level.
     */
    private static Logger createDebugLogger(String name) {
        var logger = LogManager.getLogger(name);
        if (logger instanceof org.apache.logging.log4j.core.Logger realLog) {
            realLog.setLevel(Level.DEBUG);
        } else {
            logger.warn("Failed to set this log to debug level! Some messages may be voided!");
        }
        return logger;
    }

    /**
     * Enables the OpenGL debug output and makes it synchronous.
     */
    private static void enableDebugOutput() {
        GL11.glEnable(GL_DEBUG_OUTPUT);
        GL11.glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
    }

    /**
     * Sets up the OpenGL debug message callback.
     */
    private static void setDebugMessageCallback() {
        if (callback != null) callback.free(); // replace any old callback

        callback = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
            String msg = GLDebugMessageCallback.getMessage(length, message);
            log(source, type, id, severity, msg);
        });

        GL43.glDebugMessageCallback(callback, 0);
    }

    /**
     * Enables all debug messages.
     */
    private static void enableAllDebugMessages() {
        glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, (IntBuffer) null, true);
    }

    /**
     * Logs a message with appropriate formatting to the appropriate logger.
     */
    private static void log(int sourceGLEnum, int typeGLEnum, int id, int severityGLEnum, String message) {
        var sb = new StringBuilder();

        sb.append(messageHeader(typeGLEnum))
                .append(": ")
                .append(messageContent(message));

        var stackTrace = Thread.currentThread().getStackTrace();
        var trimmedStack = trimStackTrace(stackTrace);
        for (var ste : trimmedStack) {
            sb.append("\n\tat ").append(ste);
        }

        var logger = sourceLog(sourceGLEnum);
        var level = logLevel(severityGLEnum, typeGLEnum);
        logger.log(level, sb.toString());
    }

    private static String messageHeader(int typeGLEnum) {
        return switch (typeGLEnum) {
            case GL_DEBUG_TYPE_ERROR -> "Caused by";
            case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR -> "Use of deprecated behaviour";
            case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR -> "Undefined behaviour";
            case GL_DEBUG_TYPE_PORTABILITY -> "Possible portability issue";
            case GL_DEBUG_TYPE_PERFORMANCE -> "Possible performance issue";
            case GL_DEBUG_TYPE_MARKER -> "Marker";
            case GL_DEBUG_TYPE_PUSH_GROUP -> "Group push";
            case GL_DEBUG_TYPE_POP_GROUP -> "Group pop";
            default -> String.format("Unknown Type (0x%08X)", typeGLEnum);
        };
    }

    private static String messageContent(String message) {
        return (message == null || message.isEmpty()) ? "No message" : message;
    }

    private static List<String> trimStackTrace(StackTraceElement[] stack) {
        var trimmed = new ArrayList<String>();
        boolean foundEnd = false, foundStart = false;

        for (int i = stack.length - 1; i >= 0; i--) {
            var e = stack[i];
            if (!foundEnd && isFromMinecraftMainMethod(e)) {
                foundEnd = true;
            } else if (foundEnd && isFromLWJGLOpenGLPackage(e)) {
                foundStart = true;
            }

            if (foundEnd)
                trimmed.add(e.toString());

            if (foundStart)
                break;
        }

        if (trimmed.isEmpty()) {
            for (var e : stack) trimmed.add(e.toString());
        } else {
            Collections.reverse(trimmed);
        }

        return trimmed;
    }

    private static boolean isFromMinecraftMainMethod(StackTraceElement e) {
        return e.getClassName().equals("net.minecraft.client.main.Main") &&
                e.getMethodName().equals("main");
    }

    private static boolean isFromLWJGLOpenGLPackage(StackTraceElement e) {
        return e.getClassName().startsWith("org.lwjgl.opengl.");
    }

    private static Logger sourceLog(int source) {
        return switch (source) {
            case GL_DEBUG_SOURCE_API -> API_LOG;
            case GL_DEBUG_SOURCE_WINDOW_SYSTEM -> WINDOW_SYSTEM_LOG;
            case GL_DEBUG_SOURCE_SHADER_COMPILER -> SHADER_COMPILER_LOG;
            case GL_DEBUG_SOURCE_THIRD_PARTY -> THIRD_PARTY_LOG;
            case GL_DEBUG_SOURCE_APPLICATION -> APPLICATION_LOG;
            case GL_DEBUG_SOURCE_OTHER -> OTHER_LOG;
            default -> UNKNOWN_LOG;
        };
    }

    private static Level logLevel(int severity, int type) {
        return switch (severity) {
            case GL_DEBUG_SEVERITY_HIGH -> Level.ERROR;
            case GL_DEBUG_SEVERITY_MEDIUM -> Level.WARN;
            case GL_DEBUG_SEVERITY_LOW -> Level.INFO;
            default -> (type == GL_DEBUG_TYPE_ERROR) ? Level.ERROR : Level.DEBUG;
        };
    }
}
