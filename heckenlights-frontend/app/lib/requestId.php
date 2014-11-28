<?php

$requestId = gethostname() . "." . uniqid();
apache_note("requestid", $requestId);

?>