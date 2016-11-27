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
     * POST /playlist/preset
     */
    protected function post()
    {
        session_start();
        apache_note("sessionid", session_id());
        if (sizeof($this->args) == 1 && $this->args[0] == 'queue') {
            return submitMidiFile($this, $_SESSION);
        }

        if (sizeof($this->args) == 1 && $this->args[0] == 'preset') {
            $input = json_decode(file_get_contents('php://input'), true);
            if (is_array($input) && array_key_exists('presetfile', $input)) {
                $result = submitPreset($this, $input['presetfile'], $_SESSION);
                setPresetSubmittedFlag($_SESSION);
                session_commit();
                return $result;
            }
        }
        throw new InvalidArgumentException();
    }
}

?>