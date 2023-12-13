package com.alfa.customerbills;

import java.io.File;
import java.net.URISyntaxException;

class ConnectorUtil {
    
    static File getAppPath() {
        return getPath("alfa.darwinz.jar").getParentFile();
    }
    
    static File getPath(String file) {
        File configPath = null;
        try {
            configPath = new File(ConnectorUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
        } catch (URISyntaxException e) {}
        
        
        if (configPath == null || !new File(configPath, file).exists()) { // for local dev environment
            configPath = new File(System.getProperty("user.dir"), "output");
        }
       
        return new File(configPath, file);
    }
    
    static String duration(long t0) {
        long duration = System.currentTimeMillis() - t0;
        if (duration < 1_000) {
            return String.format("%.2f sec", duration / 1000.0);

        } else if (duration < 60_000) {
            return String.format("%.1f sec", duration / 1000.0);
        
        } else if (duration < 3_600_000) {
            return String.format("%.1f min", duration / 1000.0 / 60.0);

        } else {
            return String.format("%.1f hrs", duration / 1000.0 / 60.0 / 60.0);
        }
    }

}
