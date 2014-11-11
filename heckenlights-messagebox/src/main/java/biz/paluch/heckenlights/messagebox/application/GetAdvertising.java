package biz.paluch.heckenlights.messagebox.application;

import org.springframework.stereotype.Service;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Service
public class GetAdvertising {

    public byte[] getAdvertising(String format) throws IOException {

        ParameterBlock parameterBlock = new ParameterBlock();
        parameterBlock.add("assets/heckenlights-advertising.png");
        RenderedOp image = JAI.create("fileload", parameterBlock);

		return ImageEncoder.encode(format, image);
	}
}
