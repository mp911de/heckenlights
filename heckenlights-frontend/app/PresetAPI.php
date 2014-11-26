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
        $result = array();

        foreach ($presets as $key => $value) {
            $fullname = constant('presetFileBase') . $key;
            if (file_exists($fullname)) {
                $result[$key] = $value;
            }
        }

        $model = $result;
        return $model;
    }
}

?>