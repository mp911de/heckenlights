<?php

require_once 'AbstractAPI.php';
require_once 'authentication.php';

class AuthenticationAPI extends AbstractAPI
{

    public function __construct($request)
    {
        parent::__construct($request);
    }

    /**
     * GET /settings
     */
    protected function get()
    {
        session_start();
        $model = getAuthentication($_SESSION);

        return $model;
    }

    protected function post(){

        session_start();
        $model = authenticate($_POST, $_SESSION, $_SERVER["REMOTE_ADDR"]);
        session_commit();
        return $model;
    }
}

?>