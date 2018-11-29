package de.sharknoon.slash.properties;

import org.aeonbits.owner.Config;

public interface GeneralConfig extends Config {

    @DefaultValue("https://sharknoon.de")
    String serverURL();
}
