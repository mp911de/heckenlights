package biz.paluch.heckenlights.messagebox.rest;

import biz.paluch.heckenlights.messagebox.application.DispatchNextRequest;
import biz.paluch.heckenlights.messagebox.application.GetAdvertising;
import biz.paluch.heckenlights.messagebox.application.GetCurrentTitle;
import biz.paluch.heckenlights.messagebox.application.GetTweet;
import biz.paluch.heckenlights.messagebox.model.DispatchAction;
import biz.paluch.heckenlights.messagebox.model.TweetSummary;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
@RestController
public class MessageboxRessource {

    @Inject
    private DispatchNextRequest dispatchNextRequest;

    @Inject
    private GetTweet getTweet;

    @Inject
    private GetAdvertising getAdvertising;

    @Inject
    private GetCurrentTitle getCurrentTitle;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/dispatch", method = RequestMethod.GET)
    public ResponseEntity<Void> dispatch(UriComponentsBuilder uriComponentsBuilder) {
        DispatchAction dispatchAction = dispatchNextRequest.getDispatchAction();
        return buildResponse(uriComponentsBuilder, dispatchAction, "");
    }

    @RequestMapping(value = "/dispatch.png", method = RequestMethod.GET)
    public ResponseEntity<Void> dispatchPng(UriComponentsBuilder uriComponentsBuilder) {
        DispatchAction dispatchAction = dispatchNextRequest.getDispatchAction();
        return buildResponse(uriComponentsBuilder, dispatchAction, ".png");
    }

    @RequestMapping(value = "/dispatch.ppm", method = RequestMethod.GET)
    public ResponseEntity<Void> dispatchPpm(UriComponentsBuilder uriComponentsBuilder) {
        DispatchAction dispatchAction = dispatchNextRequest.getDispatchAction();
        return buildResponse(uriComponentsBuilder, dispatchAction, ".ppm");
    }

    @RequestMapping(value = "/advertising.png", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> advertisingPng() throws IOException {
        return new ResponseEntity<>(getAdvertising.getAdvertising("png"), HttpStatus.OK);
    }

    @RequestMapping(value = "/advertising.ppm", method = RequestMethod.GET)
    public ResponseEntity<byte[]> advertisingPpm() throws IOException {
        return new ResponseEntity<>(getAdvertising.getAdvertising("ppm"), HttpStatus.OK);
    }

    @RequestMapping(value = "/tweets/{tweetId}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_XML_VALUE })
    public ResponseEntity<TweetSummary> getTweet(@PathVariable long tweetId) {
        TweetSummary tweetSummary = getTweet.getTweet(tweetId);
        if (tweetSummary == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(tweetSummary, HttpStatus.OK);
    }

    @RequestMapping(value = "/tweets/{tweetId}.png", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getTweetPng(@PathVariable long tweetId) throws IOException {
        byte[] bytes = getTweet.getTweetImage(tweetId, "png");
        if (bytes == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Content", getTweet.getTweetText(tweetId));
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/tweets/{tweetId}.ppm", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getTweetPpm(@PathVariable long tweetId) throws IOException {
        byte[] bytes = getTweet.getTweetImage(tweetId, "ppm");
        if (bytes == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Content", getTweet.getTweetText(tweetId));

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/title/current", method = RequestMethod.GET, produces = { MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<String> getCurrentTitle() {
        String result = getCurrentTitle.getCurrentTitle();
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/title/current.png", method = RequestMethod.GET, produces = { MediaType.IMAGE_PNG_VALUE })
    public ResponseEntity<byte[]> getCurrentTitlePng() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Content", getCurrentTitle.getCurrentTitle());
        return new ResponseEntity<>(getCurrentTitle.getCurrentTitleImage("png"), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/title/current.ppm", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getCurrentTitlePpm() throws IOException {
        HttpHeaders headers = new HttpHeaders();

        headers.add("X-Content", getCurrentTitle.getCurrentTitle());
        return new ResponseEntity<>(getCurrentTitle.getCurrentTitleImage("ppm"), headers, HttpStatus.OK);
    }

    private ResponseEntity<Void> buildResponse(UriComponentsBuilder uriComponentsBuilder, DispatchAction dispatchAction,
            String typeSuffix) {
        HttpHeaders headers = new HttpHeaders();

        logger.info("Dispatching to " + dispatchAction);
        if (dispatchAction == DispatchAction.Tweet) {

            TweetSummary firstUnprocessedTweet = getTweet.getFirstUnprocessedTweet();
            if (firstUnprocessedTweet != null) {
                uriComponentsBuilder.path("tweets/").path(Long.toString(firstUnprocessedTweet.getId()) + typeSuffix);

                headers.add(HttpHeaderNames.LOCATION, uriComponentsBuilder.build().toUriString());
                return new ResponseEntity<>(headers, HttpStatus.TEMPORARY_REDIRECT);
            }
        }

        if (dispatchAction == DispatchAction.Title) {
            uriComponentsBuilder.path("title/current" + typeSuffix);

            headers.add(HttpHeaderNames.LOCATION, uriComponentsBuilder.build().toUriString());
            return new ResponseEntity<>(headers, HttpStatus.TEMPORARY_REDIRECT);
        }

        uriComponentsBuilder.path("advertising" + typeSuffix);
        headers.add(HttpHeaderNames.LOCATION, uriComponentsBuilder.build().toUriString());
        return new ResponseEntity<>(headers, HttpStatus.TEMPORARY_REDIRECT);
    }
}
