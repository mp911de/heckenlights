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
define('SUBMIT_RESULT_NOT_FOUND', 'NOT_FOUND');
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

        $enqueueResult = createResult($result, $rawResponse);

        $result->response = $enqueueResult;
    };

    $upload_handler = new UploadHandler();
    $upload_handler->invoke($callback);
    $api->setStatus($upload_handler->status);

    return $upload_handler->get_response();
}

function submitPreset($api, $presetfile, $session)
{
    global $presetfiles;

    if (!isAuthenticated($session)) {
        $enqueueResult = new PlaylistEntry();
        $enqueueResult->setSubmitStatus(SUBMIT_RESULT_UNAUTHENTICATED);
        $api->setStatus(401);
        return $enqueueResult;
    }

    if (wasPresetSubmitted($session)) {
        $enqueueResult = new PlaylistEntry();
        $enqueueResult->setSubmitStatus(SUBMIT_RESULT_QUOTA);
        $api->setStatus(429);
        return $enqueueResult;
    }

    if (!array_key_exists($presetfile, $presetfiles)) {
        $enqueueResult = new PlaylistEntry();
        $enqueueResult->setSubmitStatus(SUBMIT_RESULT_NOT_FOUND);
        $enqueueResult->setTrackName($presetfile);
        $api->setStatus(400);
        return $enqueueResult;
    }

    $content = getFileContent($presetfile);
    if ($content == null) {
        $enqueueResult = new PlaylistEntry();
        $enqueueResult->setSubmitStatus(SUBMIT_RESULT_NOT_FOUND);
        $enqueueResult->setTrackName($presetfile);
        $api->setStatus(404);
        return $enqueueResult;
    }

    $result = new \stdClass();
    $result->response = null;
    $result->success = true;
    $result->encode = true;
    $result->status = 200;

    $client = new RestApiClient(constant('backend'), '');

    $headers = ["Content-Type: application/octet-stream", "Accept: application/json", "X-Submission-Host: " . $_SERVER['REMOTE_ADDR'],
        "X-External-SessionId: " . session_id(), "X-Request-FileName: " . $presetfile];

    $rawResponse = $client->send(POST, "/", $headers, $content);

    $enqueueResult = createResult($result, $rawResponse);

    if ($result->success === true) {
        setPresetSubmittedFlag($session);
    } else {
        if ($result->status === 200) {
            $result->status = 400;
        }
    }

    $api->setStatus($result->status);

    return $enqueueResult;
}

function getFileContent($filename)
{
    $fullname = constant('presetFileBase') . $filename;
    if (file_exists($fullname)) {

        $handle = fopen($fullname, "rb");
        $contents = '';
        while (!feof($handle)) {
            $contents .= fread($handle, 8192);
        }
        fclose($handle);
        return $contents;
    }

    return null;
}

/**
 * @param $result
 * @param $rawResponse
 * @return EnqueueRequest
 */
function createResult($result, $rawResponse)
{
    $header = $rawResponse->header;
    if (is_array($header)) {
        $header = implode($header);
    }
    $enqueueResult = new EnqueueRequest();

    if (strlen(strstr($header, "HTTP/1.1 200")) > 0) {
        $result->success = true;
        $enqueue = json_decode($rawResponse->body, true);

        if (array_key_exists('durationToPlay', $enqueue)) {
            $enqueueResult->setDurationToPlay($enqueue['durationToPlay']);
        }

        if (array_key_exists('trackName', $enqueue)) {
            $enqueueResult->setTrackName($enqueue['trackName']);
        }

        if (array_key_exists('fileName', $enqueue)) {
            $enqueueResult->setFileName($enqueue['fileName']);
        }

        if (array_key_exists('message', $enqueue)) {
            $enqueueResult->setMessage($enqueue['message']);
        }

        if (isset($enqueue) && isset($enqueue->playStatus)) {
            $enqueueResult->setPlayStatus($enqueue->playStatus);

            if ($enqueue->playStatus == PLAYSTATUS_ERROR) {
                $result->success = false;
                return $enqueueResult;
            }
            return $enqueueResult;
        }
        return $enqueueResult;
    } else {

        $result->success = false;
        if (strlen(strstr($header, "HTTP/1.1 423")) > 0) {
            $result->status = 423;
            $enqueueResult->setSubmitStatus(SUBMIT_RESULT_OFFLINE);
            return $enqueueResult;
        } else if (strlen(strstr($header, "HTTP/1.1 429")) > 0) {
            $result->status = 429;
            $enqueueResult->setSubmitStatus(SUBMIT_RESULT_QUOTA);
            return $enqueueResult;
        } else if (strlen(strstr($header, "HTTP/1.1 400")) > 0) {
            $result->status = 400;
            $enqueue = json_decode($rawResponse->body, true);

            if (is_array($enqueue)) {
                if (array_key_exists('message', $enqueue)) {
                    $enqueueResult->setMessage($enqueue['message']);
                }

                if (isset($enqueue) && isset($enqueue->playStatus)) {
                    $enqueueResult->setPlayStatus($enqueue->playStatus);
                }
                $enqueueResult->setSubmitStatus(SUBMIT_RESULT_ERROR);
                return $enqueueResult;
            }
            return $enqueueResult;
        } else {
            $result->status = 500;
            return $enqueueResult;
        }
    }
}

function getPlaylist()
{
    $client = new RestApiClient(constant('backend'), '');
    try {
        $rawResponse = $client->send("GET", "/?playStatus=ENQUEUED", ["Accept: application/json"], '', false);
    } catch (Exception $e) {

        if (strpos($e->getMessage(), 'Connection refused')) {
            return createPlaylistModel("{ playCommands: [], online: false, queueOpen: false, backend: \"down\"}");
        }

        if (strpos($e->getMessage(), 'timed out')) {
            return createPlaylistModel("{ playCommands: [], online: false, queueOpen: false, backend: \"timeout\"}");
        }

        throw $e;
    }
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

        if ($playlist->getOnline()) {
            foreach ($playcommands as $playcommand) {

                $entry = new PlaylistEntry();
                $entry->setId($playcommand[ID]);
                $entry->setDuration($playcommand[DURATION]);
                $entry->setPlayStatus($playcommand[PLAY_STATUS]);

                if (PLAYSTATUS_PLAYING === $playcommand[PLAY_STATUS]) {
                    $entry->setPlaying(true);
                }

                if (array_key_exists('trackName', $playcommand)) {
                    $entry->setTrackName($playcommand['trackName']);
                }

                if (array_key_exists('fileName', $playcommand)) {
                    $entry->setFileName($playcommand['fileName']);
                }

                if (array_key_exists('timeToStart', $playcommand)) {
                    $entry->setTimeToStart($playcommand['timeToStart']);
                }

                if (array_key_exists(REMAINING, $playcommand)) {
                    $entry->setRemaining($playcommand[REMAINING]);
                }

                $result[] = $entry;
            }
        }
    }
    $playlist->setEntries($result);
    return $playlist;
}

?>