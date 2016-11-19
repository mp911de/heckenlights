package biz.paluch.heckenlights.messagebox.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import biz.paluch.heckenlights.messagebox.client.midirelay.MidiRelayClient;
import biz.paluch.heckenlights.messagebox.client.midirelay.PlayerStateRepresentation;
import biz.paluch.heckenlights.messagebox.client.midirelay.PlayerStateTrackRepresentation;
import biz.paluch.heckenlights.messagebox.model.DispatchAction;
import biz.paluch.heckenlights.messagebox.repository.DisplayCountDocument;
import biz.paluch.heckenlights.messagebox.repository.DisplayCountRepository;
import biz.paluch.heckenlights.messagebox.repository.MessageRepository;
import biz.paluch.heckenlights.messagebox.repository.TweetDocument;
import biz.paluch.heckenlights.messagebox.repository.TweetRepository;

@RunWith(MockitoJUnitRunner.class)
public class DispatchNextRequestTest {

    private DispatchNextRequest sut;

    @InjectMocks
    private GetDisplayCount getDisplayCount;

    @InjectMocks
    private GetTweet getTweet;

    @InjectMocks
    private GetCurrentTitle getCurrentTitle;

    @InjectMocks
    private GetMessage getMessage;

    @Mock
    private DisplayCountRepository displayCountRepository;

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MidiRelayClient midiRelayClient;

    @Before
    public void before() throws Exception {

        DisplayCountDocument ratio = getDisplayCountDocument();
        when(displayCountRepository.findOne(GetDisplayCount.RATIO)).thenReturn(ratio);

        sut = new DispatchNextRequest(getDisplayCount, getTweet, getCurrentTitle, getMessage);

    }

    private DisplayCountDocument getDisplayCountDocument() {
        DisplayCountDocument ratio = new DisplayCountDocument();
        ratio.setAdvertising(1);
        ratio.setTitle(1);
        ratio.setTweets(1);
        return ratio;
    }

    @Test
    public void noInputThenAdvertising() throws Exception {
        DispatchAction result = sut.getDispatchAction();
        assertThat(result).isEqualTo(DispatchAction.Advertising);
    }

    @Test
    public void advertisingRateHighTweet() throws Exception {

        TweetDocument tweetDocument = new TweetDocument();
        DisplayCountDocument displayCountDocument = new DisplayCountDocument();
        displayCountDocument.setAdvertising(1);

        when(tweetRepository.findTop10ByProcessedFalseOrderByReceivedAsc()).thenReturn(Lists.newArrayList(tweetDocument));

        when(displayCountRepository.findOne(getDisplayCount.getCountKey())).thenReturn(displayCountDocument);

        DispatchAction result = sut.getDispatchAction();
        assertThat(result).isEqualTo(DispatchAction.Tweet);
    }

    @Test
    public void advertisingRateHighButNoTweet() throws Exception {

        DisplayCountDocument displayCountDocument = new DisplayCountDocument();
        displayCountDocument.setAdvertising(1);

        when(displayCountRepository.findOne(getDisplayCount.getCountKey())).thenReturn(displayCountDocument);

        DispatchAction result = sut.getDispatchAction();
        assertThat(result).isEqualTo(DispatchAction.Advertising);
    }

    @Test
    public void advertisingAndTweetRateHighTitle() throws Exception {

        TweetDocument tweetDocument = new TweetDocument();
        DisplayCountDocument displayCountDocument = new DisplayCountDocument();
        displayCountDocument.setTweets(3);
        displayCountDocument.setAdvertising(1);

        PlayerStateRepresentation playerStateRepresentation = new PlayerStateRepresentation();
        playerStateRepresentation.setRunning(true);

        PlayerStateTrackRepresentation track = new PlayerStateTrackRepresentation();
        track.setFileName("dummy");
        playerStateRepresentation.setTrack(track);

        when(tweetRepository.findTop10ByProcessedFalseOrderByReceivedAsc()).thenReturn(Lists.newArrayList(tweetDocument));

        when(midiRelayClient.getState()).thenReturn(playerStateRepresentation);

        when(displayCountRepository.findOne(getDisplayCount.getCountKey())).thenReturn(displayCountDocument);

        DispatchAction result = sut.getDispatchAction();
        assertThat(result).isEqualTo(DispatchAction.Title);
    }
}