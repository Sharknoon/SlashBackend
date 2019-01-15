package de.sharknoon.slash.database.models.sentiment;

import com.google.gson.annotations.Expose;

public class Sentiment {

    @Expose
    public Polarity polarity = Polarity.UNKNOWN;
    @Expose
    public Subjectivity subjectivity = Subjectivity.UNKNOWN;

}
