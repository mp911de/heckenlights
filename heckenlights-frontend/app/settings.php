<?php
#############################
# Global Settings
#############################

# Backend Base Uri
define('backend', 'http://localhost:8080');

# API Uri
define('apiBase', 'api/v1/');


if (file_exists('settings-override.php')) {
    require 'settings-override.php';
} else {
    define('recaptchaPublicKey', '');
    define('recaptchaPrivateKey', '');
}
?>