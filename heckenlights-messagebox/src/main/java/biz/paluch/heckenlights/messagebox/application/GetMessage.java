package biz.paluch.heckenlights.messagebox.application;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import biz.paluch.heckenlights.messagebox.repository.MessageDocument;
import biz.paluch.heckenlights.messagebox.repository.MessageRepository;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Service
public class GetMessage {

    @Value("${image.height}")
    private int height;

    @Value("${image.width.min}")
    private int minWidth;

    @Value("${image.width.preroll:0}")
    private int widthPreroll;

    @Value("${image.width.postroll:0}")
    private int widthPostroll;

    @Inject
    private MessageRepository messageRepository;

    public String getFirstUnprocessedMessageId() {
        List<MessageDocument> messages = messageRepository.findTop10ByProcessedFalse();

        if (!messages.isEmpty()) {
            return messages.get(0).getId();
        }

        return null;
    }

    public String getMessage(String id) {
        MessageDocument messageDocument = messageRepository.findOne(id);
        if (messageDocument == null) {
            return null;
        }

        messageDocument.setProcessed(true);
        messageRepository.save(messageDocument);
        return messageDocument.getMessage();
    }

    public byte[] getImage(String id, String format) throws IOException {

        String title = getMessage(id);
        if (title == null) {

            return null;
        }

        List<String> parts = new ArrayList<>();
        parts.add(title);

        Renderer renderer = new Renderer(new Color(200, 200, 200));

        int width = Math.max(minWidth, renderer.getWidth(parts)) + widthPreroll + widthPostroll + 24;

        // We need a sample model for color images where the pixels are bytes, with three bands.
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE, width, height, 3);

        TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, null);
        Graphics2D graphics = tiledImage.createGraphics();

        BufferedImage image = ImageIO.read(new File("assets/note.png"));

        graphics.drawImage(image, (int) (widthPreroll + 1), 0, null);

        renderer.runGraphics(widthPreroll + 24, parts, graphics);
        graphics.dispose();

        return ImageEncoder.encode(format, tiledImage);
    }
}
