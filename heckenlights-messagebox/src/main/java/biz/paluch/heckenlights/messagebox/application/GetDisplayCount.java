package biz.paluch.heckenlights.messagebox.application;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import biz.paluch.heckenlights.messagebox.model.DisplayCount;
import biz.paluch.heckenlights.messagebox.repository.DisplayCountDocument;
import biz.paluch.heckenlights.messagebox.repository.DisplayCountRepository;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Service
public class GetDisplayCount {

    public static final String COUNT = "COUNT";
    public static final String RATIO = "RATIO";

    public static final String COUNT_FORMAT = "yyyy-MM-dd_HH";

    @Inject
    private DisplayCountRepository displayCountRepository;


    public DisplayCount getDisplayCount() {

        DisplayCountDocument count = getCountDocument();

        return toDisplayCount(count);

    }

    private DisplayCountDocument getCountDocument() {

        SimpleDateFormat format = new SimpleDateFormat(COUNT_FORMAT);
        String key = COUNT + "_" + format.format(new Date());
        DisplayCountDocument count = displayCountRepository.findOne(key);

        if (count == null) {
            count = new DisplayCountDocument();
            count.setId(key);
            displayCountRepository.save(count);
        }
        return count;
    }

    public DisplayCount getRatio() {
        DisplayCountDocument ratio = getRatioDocument();
        return toDisplayCount(ratio);
    }

    private DisplayCount toDisplayCount(DisplayCountDocument displayCountDocument) {
        DisplayCount result = new DisplayCount();
        result.setAdvertising(displayCountDocument.getAdvertising());
        result.setTitle(displayCountDocument.getTitle());
        result.setTweets(displayCountDocument.getTweets());
        return result;
    }

    private DisplayCountDocument getRatioDocument() {
        DisplayCountDocument ratio = displayCountRepository.findOne(RATIO);
        if (ratio == null) {
            ratio = new DisplayCountDocument(RATIO, 1, 3, 1);
            displayCountRepository.save(ratio);
        }
        return ratio;
    }

	public void update(DisplayCount displayCount) {
		DisplayCountDocument displayCountDocument = getCountDocument();

		displayCountDocument.setAdvertising(displayCount.getAdvertising());
		displayCountDocument.setTitle(displayCount.getTitle());
		displayCountDocument.setTweets(displayCount.getTweets());

		displayCountRepository.save(displayCountDocument);

	}
}
