package biz.paluch.heckenlights.messagebox.application;

import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import biz.paluch.heckenlights.messagebox.model.TweetSummary;
import biz.paluch.heckenlights.messagebox.repository.TweetDocument;
import biz.paluch.heckenlights.messagebox.repository.TweetRepository;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Service
public class GetTweet {

    @Inject
    private TweetRepository tweetRepository;

    @Value("${twitter.image.height}")
    private int height;

    @Value("${twitter.image.width.min}")
    private int minWidth;

    @Value("${twitter.image.width.preroll:0}")
    private int widthPreroll;

    @Value("${twitter.image.width.postroll:}")
    private int widthPostroll;

    public TweetSummary getFirstUnprocessedTweet() {

        List<TweetDocument> tweets = tweetRepository.findTop10ByProcessedFalseOrderByReceivedAsc();
        if (tweets.isEmpty()) {
            return null;
        }

        TweetDocument tweetDocument = tweets.get(0);

        return toTweetSummary(tweetDocument);
    }

    public TweetSummary getTweet(long id) {

        TweetDocument document = tweetRepository.findOne(id);
        if (document == null) {
            return null;
        }
        document.setProcessed(true);
        tweetRepository.save(document);

        return toTweetSummary(document);
    }

    private TweetSummary toTweetSummary(TweetDocument tweetDocument) {

        TweetSummary result = new TweetSummary();
        result.setId(tweetDocument.getId());
        result.setMessage(tweetDocument.getMessage());
        result.setProcessed(tweetDocument.isProcessed());
        result.setReceived(tweetDocument.getReceived());
        result.setSender(tweetDocument.getSender());
        return result;
    }

    public byte[] getTweetImage(long id, String format) throws IOException {

        TweetSummary tweet = getTweet(id);
        if (tweet == null) {
            return null;
        }

        List<String> parts = new ArrayList<>();
        parts.add(tweet.getSender() + ": ");
        parts.add(tweet.getMessage());

        Renderer renderer = new Renderer(Color.cyan);

        int width = Math.max(minWidth, renderer.getWidth(parts)) + widthPreroll + widthPostroll;

        // We need a sample model for color images where the pixels are bytes, with three bands.
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE, width, height, 3);

        TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, null);
        Graphics2D graphics = tiledImage.createGraphics();
        graphics.setPaint(Color.black);
        graphics.fillRect(0, 0, width, height);

        renderer.runGraphics(widthPreroll, parts, graphics);
        graphics.dispose();

        return ImageEncoder.encode(format, tiledImage);
    }

}
