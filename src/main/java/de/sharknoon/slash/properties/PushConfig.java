package de.sharknoon.slash.properties;

import org.aeonbits.owner.Config;

public interface PushConfig extends Config {
    
    @DefaultValue("")
    String APIKey();
    
}
