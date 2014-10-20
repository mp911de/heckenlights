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
require_once 'RestApiClient.php';

class PlaylistAPI extends API
{

    public function __construct($request, $origin)
    {
        parent::__construct($request);
        $this->endpoint = 'playlist';

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
        $client = new RestApiClient(constant('backend'), '');

        $rawResponse = $client->send("GET", "/heckenlights?playStatus=ENQUEUED", ["Accept: application/json"], '', false);

        if (stripos($rawResponse->header, "HTTP/1.1 200") != 0) {
            throw new Exception("Bad Request");
        }

        return $this->createPlaylistModel($rawResponse->body);
    }

    private function createPlaylistModel($jsonData)
    {
        $json = json_decode($jsonData, true);
        $result = array();
        if (isset($json['playCommands']) && is_array($json['playCommands']['playCommand'])) {
            $wrapper = $json['playCommands'];
            $playcommands = $wrapper['playCommand'];

            $i = 0;
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
        }

        return $result;
    }
}

?>