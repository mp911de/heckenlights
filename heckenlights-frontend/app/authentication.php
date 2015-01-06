<?php

require_once 'model/Authentication.php';
require_once 'lib/RestApiClient.php';
require_once 'lib/UploadHandler.php';

define('MACHINE', "machine");
define('HUMAN', "human");
define('RECAPTCHA_PUBLIC_KEY', 'recaptchaPublicKey');
define('RECAPTCHA_PRIVATE_KEY', 'recaptchaPrivateKey');
define('RECAPTCHA_CHALLENGE_FIELD', "recaptcha_challenge_field");
define('RECAPTCHA_RESPONSE_FIELD', "recaptcha_response_field");
define('HUMAN_OR_MACHINE_KEY', "humanOrMachine");
define('PRESET_SUBMITTED', "presetSubmitted");

function getAuthentication($session)
{
    $model = new Authentication();
    $model->setRecaptchaPublicKey(constant(RECAPTCHA_PUBLIC_KEY));
    $model->setPresetSubmitted(wasPresetSubmitted($session));

    /*if (array_key_exists(HUMAN_OR_MACHINE_KEY, $session)) {
        $model->setHumanOrMachine($session[HUMAN_OR_MACHINE_KEY]);
    }*/
    $model->setHumanOrMachine("human");

    return $model;
}

function isAuthenticated($session)
{
    if (array_key_exists(HUMAN_OR_MACHINE_KEY, $session)) {
        if ($session[HUMAN_OR_MACHINE_KEY] === HUMAN) {
            return true;
        }
    }

    return true;
}

function wasPresetSubmitted($session)
{
    if (array_key_exists(PRESET_SUBMITTED, $session)) {
        $count = $session[PRESET_SUBMITTED];
        if (is_numeric($count) && $count >= 10) {
            return true;
        }
    }

    return false;
}

function setPresetSubmittedFlag(&$session)
{
    if (array_key_exists(PRESET_SUBMITTED, $session)) {
        $session[PRESET_SUBMITTED]++;
    } else {
        $session[PRESET_SUBMITTED] = 1;
    }
}

function authenticate($post, &$session, $remoteAddr)
{
    $model = getAuthentication($session);
    if ($post[RECAPTCHA_RESPONSE_FIELD]) {
        $resp = recaptchaSiteVerify(constant(RECAPTCHA_PRIVATE_KEY),
            $post[RECAPTCHA_RESPONSE_FIELD], $remoteAddr);

        if (is_array($resp) && array_key_exists("success", $resp) && $resp["success"]) {
            $session["humanOrMachine"] = HUMAN;
            $model->setHumanOrMachine(HUMAN);
        } else {
            # set the error code so that we can display it
            $session["humanOrMachine"] = MACHINE;
            $model->setHumanOrMachine(MACHINE);
        }

        if(is_array($resp) && array_key_exists("error-codes", $resp)){
            $model->setChallengeResponse(implode($resp["error-codes"]));
        }
    }
    return $model;
}

function recaptchaSiteVerify($secret, $response, $remoteip)
{

    $data = "secret=" . urlencode($secret) . "&response=" . urlencode($response) . "&remoteip=" . urlencode($remoteip);
    $url = "https://www.google.com/recaptcha/api/siteverify";
    $httpHeaders = array("User-Agent:" => "PHP", "Content-Length" => strlen($data));

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $httpHeaders);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_HEADER, 1);
    curl_setopt($ch, CURLOPT_BINARYTRANSFER, 1);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);

    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 1);
    curl_setopt($ch, CURLOPT_TIMEOUT, 5);

    $chResponse = curl_exec($ch);
    $headerSize = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
    curl_close($ch);

    $httpResponse = new HttpResponse($chResponse, $headerSize);

    $header = $httpResponse->header;
    if (is_array($header)) {
        $header = implode($header);
    }

    if (strlen(strstr($header, "HTTP/1.1 200")) > 0) {
        $result = json_decode($httpResponse->body, true);
        if(array_key_exists("success", $result)){
            return $result;
        }
    }
    return array("success" => false, "error-codes" => array());
}

?>