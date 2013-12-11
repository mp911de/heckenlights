<?php
/**
 * Created by PhpStorm.
 * User: mark
 * Date: 11.12.13
 * Time: 21:21
 */

class PlaylistModel
{
    public $entries;

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


} 