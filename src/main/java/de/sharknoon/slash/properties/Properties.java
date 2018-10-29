package de.sharknoon.slash.properties;

import org.aeonbits.owner.ConfigFactory;

public class Properties {
    
    private static DBConfig dbConfig = null;
    
    public static DBConfig getProperties() {
        if (dbConfig == null) {
            dbConfig = ConfigFactory.create(DBConfig.class);
        }
        return dbConfig;
    }
    
}
