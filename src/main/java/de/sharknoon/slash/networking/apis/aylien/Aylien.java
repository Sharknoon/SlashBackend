package de.sharknoon.slash.networking.apis.aylien;

import com.aylien.textapi.TextAPIClient;
import com.aylien.textapi.parameters.SentimentParams;
import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.message.Message;
import de.sharknoon.slash.database.models.sentiment.Polarity;
import de.sharknoon.slash.database.models.sentiment.Sentiment;
import de.sharknoon.slash.database.models.sentiment.Subjectivity;
import de.sharknoon.slash.networking.apis.pushy.PushStatus;
import de.sharknoon.slash.networking.apis.pushy.Pushy;
import de.sharknoon.slash.properties.Properties;
import de.sharknoon.slash.utils.Try;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Set;
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
            try {
                sentiment.polarity = Polarity.valueOf(newSentiment.getPolarity().toUpperCase());
            } catch (Exception e) {
                sentiment.polarity = Polarity.UNKNOWN;
                Logger.getGlobal().log(Level.WARNING, "Unknown Polarity from Emotion API: " + newSentiment.getPolarity());
            }
            try {
                sentiment.subjectivity = Subjectivity.valueOf(newSentiment.getSubjectivity().toUpperCase());
            } catch (Exception e) {
                sentiment.subjectivity = Subjectivity.UNKNOWN;
                Logger.getGlobal().log(Level.WARNING, "Unknown Subjectivity from Emotion API: " + newSentiment.getSubjectivity());
            }
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
        Runnable chatMessagesSentiment = () -> {
            DB.getAllUsers().forEach(user -> {
                Set<Message> allMessagesOfUser = DB.getAllMessagesOfUser(user);
                String messages = allMessagesOfUser
                        .stream()
                        .skip(allMessagesOfUser.size() > 10 ? allMessagesOfUser.size() - 10 : 0)
                        .map(Aylien::getMessageContent)
                        .collect(Collectors.joining(StringUtils.SPACE));
                Try<Sentiment> emotion = getEmotion(messages);
                emotion.ifSuccess(sentiment -> DB.setUserSentiment(user, sentiment));
                emotion.ifFailure(e -> Logger.getGlobal().log(Level.WARNING, "Could not get Emotion from API", e));
            });
            Logger.getGlobal().log(Level.INFO, "Finished User Sentiment analysis");
        };
        Runnable projectMessagesSentiment = () -> {
            DB.getAllProjects().forEach(project -> {
                Set<Message> allMessagesOfProject = project.messages;
                String messages = allMessagesOfProject
                        .stream()
                        .skip(allMessagesOfProject.size() > 10 ? allMessagesOfProject.size() - 10 : 0)
                        .map(Aylien::getMessageContent)
                        .collect(Collectors.joining(StringUtils.SPACE));
                Try<Sentiment> emotion = getEmotion(messages);
                emotion.ifSuccess(sentiment -> {
                    Polarity oldPolarity = project.sentiment.polarity;
                    DB.setProjectSentiment(project, sentiment);
                    if (sentiment.polarity == Polarity.NEGATIVE &&
                            project.projectOwner != null &&
                            oldPolarity != sentiment.polarity) {
                        DB.getUser(project.projectOwner).ifPresent(projectOwner ->
                                Pushy.sendPush(
                                        PushStatus.BAD_PROJECT_SENTIMENT,
                                        project.id.toHexString(),
                                        project.name,
                                        projectOwner
                                )
                        );
                    }
                });
                emotion.ifFailure(e -> Logger.getGlobal().log(Level.WARNING, "Could not get Emotion from API", e));
            });
            Logger.getGlobal().log(Level.INFO, "Finished Project Sentiment analysis");
        };

        LocalTime nextHour = LocalTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(1);
        long delay = LocalTime.now().until(nextHour, ChronoUnit.SECONDS);
        SCHEDULER.scheduleAtFixedRate(chatMessagesSentiment, delay, DURATION.toSeconds(), TimeUnit.SECONDS);
        SCHEDULER.scheduleAtFixedRate(projectMessagesSentiment, delay, DURATION.toSeconds(), TimeUnit.SECONDS);
    }

    private static String getMessageContent(Message m) {
        String subject = Objects.requireNonNullElse(m.getSubject(), StringUtils.EMPTY);
        String content = Objects.requireNonNullElse(m.getContent(), StringUtils.EMPTY);
        if (!subject.isEmpty()) {
            return subject
                    + StringUtils.SPACE
                    + content;
        } else {
            return content;
        }
    }

}
