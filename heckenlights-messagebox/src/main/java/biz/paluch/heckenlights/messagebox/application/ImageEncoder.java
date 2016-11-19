package biz.paluch.heckenlights.messagebox.application;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.sun.media.jai.codec.PNGEncodeParam;
import com.sun.media.jai.codec.PNMEncodeParam;
import com.sun.media.jai.codecimpl.PNGImageEncoder;
import com.sun.media.jai.codecimpl.PNMImageEncoder;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class ImageEncoder {

    public static byte[] encode(String format, RenderedImage image) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        if ("ppm".equalsIgnoreCase(format)) {
            PNMEncodeParam encodeParam = new PNMEncodeParam();
            encodeParam.setRaw(true);

            PNMImageEncoder encoder = new PNMImageEncoder(buffer, encodeParam);
            encoder.encode(image);
            return buffer.toByteArray();
        }

        PNGEncodeParam encodeParam = new PNGEncodeParam.RGB();
        PNGImageEncoder encoder = new PNGImageEncoder(buffer, encodeParam);
        encoder.encode(image);
        return buffer.toByteArray();
    }
}
