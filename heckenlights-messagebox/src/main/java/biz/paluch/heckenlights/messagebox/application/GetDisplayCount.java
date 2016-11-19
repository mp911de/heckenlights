package biz.paluch.heckenlights.messagebox.application;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Service;

import biz.paluch.heckenlights.messagebox.model.DisplayCount;
import biz.paluch.heckenlights.messagebox.repository.DisplayCountDocument;
import biz.paluch.heckenlights.messagebox.repository.DisplayCountRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Service
@RequiredArgsConstructor
public class GetDisplayCount {

    public static final String COUNT = "COUNT";
    public static final String RATIO = "RATIO";

    public static final String COUNT_FORMAT = "yyyy-MM-dd_HH";

    @NonNull
    DisplayCountRepository displayCountRepository;

    public DisplayCount getDisplayCount() {

        DisplayCountDocument count = getCountDocument();

        return toDisplayCount(count);

    }

    private DisplayCountDocument getCountDocument() {

        String key = getCountKey();
        DisplayCountDocument count = displayCountRepository.findOne(key);

        if (count == null) {
            count = new DisplayCountDocument();
            count.setId(key);
            displayCountRepository.save(count);
        }
        return count;
    }

    public String getCountKey() {
        SimpleDateFormat format = new SimpleDateFormat(COUNT_FORMAT);
        return COUNT + "_" + format.format(new Date());
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
        result.setMessages(displayCountDocument.getMessages());

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
        displayCountDocument.setMessages(displayCount.getMessages());

        displayCountRepository.save(displayCountDocument);
    }
}
