package biz.paluch.heckenlights.messagebox.client.twitter;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.stereotype.Component;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import biz.paluch.heckenlights.messagebox.repository.TweetDocument;
import biz.paluch.heckenlights.messagebox.repository.TweetRepository;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
@Scope("prototype")
public class WriteMessageActor extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), "TaskProcessor");

    @Autowired
    private TweetRepository tweetRepository;

    @Override
    public void onReceive(Object message) throws Exception {

        Tweet tweet = (Tweet) message;
        log.info("Incoming tweet {} {}", tweet.getFromUser(), tweet.getText());

        TweetDocument document = new TweetDocument();
        document.setId(tweet.getId());
        document.setReceived(new Date());
        document.setSender(tweet.getFromUser());
        document.setMessage(tweet.getText());
        document.setProcessed(false);

        tweetRepository.save(document);
    }
}
