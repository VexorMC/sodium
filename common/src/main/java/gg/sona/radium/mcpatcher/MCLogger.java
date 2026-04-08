package gg.sona.radium.mcpatcher;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class MCLogger {

    private static final Map<String, MCLogger> allLoggers = new HashMap<>();

    private static final long FLOOD_INTERVAL = 1000L;
    private static final long FLOOD_REPORT_INTERVAL = 5000L;
    private static final int FLOOD_LEVEL = Level.INFO.intLevel();

    private final String logPrefix;
    private final Logger logger;

    private boolean flooding;
    private long lastFloodReport;
    private int floodCount;
    private long lastMessage = System.currentTimeMillis();

    public static MCLogger getLogger(Category category) {
        return getLogger(category, category.name);
    }

    public static synchronized MCLogger getLogger(Category category, String logPrefix) {
        MCLogger logger = allLoggers.get(category.name);
        if (logger == null) {
            logger = new MCLogger(category, logPrefix);
            allLoggers.put(category.name, logger);
        }
        return logger;
    }

    private MCLogger(Category category, String logPrefix) {
        this.logPrefix = logPrefix;
        logger = LogManager.getLogger(category.name);
    }

    private boolean checkFlood() {
        long now = System.currentTimeMillis();
        boolean showFloodMessage = false;
        if (now - lastMessage > FLOOD_INTERVAL) {
            if (flooding) {
                reportFlooding(now);
                flooding = false;
            } else {
                floodCount = 0;
            }
        } else if (flooding && now - lastFloodReport > FLOOD_REPORT_INTERVAL) {
            reportFlooding(now);
            showFloodMessage = true;
        }
        lastMessage = now;
        floodCount++;
        if (flooding) {
            return showFloodMessage;
        } else {
            return true;
        }
    }

    private void reportFlooding(long now) {
        if (floodCount > 0) {
            logger.log(
                Level.WARN,
                String
                    .format("%d flood messages dropped in the last %ds", floodCount, (now - lastFloodReport) / 1000L));
        }
        floodCount = 0;
        lastFloodReport = now;
    }

    public boolean isLoggable(Level level) {
        return true /* TODO? */;
    }

    public void log(Level level, String format, Object... params) {
        if (isLoggable(level)) {
            if (level.intLevel() >= FLOOD_LEVEL && !checkFlood()) {
                return;
            }
            logger.log(level, String.format(format, params));
        }
    }

    public void severe(String format, Object... params) {
        log(Level.FATAL, format, params);
    }

    public void error(String format, Object... params) {
        log(Level.ERROR, format, params);
    }

    public void warning(String format, Object... params) {
        log(Level.WARN, format, params);
    }

    public void info(String format, Object... params) {
        log(Level.INFO, format, params);
    }

    public void config(String format, Object... params) {
        log(Level.DEBUG, format, params);
    }

    public void fine(String format, Object... params) {
        log(Level.TRACE, format, params);
    }

    public void finer(String format, Object... params) {
        log(Level.TRACE, format, params);
    }

    public void finest(String format, Object... params) {
        log(Level.TRACE, format, params);
    }


    public enum Category {

        CUSTOM_COLORS(MCPatcherUtils.CUSTOM_COLORS),
        CUSTOM_ITEM_TEXTURES(MCPatcherUtils.CUSTOM_ITEM_TEXTURES),
        CONNECTED_TEXTURES(MCPatcherUtils.CONNECTED_TEXTURES),
        EXTENDED_HD(MCPatcherUtils.EXTENDED_HD),
        RANDOM_MOBS(MCPatcherUtils.RANDOM_MOBS),
        BETTER_SKIES(MCPatcherUtils.BETTER_SKIES),
        TEXTURE_PACK("Texture Pack"),
        TILESHEET("Tilesheet"),
        BETTER_GLASS(MCPatcherUtils.BETTER_GLASS),

        ;

        public final String name;

        Category(String name) {
            this.name = name;
        }
    }
}
