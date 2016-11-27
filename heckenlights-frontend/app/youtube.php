<?php
require_once 'settings.php';
require_once 'lib/RestApiClient.php';

$redis = new Redis();
$redis->pconnect('127.0.0.1', 6379);

$key = constant('keyspace') . ':' . 'youtube-streaming-id';
$streamingId = $redis->get($key);

if ($streamingId == FALSE) {
    $streamingId = getYouTubeStreamingId();
    $redis->set($key, $streamingId, 120);
}

function getYouTubeStreamingId()
{
    $client = new RestApiClient(constant('backend'), '');
    try {
        $rawResponse = $client->send("GET", "/youtube-streaming-id", ["Accept: text/plain"], '', false);
    } catch (Exception $e) {

        return "";
    }
    if (stripos($rawResponse->header, HTTP_1_1_200) != 0) {
        throw new Exception("Bad Request");
    }

    return createPlaylistModel($rawResponse->body);
}

?>
<iframe width="720" height="404" src="https://www.youtube.com/embed/<?php echo $streamingId; ?>&autoplay=1"
        frameborder="0" allowfullscreen></iframe>

