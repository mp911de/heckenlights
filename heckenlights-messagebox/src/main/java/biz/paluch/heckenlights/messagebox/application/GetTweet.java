package biz.paluch.heckenlights.messagebox.application;

import biz.paluch.heckenlights.messagebox.model.TweetSummary;
import biz.paluch.heckenlights.messagebox.repository.TweetDocument;
import biz.paluch.heckenlights.messagebox.repository.TweetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Service
public class GetTweet {

    @Inject
    private TweetRepository tweetRepository;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${image.height}")
    private int height;

    @Value("${image.width.min}")
    private int minWidth;

    @Value("${image.width.preroll:0}")
    private int widthPreroll;

    @Value("${image.width.postroll:}")
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
        logger.info("Retrieving Tweet " + document.getSender() + ": " + document.getMessage());

        document.setProcessed(true);
        tweetRepository.save(document);

        return toTweetSummary(document);
    }

    public String getTweetText(long id)
    {
        TweetDocument document = tweetRepository.findOne(id);
        if (document == null) {
            return null;
        }

        String result = document.getSender() + ": " + document.getMessage();
        result = result.replace('\r', ' ').replace('\n', ' ').replace("  ", " ");
        return result;
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

        BufferedImage image = ImageIO.read(new File("assets/twitter-bird.png"));

        graphics.drawImage(image, (int) (widthPreroll + 1), 0, null);

        renderer.runGraphics(widthPreroll + 24, parts, graphics);
        graphics.dispose();

        return ImageEncoder.encode(format, tiledImage);
    }


}
