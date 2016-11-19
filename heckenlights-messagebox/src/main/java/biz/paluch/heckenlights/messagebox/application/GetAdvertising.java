package biz.paluch.heckenlights.messagebox.application;

import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Service
@Slf4j
public class GetAdvertising {

    public byte[] getAdvertising(String format) throws IOException {

        File assets = new File("assets");
        List<File> files = new ArrayList<>(FileUtils.listFiles(assets,
                FileFilterUtils.prefixFileFilter("heckenlights-advertising"), TrueFileFilter.INSTANCE));

        int randomIndex = BigDecimal.valueOf(Math.random() * files.size()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue()
                % files.size();

        File file = files.get(randomIndex);

        ParameterBlock parameterBlock = new ParameterBlock();
        parameterBlock.add(file.getCanonicalPath());
        RenderedOp image = JAI.create("fileload", parameterBlock);

        log.info("Advertising: " + file + ", format: " + format);

        return ImageEncoder.encode(format, image);
    }
}
