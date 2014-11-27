package biz.paluch.heckenlights.messagebox.application;

import biz.paluch.heckenlights.messagebox.model.DispatchAction;
import biz.paluch.heckenlights.messagebox.model.DisplayCount;
import biz.paluch.heckenlights.messagebox.model.TweetSummary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.inject.Inject;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Service
public class DispatchNextRequest {

    @Inject
    private GetDisplayCount getDisplayCount;

    @Inject
    private GetTweet getTweet;

    @Inject
    private GetCurrentTitle getCurrentTitle;

    @Inject
    private GetMessage getMessage;

    public DispatchAction getDispatchAction() {

        DisplayCount displayCount = getDisplayCount.getDisplayCount();
        DisplayCount ratio = getDisplayCount.getRatio();

        double tweets = displayCount.getTweets();
        double advertising = displayCount.getAdvertising();
        double title = displayCount.getTitle();

        double ratioTweets = divIfNotZero(tweets, ratio.getTweets());
        double ratioAdvertising = divIfNotZero(advertising, ratio.getAdvertising());
        double ratioTitle = divIfNotZero(title, ratio.getTitle());

        if (ratio.getMessages() != 0) {
            String messageId = getMessage.getFirstUnprocessedMessageId();

            if (StringUtils.hasText(messageId)) {
                displayCount.setMessages(displayCount.getMessages() + 1);
                getDisplayCount.update(displayCount);
                return DispatchAction.Message;
            }
        }

        if (ratio.getTitle() != 0 && (ratioTitle < ratioAdvertising || ratioTitle < ratioTweets)) {
            String titleText = getCurrentTitle.getCurrentTitle();

            if (StringUtils.hasText(titleText)) {
                displayCount.setTitle(displayCount.getTitle() + 1);
                getDisplayCount.update(displayCount);
                return DispatchAction.Title;
            }
        }

        if (ratio.getTweets() != 0 && (ratioTweets < ratioAdvertising || ratioTweets < ratioTitle)) {
            TweetSummary firstUnprocessedTweet = getTweet.getFirstUnprocessedTweet();
            if (firstUnprocessedTweet != null) {
                displayCount.setTweets(displayCount.getTweets() + 1);
                getDisplayCount.update(displayCount);
                return DispatchAction.Tweet;
            }
        }

        displayCount.setAdvertising(displayCount.getAdvertising() + 1);
        getDisplayCount.update(displayCount);

        return DispatchAction.Advertising;

    }

    private double divIfNotZero(double base, double div) {
        if (div != 0) {
            return base / div;
        }
        return -1;
    }
}
