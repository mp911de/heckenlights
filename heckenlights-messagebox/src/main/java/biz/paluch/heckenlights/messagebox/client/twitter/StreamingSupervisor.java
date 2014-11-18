package biz.paluch.heckenlights.messagebox.client.twitter;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.twitter.api.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import biz.paluch.heckenlights.messagebox.akka.SpringExtension;
import biz.paluch.heckenlights.messagebox.repository.TweetRepository;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
public class StreamingSupervisor {

    @Autowired
    private TweetRepository tweetRepository;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Stream stream;

    @Autowired
    private ListableBeanFactory context;

    @Autowired
    private ActorSystem system;

    @Autowired
    private SpringExtension springExtension;

    @Autowired
    private Twitter twitter;

    @Value("${twitter.filter}")
    private String filter;

    @Value("${twitter.streaming.enabled}")
    private boolean enabled;

    @PostConstruct
    public void postConstruct() {

        Props props = springExtension.props("supervisingActor");
        final ActorRef writeMessageActor = system.actorOf(props, "supervisingActor");

        logger.info("Prepare Stream");
        StreamListener streamListener = new StreamListener() {
            @Override
            public void onTweet(Tweet tweet) {

                writeMessageActor.tell(tweet, ActorRef.noSender());

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

    public static void main(String[] args) throws IOException
    {
        Properties p = new Properties();
        p.setProperty("k", "اليمن_البحرين");
        p.store(System.out, "");
    }
}
