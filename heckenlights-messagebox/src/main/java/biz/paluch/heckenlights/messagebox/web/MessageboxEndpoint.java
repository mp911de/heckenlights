package biz.paluch.heckenlights.messagebox.web;

import java.io.IOException;

import org.jboss.resteasy.util.HttpHeaderNames;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import biz.paluch.heckenlights.messagebox.application.DispatchNextRequest;
import biz.paluch.heckenlights.messagebox.application.GetAdvertising;
import biz.paluch.heckenlights.messagebox.application.GetCurrentTitle;
import biz.paluch.heckenlights.messagebox.application.GetMessage;
import biz.paluch.heckenlights.messagebox.application.GetTweet;
import biz.paluch.heckenlights.messagebox.model.DispatchAction;
import biz.paluch.heckenlights.messagebox.model.TweetSummary;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
@Controller
@ResponseBody
@RequiredArgsConstructor
@Slf4j
public class MessageboxEndpoint {

    @NonNull
    DispatchNextRequest dispatchNextRequest;

    @NonNull
    GetTweet getTweet;

    @NonNull
    GetAdvertising getAdvertising;

    @NonNull
    GetCurrentTitle getCurrentTitle;

    @NonNull
    GetMessage getMessage;

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
        return respondImage(getTweet.getTweetText(tweetId), getTweet.getTweetImage(tweetId, "png"));
    }

    @RequestMapping(value = "/tweets/{tweetId}.ppm", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getTweetPpm(@PathVariable long tweetId) throws IOException {
        return respondImage(getTweet.getTweetText(tweetId), getTweet.getTweetImage(tweetId, "ppm"));
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
        return respondImage(getCurrentTitle.getCurrentTitle(), getCurrentTitle.getCurrentTitleImage("png"));
    }

    @RequestMapping(value = "/title/current.ppm", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getCurrentTitlePpm() throws IOException {
        HttpHeaders headers = new HttpHeaders();

        return respondImage(getCurrentTitle.getCurrentTitle(), getCurrentTitle.getCurrentTitleImage("ppm"));
    }

    @RequestMapping(value = "/messages/{messageId}", method = RequestMethod.GET, produces = { MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<String> getMessage(@PathVariable String messageId) {
        String result = getMessage.getMessage(messageId);
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/messages/{messageId}.png", method = RequestMethod.GET, produces = { MediaType.IMAGE_PNG_VALUE })
    public ResponseEntity<byte[]> getMessagePng(@PathVariable String messageId) throws IOException {

        return respondImage(getMessage.getMessage(messageId), getMessage.getImage(messageId, "png"));
    }

    @RequestMapping(value = "/messages/{messageId}.ppm", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getMessagePpm(@PathVariable String messageId) throws IOException {
        return respondImage(getMessage.getMessage(messageId), getMessage.getImage(messageId, "ppm"));
    }

    private ResponseEntity<byte[]> respondImage(String content, byte[] result) {
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        HttpHeaders headers = new HttpHeaders();

        headers.add("X-Content", content);
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    private ResponseEntity<Void> buildResponse(UriComponentsBuilder uriComponentsBuilder, DispatchAction dispatchAction,
            String typeSuffix) {
        HttpHeaders headers = new HttpHeaders();

        log.info("Dispatching to " + dispatchAction);
        if (dispatchAction == DispatchAction.Tweet) {

            TweetSummary firstUnprocessedTweet = getTweet.getFirstUnprocessedTweet();
            if (firstUnprocessedTweet != null) {
                uriComponentsBuilder.path("tweets/").path(Long.toString(firstUnprocessedTweet.getId()) + typeSuffix);

                headers.add(HttpHeaderNames.LOCATION, uriComponentsBuilder.build().toUriString());
                return new ResponseEntity<>(headers, HttpStatus.TEMPORARY_REDIRECT);
            }
        }

        if (dispatchAction == DispatchAction.Message) {

            String messageId = getMessage.getFirstUnprocessedMessageId();
            if (messageId != null) {
                uriComponentsBuilder.path("messages/").path(messageId + typeSuffix);

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
