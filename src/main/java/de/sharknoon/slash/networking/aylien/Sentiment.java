package de.sharknoon.slash.networking.aylien;

import com.google.gson.annotations.Expose;

public class Sentiment {

    @Expose
    public Polarity polarity = Polarity.NEUTRAL;
    @Expose
    public Subjectivity subjectivity = Subjectivity.OBJECTIVE;

}
