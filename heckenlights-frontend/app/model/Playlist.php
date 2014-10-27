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
    public $online;

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
     * @return mixed
     */
    public function getOnline()
    {
        return $this->online;
    }

    /**
     * @param mixed $online
     */
    public function setOnline($online)
    {
        $this->online = $online;
    }




} 