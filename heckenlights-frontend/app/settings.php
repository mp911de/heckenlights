<?php
#############################
# Global Settings
#############################

# Backend Base Uri
define('backend', 'http://127.0.0.1:8080');

# API Uri
define('apiBase', 'api/v1/');
define('presetFileBase', '/srv');


if (file_exists('settings-override.php')) {
    require 'settings-override.php';
} else {
    define('recaptchaPublicKey', '');
    define('recaptchaPrivateKey', '');
}
?>