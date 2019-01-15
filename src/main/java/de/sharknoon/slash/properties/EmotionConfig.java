package de.sharknoon.slash.properties;

import org.aeonbits.owner.Config;

public interface EmotionConfig extends Config {

    @DefaultValue("")
    String APPID();

    @DefaultValue("")
    String APIKey();

}
