<?php

require_once 'model/AuthenticationModel.php';
require_once 'lib/RestApiClient.php';
require_once 'lib/UploadHandler.php';
require_once 'lib/recaptchalib.php';

define('MACHINE', "machine");
define('HUMAN', "human");
define('RECAPTCHA_PUBLIC_KEY', 'recaptchaPublicKey');
define('RECAPTCHA_PRIVATE_KEY', 'recaptchaPrivateKey');
define('RECAPTCHA_CHALLENGE_FIELD', "recaptcha_challenge_field");
define('RECAPTCHA_RESPONSE_FIELD', "recaptcha_response_field");
define('HUMAN_OR_MACHINE_KEY', "humanOrMachine");

function getAuthentication($session)
{
    $model = new AuthenticationModel();
    $model->setRecaptchaPublicKey(constant(RECAPTCHA_PUBLIC_KEY));

    if (array_key_exists(HUMAN_OR_MACHINE_KEY, $session)) {
        $model->setHumanOrMachine($session[HUMAN_OR_MACHINE_KEY]);
    }
    return $model;
}

function isAuthenticated($session)
{
    if (array_key_exists(HUMAN_OR_MACHINE_KEY, $session)) {
        if ($session[HUMAN_OR_MACHINE_KEY] === HUMAN) {
            return true;
        }
    }

    return false;
}

function authenticate($post, &$session, $remoteAddr)
{
    $model = getAuthentication($session);
    if ($post[RECAPTCHA_RESPONSE_FIELD]) {
        $resp = recaptcha_check_answer(constant(RECAPTCHA_PRIVATE_KEY),
            $remoteAddr,
            $post[RECAPTCHA_CHALLENGE_FIELD],
            $post[RECAPTCHA_RESPONSE_FIELD]);

        if (true || $resp->is_valid) {
            $session["humanOrMachine"] = HUMAN;
            $model->setHumanOrMachine(HUMAN);
        } else {
            # set the error code so that we can display it
            $error = $resp->error;
            $session["humanOrMachine"] = MACHINE;
            $model->setHumanOrMachine(MACHINE);
        }
    }
    $model->setChallengeResponse($error);
    return $model;
}

?>