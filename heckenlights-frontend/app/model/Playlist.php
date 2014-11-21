<?php
/**
 * Created by PhpStorm.
 * User: mark
 * Date: 11.12.13
 * Time: 21:21
 */

class Playlist
{
    public $entries;
    public $online = false;
    public $queueOpen = false;

    /**
     * @param mixed $entries
     */
    public function setEntries($entries)
    {
        $this->entries = $entries;
    }

    /**
     * @return mixed
     */
    public function getEntries()
    {
        return $this->entries;
    }

    /**
     * @return boolean
     */
    public function getOnline()
    {
        return $this->online;
    }

    /**
     * @param boolean $online
     */
    public function setOnline($online)
    {
        $this->online = $online;
    }

    /**
     * @return boolean
     */
    public function getQueueOpen()
    {
        return $this->queueOpen;
    }

    /**
     * @param boolean $queueOpen
     */
    public function setQueueOpen($queueOpen)
    {
        $this->queueOpen = $queueOpen;
    }






} 