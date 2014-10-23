<?php

require_once 'AbstractAPI.php';
require_once 'settings.php';
require_once 'playlist.php';


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
        return getPlaylist();
    }

    /**
     * POST /playlist/queue
     */
    protected function post()
    {
        session_start();
        if (sizeof($this->args) == 1 && $this->args[0] == 'queue') {
            return submitMidiFile($this, $_SESSION);
        }
        throw new InvalidArgumentException();
    }

}

?>