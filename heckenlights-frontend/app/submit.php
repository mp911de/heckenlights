<?php
require_once('PlaylistAPI.php');
require_once('UploadHandler.php');

if (!array_key_exists('HTTP_ORIGIN', $_SERVER)) {
    $_SERVER['HTTP_ORIGIN'] = $_SERVER['SERVER_NAME'];
}

session_start();
$callback = function ($file, $result) {
    $client = new RestApiClient(constant('backend'), '');

    $headers = ["Accept: application/json", "X-Submission-Host: " . $_SERVER['REMOTE_ADDR'],
        "X-External-SessionId: " . session_id(), "X-Request-FileName: " . $file->name];

    $rawResponse = $client->send("PUT", "/heckenlights", $headers, $file->contents, false);

    $result->encode = false;

    $header = $rawResponse->header;
    if (is_array($header)) {
        $header = implode($header);
    }

    if (strlen(strstr($header, "HTTP/1.1 200")) > 0) {
        $enqueue = json_decode($rawResponse->body);
        if (isset($enqueue) && isset($enqueue->enqueued)) {
            if ($enqueue->enqueued->playStatus == 'ERROR') {
                $result->success = false;
            }
        }
    } else {
        $result->success = false;
    }

    $result->response = json_encode(json_decode($rawResponse->body));
};

$upload_handler = new UploadHandler(null, true, null, $callback);
?>