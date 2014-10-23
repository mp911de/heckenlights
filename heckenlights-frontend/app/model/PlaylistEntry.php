<?php

/**
 * Created by PhpStorm.
 * User: mark
 * Date: 11.12.13
 * Time: 21:22
 */
class PlaylistEntry
{
    public $id = '';
    public $trackName = '';
    public $playStatus = '';
    public $duration = 0;
    public $timeToStart = 0;
    public $playing = false;
    public $remaining = 0;

    /**
     * @param int $duration
     */
    public function setDuration($duration)
    {
        $this->duration = $duration;
    }

    /**
     * @return int
     */
    public function getDuration()
    {
        return $this->duration;
    }

    /**
     * @param string $id
     */
    public function setId($id)
    {
        $this->id = $id;
    }

    /**
     * @return string
     */
    public function getId()
    {
        return $this->id;
    }

    /**
     * @param string $playStatus
     */
    public function setPlayStatus($playStatus)
    {
        $this->playStatus = $playStatus;
    }

    /**
     * @return string
     */
    public function getPlayStatus()
    {
        return $this->playStatus;
    }

    /**
     * @param int $timeToStart
     */
    public function setTimeToStart($timeToStart)
    {
        $this->timeToStart = $timeToStart;
    }

    /**
     * @return int
     */
    public function getTimeToStart()
    {
        return $this->timeToStart;
    }

    /**
     * @param string $trackName
     */
    public function setTrackName($trackName)
    {
        $this->trackName = $trackName;
    }

    /**
     * @return string
     */
    public function getTrackName()
    {
        return $this->trackName;
    }

    /**
     * @return boolean
     */
    public function isPlaying()
    {
        return $this->playing;
    }

    /**
     * @param boolean $playing
     */
    public function setPlaying($playing)
    {
        $this->playing = $playing;
    }

    /**
     * @return int
     */
    public function getRemaining()
    {
        return $this->remaining;
    }

    /**
     * @param int $remaining
     */
    public function setRemaining($remaining)
    {
        $this->remaining = $remaining;
    }


}