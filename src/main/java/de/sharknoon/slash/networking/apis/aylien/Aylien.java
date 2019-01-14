package de.sharknoon.slash.networking.apis.aylien;

import com.aylien.textapi.TextAPIClient;
import com.aylien.textapi.parameters.SentimentParams;
import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.properties.Properties;
import de.sharknoon.slash.utils.Try;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Aylien {

    private static final String APP_ID = Properties.getEmotionConfig().APPID();
    private static final String API_KEY = Properties.getEmotionConfig().APIKey();
    private static final TextAPIClient CLIENT = new TextAPIClient(APP_ID, API_KEY);

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);
    private static final Duration DURATION = Duration.ofHours(1);

    private static Try<Sentiment> getEmotion(String text) {
        try {
            SentimentParams params = SentimentParams
                    .newBuilder()
                    .setText(text)
                    .setLanguage("de")
                    .setMode("tweet")
                    .build();
            com.aylien.textapi.responses.Sentiment newSentiment = CLIENT.sentiment(params);
            Sentiment sentiment = new Sentiment();
            sentiment.polarity = Polarity.valueOf(newSentiment.getPolarity().toLowerCase());
            sentiment.subjectivity = Subjectivity.valueOf(newSentiment.getSubjectivity().toLowerCase());
            return Try.success(sentiment);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Could not get sentiment from emotion API", e);
            return Try.failure(e);
        }
    }

    /**
     * Initializes the emotion api
     */
    public static void init() {
        Runnable chatMessagesSentiment = () ->
                DB.getAllUsers().forEach(user -> {
                    String messages = DB.getAllMessagesOfUser(user)
                            .stream()
                            .map(message ->
                                    message.getSubject()
                                            + StringUtils.SPACE
                                            + message.getContent()
                            )
                            .collect(Collectors.joining(StringUtils.SPACE));
                    Try<Sentiment> emotion = getEmotion(messages);
                    emotion.ifSuccess(sentiment -> DB.setUserSentiment(user, sentiment));
                    emotion.ifFailure(e -> Logger.getGlobal().log(Level.WARNING, "Could not get Emotion from API", e));
                });
        Runnable projectMessagesSentiment = () ->
                DB.getAllProjects().forEach(project -> {
                    String messages = project
                            .messages
                            .stream()
                            .map(message ->
                                    message.getSubject()
                                            + StringUtils.SPACE
                                            + message.getContent()
                            )
                            .collect(Collectors.joining(StringUtils.SPACE));
                    Try<Sentiment> emotion = getEmotion(messages);
                    emotion.ifSuccess(sentiment -> DB.setProjectSentiment(project, sentiment));
                    emotion.ifFailure(e -> Logger.getGlobal().log(Level.WARNING, "Could not get Emotion from API", e));
                });

        SCHEDULER.scheduleAtFixedRate(chatMessagesSentiment, 0, DURATION.toSeconds(), TimeUnit.SECONDS);
        SCHEDULER.scheduleAtFixedRate(projectMessagesSentiment, 0, DURATION.toSeconds(), TimeUnit.SECONDS);
    }

}
