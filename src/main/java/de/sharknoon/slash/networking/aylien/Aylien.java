package de.sharknoon.slash.networking.aylien;

import com.aylien.textapi.TextAPIClient;
import com.aylien.textapi.parameters.SentimentParams;
import de.sharknoon.slash.properties.Properties;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Aylien {

    private static final String APPID = Properties.getEmotionConfig().APPID();
    private static final String APIKey = Properties.getEmotionConfig().APIKey();
    private static final TextAPIClient client = new TextAPIClient(APPID, APIKey);

    public static Optional<Sentiment> getEmotion(String text) {
        try {
            SentimentParams params = SentimentParams
                    .newBuilder()
                    .setText(text)
                    .setLanguage("de")
                    .setMode("tweet")
                    .build();
            com.aylien.textapi.responses.Sentiment newSentiment = client.sentiment(params);
            Sentiment sentiment = new Sentiment();
            sentiment.polarity = Polarity.valueOf(newSentiment.getPolarity().toLowerCase());
            sentiment.subjectivity = Subjectivity.valueOf(newSentiment.getSubjectivity().toLowerCase());
            return Optional.of(sentiment);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Could not get sentiment from emotion API", e);
            return Optional.empty();
        }
    }

}
