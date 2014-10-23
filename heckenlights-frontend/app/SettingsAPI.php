<?php

require_once 'AbstractAPI.php';
require_once 'settings.php';
require_once 'model/SettingsModel.php';
require_once 'RestApiClient.php';
require_once 'recaptchalib.php';

class SettingsAPI extends AbstractAPI
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
        $model = new SettingsModel();
        $model->setRecaptchaPublicKey(constant('recaptchaPublicKey'));

        if(array_key_exists("humanOrMachine", $_SESSION)){
            $model->setHumanOrMachine($_SESSION["humanOrMachine"]);
        }

        return $model;
    }

    protected function post(){

        $model = $this->get();
        if ($_POST["recaptcha_response_field"]) {
            $resp = recaptcha_check_answer (constant('recaptchaPrivateKey'),
                $_SERVER["REMOTE_ADDR"],
                $_POST["recaptcha_challenge_field"],
                $_POST["recaptcha_response_field"]);

            if ($resp->is_valid) {
                $_SESSION["humanOrMachine"] = "human";
                $model->setHumanOrMachine("human");
            } else {
                # set the error code so that we can display it
                $error = $resp->error;
                $_SESSION["humanOrMachine"] = "machine";
                $model->setHumanOrMachine("machine");
            }
        }
        session_commit();
        $model->setChallengeResponse($error);

        return $model;
    }
}

?>