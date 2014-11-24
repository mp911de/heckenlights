<?php

require_once 'AbstractAPI.php';

class PresetAPI extends AbstractAPI
{

    public function __construct($request)
    {
        parent::__construct($request);
    }

    /**
     * GET /presets
     */
    protected function get()
    {
        session_start();
        global $presets;
        // filename:title
        $model = $presets;

        return $model;
    }
}

?>