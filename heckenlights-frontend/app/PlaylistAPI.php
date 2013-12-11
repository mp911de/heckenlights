<?php
/**
 * Created by PhpStorm.
 * User: mark
 * Date: 11.12.13
 * Time: 21:12
 */

require_once 'API.php';
require_once 'settings.php';
require_once 'PlaylistEntry.php';
require_once 'PlaylistModel.php';

class PlaylistAPI extends API
{

    public function __construct($request, $origin)
    {
        parent::__construct($request);

        // Abstracted out for example
    }


    /**
     * Example of an Endpoint
     */
    protected function playlist()
    {
        if ($this->method == 'GET') {
            return $this->getPlaylist();
        } else {
            return "Only accepts GET requests";
        }
    }


    private function getPlaylist()
    {
        global $backend;
        $client = new RestApiClient($backend, '');

        $rawResponse = $client->send("GET", "heckenlights", "Accept: application/json", '', false);

        if (stripos($rawResponse->header, "HTTP/1.1 200") !== false) {
            throw new Exception("Bad Request");
        }

        return $this->createPlaylistModel($rawResponse->body);
    }

    private function createPlaylistModel($jsonData)
    {
        $json = json_decode($jsonData, true);
        $playcommands = $json['playCommands']['playCommand'];

        $result = array();
        $i=0;
        foreach ($playcommands as $playcommand) {


            $entry = new PlaylistEntry();
            $entry->setId($playcommand['@id']);
            $entry->setDuration($playcommand['duration']);
            $entry->setPlayStatus($playcommand['playStatus']);

            if (array_key_exists('trackName', $playcommand)) {
                $entry->setTrackName($playcommand['trackName']);
            }

            if (array_key_exists('timeToStart', $playcommand)) {
                $entry->setTimeToStart($playcommand['timeToStart']);
            }

            $result[$i++] = $entry;
        }

        return $result;
    }
}

?>