package biz.paluch.heckenlights.messagebox.client.twitter;

import java.util.Collections;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import reactor.core.publisher.TopicProcessor;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
public class StreamingSupervisor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Stream stream;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @Autowired
    private Twitter twitter;

    @Value("${twitter.filter}")
    private String filter;

    @Value("${twitter.streaming.enabled}")
    private boolean enabled;

    private TopicProcessor<Tweet> fluxProcessor = TopicProcessor.create();

    @PostConstruct
    public void postConstruct() {

        logger.info("Prepare Stream");
        StreamListener streamListener = new StreamListener() {
            @Override
            public void onTweet(Tweet tweet) {

                fluxProcessor.onNext(tweet);
            }

            @Override
            public void onDelete(StreamDeleteEvent streamDeleteEvent) {
                logger.info("Tweet Deleted: " + streamDeleteEvent.getTweetId());
            }

            @Override
            public void onLimit(int i) {
                logger.warn("Limit: " + i);
            }

            @Override
            public void onWarning(StreamWarningEvent streamWarningEvent) {
                logger.warn(streamWarningEvent.getCode() + ": " + streamWarningEvent.getMessage() + ", "
                        + streamWarningEvent.getPercentFull());
            }
        };

        fluxProcessor.map(tweet -> {
            logger.info("Incoming tweet {} {}", tweet.getFromUser(), tweet.getText());

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
            logger.info("Stream open with filter=" + filter);
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (stream != null) {
            stream.close();
        }
    }
}
