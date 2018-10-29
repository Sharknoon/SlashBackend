package de.sharknoon.slash.properties;

import org.aeonbits.owner.Config;

public interface DBConfig extends Config {
    
    @DefaultValue("sharknoon.de")
    String databaseip();
    
    @DefaultValue("27017")
    int databaseport();
    
    @DefaultValue("slash")
    String database();
    
    @DefaultValue("slash")
    String dbuser();
    
    @DefaultValue("")
    String dbpassword();
    
}
