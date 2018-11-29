package de.sharknoon.slash.properties;

import org.aeonbits.owner.ConfigFactory;

public class Properties {
    
    private static DBConfig dbConfig = null;
    private static UserConfig userConfig = null;
    private static PushConfig pushConfig = null;
    public static GeneralConfig generalConfig = null;
    
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
    
    public static PushConfig getPushConfig() {
        if (pushConfig == null) {
            pushConfig = ConfigFactory.create(PushConfig.class);
        }
        return pushConfig;
    }

    public static GeneralConfig getGeneralConfig() {
        if (generalConfig == null) {
            generalConfig = ConfigFactory.create(GeneralConfig.class);
        }
        return generalConfig;
    }
}
