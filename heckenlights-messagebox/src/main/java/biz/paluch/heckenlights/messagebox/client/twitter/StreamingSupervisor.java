package biz.paluch.heckenlights.messagebox.client.twitter;

import java.util.Collections;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.social.twitter.api.Stream;
import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamWarningEvent;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import biz.paluch.heckenlights.messagebox.repository.TweetDocument;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.TopicProcessor;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
@Slf4j
public class StreamingSupervisor {

    private final TopicProcessor<Tweet> fluxProcessor = TopicProcessor.create();

    private final ReactiveMongoTemplate mongoTemplate;
    private final Twitter twitter;
    private final String filter;
    private final boolean enabled;

    private Stream stream;

    public StreamingSupervisor(ReactiveMongoTemplate mongoTemplate, Twitter twitter, @Value("${twitter.filter}") String filter,
            @Value("${twitter.streaming.enabled}") boolean enabled) {

        this.mongoTemplate = mongoTemplate;
        this.twitter = twitter;
        this.filter = filter;
        this.enabled = enabled;
    }

    @PostConstruct
    public void postConstruct() {

        log.info("Initialize StreamListener");

        StreamListener streamListener = new StreamListener() {
            @Override
            public void onTweet(Tweet tweet) {

                fluxProcessor.onNext(tweet);
            }

            @Override
            public void onDelete(StreamDeleteEvent streamDeleteEvent) {
                log.info("Tweet Deleted: " + streamDeleteEvent.getTweetId());
            }

            @Override
            public void onLimit(int i) {
                log.warn("Limit: " + i);
            }

            @Override
            public void onWarning(StreamWarningEvent streamWarningEvent) {
                log.warn(streamWarningEvent.getCode() + ": " + streamWarningEvent.getMessage() + ", "
                        + streamWarningEvent.getPercentFull());
            }
        };

        fluxProcessor.map(tweet -> {

            log.info("Incoming tweet {} {}", tweet.getFromUser(), tweet.getText());

            TweetDocument document = new TweetDocument();
            document.setId(tweet.getId());
            document.setReceived(new Date());
            document.setSender(tweet.getFromUser());
            document.setMessage(tweet.getText());
            document.setProcessed(false);

            return document;
        }).flatMap(mongoTemplate::insert).subscribe();

        if (enabled && StringUtils.hasText(filter)) {
            stream = twitter.streamingOperations().filter(filter, Collections.singletonList(streamListener));
            log.info("Stream open with filter=" + filter);
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (stream != null) {
            stream.close();
        }
    }
}
