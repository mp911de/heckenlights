<?php

require_once 'AbstractAPI.php';
require_once 'settings.php';
require_once 'model/PlaylistEntry.php';
require_once 'model/PlaylistModel.php';
require_once 'RestApiClient.php';
require_once 'UploadHandler.php';

class PlaylistAPI extends AbstractAPI
{

    public function __construct($request)
    {
        parent::__construct($request);
    }


    /**
     * GET /playlist
     */
    protected function get()
    {
        session_start();
        return $this->getPlaylist();
    }

    /**
     * POST /playlist/queue
     */
    protected function post()
    {
        session_start();
        if (sizeof($this->args) == 1 && $this->args[0] == 'queue') {
            return $this->submitMidiFile();
        }
        throw new InvalidArgumentException();
    }


    private function getPlaylist()
    {
        $client = new RestApiClient(constant('backend'), '');
        $rawResponse = $client->send("GET", "/?playStatus=ENQUEUED", ["Accept: application/json"], '', false);

        if (stripos($rawResponse->header, "HTTP/1.1 200") != 0) {
            throw new Exception("Bad Request");
        }

        return $this->createPlaylistModel($rawResponse->body);
    }

    private function createPlaylistModel($jsonData)
    {
        $json = json_decode($jsonData, true);
        $result = array();

        if (isset($json['playCommands']) && is_array($json['playCommands'])) {
            $playcommands = $json['playCommands'];
            foreach ($playcommands as $playcommand) {

                $entry = new PlaylistEntry();
                $entry->setId($playcommand['id']);
                $entry->setDuration($playcommand['duration']);
                $entry->setPlayStatus($playcommand['playStatus']);

                if ("PLAYING" === $playcommand['playStatus']) {
                    $entry->setPlaying(true);
                }

                if (array_key_exists('trackName', $playcommand)) {
                    $entry->setTrackName($playcommand['trackName']);
                }

                if (array_key_exists('trackName', $playcommand)) {
                    $entry->setTrackName($playcommand['trackName']);
                }

                if (array_key_exists('remaining', $playcommand)) {
                    $entry->setRemaining($playcommand['remaining']);
                }

                $result[] = $entry;
            }
        }
        return $result;
    }

    private function submitMidiFile()
    {
        $callback = function ($file, $result) {
            $client = new RestApiClient(constant('backend'), '');

            $headers = ["Content-Type: application/octet-stream", "Accept: application/json", "X-Submission-Host: " . $_SERVER['REMOTE_ADDR'],
                "X-External-SessionId: " . session_id(), "X-Request-FileName: " . $file->name];

            $rawResponse = $client->send("POST", "/", $headers, $file->contents);
            $result->encode = false;

            $header = $rawResponse->header;
            if (is_array($header)) {
                $header = implode($header);
            }

            if (strlen(strstr($header, "HTTP/1.1 200")) > 0) {
                $enqueue = json_decode($rawResponse->body);
                if (isset($enqueue) && isset($enqueue->playStatus)) {
                    if ($enqueue->playStatus == 'ERROR') {
                        $result->success = false;
                    }
                }
            } else {

                $result->success = false;
                if (strlen(strstr($header, "HTTP/1.1 400")) > 0) {
                    $result->status = 400;
                } else {
                    $result->status = 500;
                }
            }

            $result->response = json_decode($rawResponse->body);
        };

        $upload_handler = new UploadHandler();
        $upload_handler->invoke($callback);
        $this->status = $upload_handler->status;

        return $upload_handler->get_response();
    }

}

?>