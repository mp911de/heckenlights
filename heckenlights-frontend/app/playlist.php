<?php
require_once('PlaylistAPI.php');

if (!array_key_exists('HTTP_ORIGIN', $_SERVER)) {
    $_SERVER['HTTP_ORIGIN'] = $_SERVER['SERVER_NAME'];
}

try {
    $API = new PlaylistAPI($_REQUEST['request'], $_SERVER['HTTP_ORIGIN']);
    echo json_encode($API->processAPI());
} catch (Exception $e) {
    echo json_encode(Array('error' => $e->getMessage()));
}