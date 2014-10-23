<?php

/**
 * Created by PhpStorm.
 * User: mark
 * Date: 11.12.13
 * Time: 21:21
 */
class AuthenticationModel
{
    public $recaptchaPublicKey;
    public $humanOrMachine = "machine";
    public $challengeResponse = "";

    /**
     * @return mixed
     */
    public function getRecaptchaPublicKey()
    {
        return $this->recaptchaPublicKey;
    }

    /**
     * @param mixed $recaptchaPublicKey
     */
    public function setRecaptchaPublicKey($recaptchaPublicKey)
    {
        $this->recaptchaPublicKey = $recaptchaPublicKey;
    }

    /**
     * @return string
     */
    public function getHumanOrMachine()
    {
        return $this->humanOrMachine;
    }

    /**
     * @param string $humanOrMachine
     */
    public function setHumanOrMachine($humanOrMachine)
    {
        $this->humanOrMachine = $humanOrMachine;
    }

    /**
     * @return string
     */
    public function getChallengeResponse()
    {
        return $this->challengeResponse;
    }

    /**
     * @param string $challengeResponse
     */
    public function setChallengeResponse($challengeResponse)
    {
        $this->challengeResponse = $challengeResponse;
    }


} 