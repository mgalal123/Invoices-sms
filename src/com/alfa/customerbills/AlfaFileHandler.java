package com.alfa.customerbills;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import static com.alfa.customerbills.ConnectorUtil.getAppPath;

public class AlfaFileHandler extends Handler {
    
    private final FileHandler fileHandler;

    public AlfaFileHandler() throws IOException, SecurityException {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();
        String pattern = manager.getProperty(cname + ".pattern");
        File logsPath = new File(getAppPath(), "logs");
        if (!logsPath.exists()) logsPath.mkdir();

        String file = pattern != null ? new File(logsPath, pattern.replaceAll("%d", getCurrentDateStr())).getAbsolutePath() : null;
        fileHandler = file != null ? new FileHandler(file, true) : new FileHandler();
        fileHandler.setFormatter(new SimpleFormatter());
    }
    
    public void publish(LogRecord record) {
        fileHandler.publish(record);
    }
    
    public void flush() {
        fileHandler.flush();
    }

    public void close() throws SecurityException {
        fileHandler.close();
    }

    private String getCurrentDateStr() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }
}