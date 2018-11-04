package de.sharknoon.slash.properties;

import org.aeonbits.owner.Config;

public interface UserConfig extends Config {
    
    @DefaultValue("5")
    int maxdevices();
    
    @DefaultValue("6")
    int amountfavouritechats();
    
    @DefaultValue("50")
    int amountstoredchatmessages();
    
}

