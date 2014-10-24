<?php

/**
 * Created by PhpStorm.
 * User: mark
 * Date: 23.10.14
 * Time: 13:39
 */
class EnqueueRequest
{
    public $enqueuedCommandId = '';
    public $trackName;
    public $playStatus;
    public $message;
    public $durationToPlay = 0;
    public $submitStatus;

    /**
     * @return int
     */
    public function getDurationToPlay()
    {
        return $this->durationToPlay;
    }

    /**
     * @param int $durationToPlay
     */
    public function setDurationToPlay($durationToPlay)
    {
        $this->durationToPlay = $durationToPlay;
    }

    /**
     * @return string
     */
    public function getEnqueuedCommandId()
    {
        return $this->enqueuedCommandId;
    }

    /**
     * @param string $enqueuedCommandId
     */
    public function setEnqueuedCommandId($enqueuedCommandId)
    {
        $this->enqueuedCommandId = $enqueuedCommandId;
    }

    /**
     * @return mixed
     */
    public function getMessage()
    {
        return $this->message;
    }

    /**
     * @param mixed $message
     */
    public function setMessage($message)
    {
        $this->message = $message;
    }

    /**
     * @return mixed
     */
    public function getPlayStatus()
    {
        return $this->playStatus;
    }

    /**
     * @param mixed $playStatus
     */
    public function setPlayStatus($playStatus)
    {
        $this->playStatus = $playStatus;
    }

    /**
     * @return mixed
     */
    public function getSubmitStatus()
    {
        return $this->submitStatus;
    }

    /**
     * @param mixed $submitStatus
     */
    public function setSubmitStatus($submitStatus)
    {
        $this->submitStatus = $submitStatus;
    }

    /**
     * @return mixed
     */
    public function getTrackName()
    {
        return $this->trackName;
    }

    /**
     * @param mixed $trackName
     */
    public function setTrackName($trackName)
    {
        $this->trackName = $trackName;
    }


}