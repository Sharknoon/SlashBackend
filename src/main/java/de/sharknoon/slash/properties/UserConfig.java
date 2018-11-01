package de.sharknoon.slash.properties;

import org.aeonbits.owner.Config;

public interface UserConfig extends Config {
    
    @DefaultValue("5")
    int maxdevices();
    
}

