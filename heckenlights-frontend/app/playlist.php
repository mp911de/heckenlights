<?php

define('REMAINING', 'remaining');
define('ID', 'id');
define('DURATION', 'duration');
define('DURATION_TO_PLAY', 'durationToPlay');
define('PLAY_STATUS', 'playStatus');
define('TRACK_NAME', 'trackName');
define('PLAY_COMMANDS', 'playCommands');
define('ONLINE', 'online');
define('QUEUE_OPEN', 'queueOpen');
define('PLAYSTATUS_ERROR', 'ERROR');
define('PLAYSTATUS_PLAYING', 'PLAYING');
define('SUBMIT_RESULT_SUCCESS', 'SUCCESS');
define('SUBMIT_RESULT_ERROR', 'ERROR');
define('SUBMIT_RESULT_UNAUTHENTICATED', 'UNAUTHENTICATED');
define('SUBMIT_RESULT_QUOTA', 'QUOTA');
define('SUBMIT_RESULT_OFFLINE', 'OFFLINE');
define('POST', "POST");

require_once 'model/PlaylistEntry.php';
require_once 'model/Playlist.php';
require_once 'model/EnqueueRequest.php';
require_once 'lib/RestApiClient.php';
require_once 'lib/UploadHandler.php';
require_once 'authentication.php';

function submitMidiFile($api, $session)
{

    if (!isAuthenticated($session)) {
        $enqueueResult = new PlaylistEntry();
        $enqueueResult->setSubmitStatus(SUBMIT_RESULT_UNAUTHENTICATED);
        $api->setStatus(401);
        return $enqueueResult;
    }

    $callback = function ($file, $result) {
        $client = new RestApiClient(constant('backend'), '');

        $headers = ["Content-Type: application/octet-stream", "Accept: application/json", "X-Submission-Host: " . $_SERVER['REMOTE_ADDR'],
            "X-External-SessionId: " . session_id(), "X-Request-FileName: " . $file->name];

        $rawResponse = $client->send(POST, "/", $headers, $file->contents);
        $result->encode = false;

        $header = $rawResponse->header;
        if (is_array($header)) {
            $header = implode($header);
        }

        $enqueueResult = new EnqueueRequest();

        if (strlen(strstr($header, "HTTP/1.1 200")) > 0) {
            $enqueue = json_decode($rawResponse->body, true);

            if (array_key_exists(DURATION_TO_PLAY, $enqueue)) {
                $enqueueResult->setDurationToPlay($enqueue[DURATION_TO_PLAY]);
            }

            if (array_key_exists(TRACK_NAME, $enqueue)) {
                $enqueueResult->setTrackName($enqueue[TRACK_NAME]);
            }

            if (array_key_exists(MESSAGE, $enqueue)) {
                $enqueueResult->setTrackName($enqueue[MESSAGE]);
            }

            if (isset($enqueue) && isset($enqueue->playStatus)) {
                $enqueueResult->setPlayStatus($enqueue->playStatus);

                if ($enqueue->playStatus == PLAYSTATUS_ERROR) {
                    $result->success = false;
                }
            }
        } else {

            $result->success = false;
            if (strlen(strstr($header, "HTTP/1.1 423")) > 0) {
                $result->status = 423;
                $enqueueResult->setSubmitStatus(SUBMIT_RESULT_OFFLINE);
            } else if (strlen(strstr($header, "HTTP/1.1 429")) > 0) {
                $result->status = 429;
                $enqueueResult->setSubmitStatus(SUBMIT_RESULT_QUOTA);
            } else if (strlen(strstr($header, "HTTP/1.1 400")) > 0) {
                $result->status = 400;
                $enqueue = json_decode($rawResponse->body, true);

                if (is_array($enqueue)) {
                    if (array_key_exists(MESSAGE, $enqueue)) {
                        $enqueueResult->setTrackName($enqueue[MESSAGE]);
                    }

                    if (isset($enqueue) && isset($enqueue->playStatus)) {
                        $enqueueResult->setPlayStatus($enqueue->playStatus);
                    }
                    $enqueueResult->setSubmitStatus(SUBMIT_RESULT_ERROR);
                }
            } else {
                $result->status = 500;
            }
        }


        $result->response = $enqueueResult;
    };

    $upload_handler = new UploadHandler();
    $upload_handler->invoke($callback);
    $api->setStatus($upload_handler->status);

    return $upload_handler->get_response();
}

function getPlaylist()
{
    $client = new RestApiClient(constant('backend'), '');
    $rawResponse = $client->send("GET", "/?playStatus=ENQUEUED", ["Accept: application/json"], '', false);

    if (stripos($rawResponse->header, HTTP_1_1_200) != 0) {
        throw new Exception("Bad Request");
    }

    return createPlaylistModel($rawResponse->body);
}

function createPlaylistModel($jsonData)
{
    $json = json_decode($jsonData, true);
    $result = array();
    $playlist = new Playlist();
    $playlist->setOnline(false);

    if (isset($json[PLAY_COMMANDS]) && is_array($json[PLAY_COMMANDS])) {
        $playcommands = $json[PLAY_COMMANDS];
        $playlist->setOnline($json[ONLINE]);
        $playlist->setQueueOpen($json[QUEUE_OPEN]);

        foreach ($playcommands as $playcommand) {

            $entry = new PlaylistEntry();
            $entry->setId($playcommand[ID]);
            $entry->setDuration($playcommand[DURATION]);
            $entry->setPlayStatus($playcommand[PLAY_STATUS]);

            if (PLAYSTATUS_PLAYING === $playcommand[PLAY_STATUS]) {
                $entry->setPlaying(true);
            }

            if (array_key_exists(TRACK_NAME, $playcommand)) {
                $entry->setTrackName($playcommand[TRACK_NAME]);
            }

            if (array_key_exists(REMAINING, $playcommand)) {
                $entry->setRemaining($playcommand[REMAINING]);
            }

            $result[] = $entry;
        }
    }
    $playlist->setEntries($result);
    return $playlist;
}

?>