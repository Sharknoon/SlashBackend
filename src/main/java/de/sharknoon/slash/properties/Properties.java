package de.sharknoon.slash.properties;

import org.aeonbits.owner.ConfigFactory;

public class Properties {
    
    private static DBConfig dbConfig = null;
    private static UserConfig userConfig = null;
    
    public static DBConfig getDBConfig() {
        if (dbConfig == null) {
            dbConfig = ConfigFactory.create(DBConfig.class);
        }
        return dbConfig;
    }
    
    public static UserConfig getUserConfig() {
        if (userConfig == null) {
            userConfig = ConfigFactory.create(UserConfig.class);
        }
        return userConfig;
    }
    
}
