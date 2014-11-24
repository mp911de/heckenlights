<?php
require_once 'settings.php';
require_once 'lib/requestId.php';

if (!array_key_exists('HTTP_ORIGIN', $_SERVER)) {
    $_SERVER['HTTP_ORIGIN'] = $_SERVER['SERVER_NAME'];
}

$urlPath = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$apiBase = constant('apiBase');
$resourcePath = substr($urlPath, strpos($urlPath, $apiBase) + strlen($apiBase));

$knownResources = [
    'playlist' => 'PlaylistAPI',
    'authentication' => 'AuthenticationAPI'
];

$validResource = false;
$handler = null;

preg_match('/([^ \\/]+)/i', $resourcePath, $matches, PREG_OFFSET_CAPTURE);

if (is_array($matches) && sizeof($matches) == 2) {
    $resource = $matches[1][0];
    if (array_key_exists($resource, $knownResources)) {
        $validResource = true;
        $handler = $knownResources[$resource];
    }
}

if (!$validResource || $handler === null) {
    header('HTTP/1.1 400 Bad Request');
    echo '<html><body><h1>400 Bad Request</h1></body></html>';
} else {

    require "$handler.php";
    $apiHandler = new $handler($resourcePath);
    $result = $apiHandler->processAPI();
    if ($result != null) {
        echo $result;
    }
}

?>