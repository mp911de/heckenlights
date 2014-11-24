<?php

/*
 * jQuery File Upload Plugin PHP Class 8.2.3
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2010, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/MIT
 */

class UploadHandler
{

    protected $options;

    // PHP File Upload error message codes:
    // http://php.net/manual/en/features.file-upload.errors.php
    protected $error_messages = array(
        1 => 'The uploaded file exceeds the upload_max_filesize directive in php.ini',
        2 => 'The uploaded file exceeds the MAX_FILE_SIZE directive that was specified in the HTML form',
        3 => 'The uploaded file was only partially uploaded',
        4 => 'No file was uploaded',
        6 => 'Missing a temporary folder',
        7 => 'Failed to write file to disk',
        8 => 'A PHP extension stopped the file upload',
        'post_max_size' => 'The uploaded file exceeds the post_max_size directive in php.ini',
        'max_file_size' => 'File is too big',
        'min_file_size' => 'File is too small',
        'accept_file_types' => 'Filetype not allowed',
        'max_number_of_files' => 'Maximum number of files exceeded',
        'abort' => 'File upload aborted'
    );


    function __construct($options = null, $error_messages = null)
    {
        $this->response = array();
        $this->status = 200;
        $this->options = array(
            'script_url' => $this->get_full_url() . '/',
            'param_name' => 'files',
            // Set the following option to 'POST', if your server does not support
            // DELETE requests. This is a parameter sent to the client:
            'delete_type' => 'DELETE',

            // Read files in chunks to avoid memory limits when download_via_php
            // is enabled, set to 0 to disable chunked reading of files:
            'readfile_chunk_size' => 10 * 1024 * 1024, // 10 MiB
            // Defines which files (based on their names) are accepted for upload:
            'accept_file_types' => '/.+$/i',
            // The php.ini settings upload_max_filesize and post_max_size
            // take precedence over the following max_file_size setting:
            'max_file_size' => null,
            'min_file_size' => 1,
            // The maximum number of files for the upload directory:
            'max_number_of_files' => null,

            // Set the following option to false to enable resumable uploads:
            'discard_aborted_uploads' => true,

        );
        if ($options) {
            $this->options = $options + $this->options;
        }
        if ($error_messages) {
            $this->error_messages = $error_messages + $this->error_messages;
        }
    }

    public function invoke($callback)
    {
        switch ($this->get_server_var('REQUEST_METHOD')) {
            case 'PATCH':
            case 'PUT':
            case 'POST':
                $this->post($callback);
                break;
            default:
                $this->header('HTTP/1.1 405 Method Not Allowed');
        }
    }

    protected function get_full_url()
    {
        $https = !empty($_SERVER['HTTPS']) && strcasecmp($_SERVER['HTTPS'], 'on') === 0 ||
            !empty($_SERVER['HTTP_X_FORWARDED_PROTO']) &&
            strcasecmp($_SERVER['HTTP_X_FORWARDED_PROTO'], 'https') === 0;
        return
            ($https ? 'https://' : 'http://') .
            (!empty($_SERVER['REMOTE_USER']) ? $_SERVER['REMOTE_USER'] . '@' : '') .
            (isset($_SERVER['HTTP_HOST']) ? $_SERVER['HTTP_HOST'] : ($_SERVER['SERVER_NAME'] .
                ($https && $_SERVER['SERVER_PORT'] === 443 ||
                $_SERVER['SERVER_PORT'] === 80 ? '' : ':' . $_SERVER['SERVER_PORT']))) .
            substr($_SERVER['SCRIPT_NAME'], 0, strrpos($_SERVER['SCRIPT_NAME'], '/'));
    }

    protected function get_query_separator($url)
    {
        return strpos($url, '?') === false ? '?' : '&';
    }

    protected function set_additional_file_properties($file)
    {
    }

    // Fix for overflowing signed 32 bit integers,
    // works for sizes up to 2^32-1 bytes (4 GiB - 1):
    protected function fix_integer_overflow($size)
    {
        if ($size < 0) {
            $size += 2.0 * (PHP_INT_MAX + 1);
        }
        return $size;
    }

    protected function get_file_size($file_path, $clear_stat_cache = false)
    {
        if ($clear_stat_cache) {
            if (version_compare(PHP_VERSION, '5.3.0') >= 0) {
                clearstatcache(true, $file_path);
            } else {
                clearstatcache();
            }
        }
        return $this->fix_integer_overflow(filesize($file_path));
    }


    protected function count_file_objects()
    {
        return count($this->get_file_objects('is_valid_file_object'));
    }

    protected function get_error_message($error)
    {
        return isset($this->error_messages[$error]) ?
            $this->error_messages[$error] : $error;
    }

    function get_config_bytes($val)
    {
        $val = trim($val);
        $last = strtolower($val[strlen($val) - 1]);
        switch ($last) {
            case 'g':
                $val *= 1024;
            case 'm':
                $val *= 1024;
            case 'k':
                $val *= 1024;
        }
        return $this->fix_integer_overflow($val);
    }

    protected function validate($uploaded_file, $file, $error, $index)
    {
        if ($error) {
            $file->error = $this->get_error_message($error);
            return false;
        }
        $content_length = $this->fix_integer_overflow(
            (int)$this->get_server_var('CONTENT_LENGTH')
        );
        $post_max_size = $this->get_config_bytes(ini_get('post_max_size'));
        if ($post_max_size && ($content_length > $post_max_size)) {
            $file->error = $this->get_error_message('post_max_size');
            return false;
        }
        if (!preg_match($this->options['accept_file_types'], $file->name)) {
            $file->error = $this->get_error_message('accept_file_types');
            return false;
        }
        if ($uploaded_file && is_uploaded_file($uploaded_file)) {
            $file_size = $this->get_file_size($uploaded_file);
        } else {
            $file_size = $content_length;
        }
        if ($this->options['max_file_size'] && (
                $file_size > $this->options['max_file_size'] ||
                $file->size > $this->options['max_file_size'])
        ) {
            $file->error = $this->get_error_message('max_file_size');
            return false;
        }
        if ($this->options['min_file_size'] &&
            $file_size < $this->options['min_file_size']
        ) {
            $file->error = $this->get_error_message('min_file_size');
            return false;
        }

        return true;
    }

    protected function upcount_name_callback($matches)
    {
        $index = isset($matches[1]) ? ((int)$matches[1]) + 1 : 1;
        $ext = isset($matches[2]) ? $matches[2] : '';
        return ' (' . $index . ')' . $ext;
    }

    protected function upcount_name($name)
    {
        return preg_replace_callback(
            '/(?:(?: \(([\d]+)\))?(\.[^.]+))?$/',
            array($this, 'upcount_name_callback'),
            $name,
            1
        );
    }


    protected function fix_file_extension($file_path, $name, $size, $type, $error,
                                          $index, $content_range)
    {
        // Add missing file extension for known image types:
        if (strpos($name, '.') === false &&
            preg_match('/^image\/(gif|jpe?g|png)/', $type, $matches)
        ) {
            $name .= '.' . $matches[1];
        }
        if ($this->options['correct_image_extensions'] &&
            function_exists('exif_imagetype')
        ) {
            switch (@exif_imagetype($file_path)) {
                case IMAGETYPE_JPEG:
                    $extensions = array('jpg', 'jpeg');
                    break;
                case IMAGETYPE_PNG:
                    $extensions = array('png');
                    break;
                case IMAGETYPE_GIF:
                    $extensions = array('gif');
                    break;
            }
            // Adjust incorrect image file extensions:
            if (!empty($extensions)) {
                $parts = explode('.', $name);
                $extIndex = count($parts) - 1;
                $ext = strtolower(@$parts[$extIndex]);
                if (!in_array($ext, $extensions)) {
                    $parts[$extIndex] = $extensions[0];
                    $name = implode('.', $parts);
                }
            }
        }
        return $name;
    }

    protected function trim_file_name($file_path, $name, $size, $type, $error,
                                      $index, $content_range)
    {
        // Remove path information and dots around the filename, to prevent uploading
        // into different directories or replacing hidden system files.
        // Also remove control characters and spaces (\x00..\x20) around the filename:
        $name = trim(basename(stripslashes($name)), ".\x00..\x20");
        // Use a timestamp for empty filenames:
        if (!$name) {
            $name = str_replace('.', '-', microtime(true));
        }
        return $name;
    }

    protected function get_file_name($file_path, $name, $size, $type, $error,
                                     $index, $content_range)
    {
        $name = $this->trim_file_name($file_path, $name, $size, $type, $error,
            $index, $content_range);
        return $name;
    }

    protected function handle_form_data($file, $index)
    {
        // Handle form data, e.g. $_REQUEST['description'][$index]
    }


    protected function handle_file_upload($uploaded_file, $name, $size, $type, $error,
                                          $index = null, $content_range = null)
    {

        $file = new \stdClass();
        $file->name = $this->get_file_name($uploaded_file, $name, $size, $type, $error,
            $index, $content_range);

        $file->size = $this->fix_integer_overflow((int)$size);
        $file->type = $type;
        if ($this->validate($uploaded_file, $file, $error, $index)) {
            $this->handle_form_data($file, $index);
            $file->uploaded_file = $uploaded_file;
            if ($uploaded_file && is_uploaded_file($uploaded_file)) {
                // multipart/formdata uploads (POST method uploads)

                $handle = fopen($uploaded_file, "rb");
                $contents = '';
                while (!feof($handle)) {
                    $contents .= fread($handle, 8192);
                }
                fclose($handle);
                $file->contents = $contents;
            } else {

                $handle = fopen("php://input", "rb");
                $contents = '';
                while (!feof($handle)) {
                    $contents .= fread($handle, 8192);
                }
                fclose($handle);
                $file->contents = $contents;
            }
            $file_size = $this->get_file_size($uploaded_file, true);
            if ($file_size === $file->size) {

            } else {
                $file->size = $file_size;
                if (!$content_range && $this->options['discard_aborted_uploads']) {
                    $file->error = $this->get_error_message('abort');
                }
            }
            $this->set_additional_file_properties($file);
        }
        return $file;
    }

    protected function readfile($file_path)
    {
        $file_size = $this->get_file_size($file_path);
        $chunk_size = $this->options['readfile_chunk_size'];
        if ($chunk_size && $file_size > $chunk_size) {
            $handle = fopen($file_path, 'rb');
            while (!feof($handle)) {
                echo fread($handle, $chunk_size);
                @ob_flush();
                @flush();
            }
            fclose($handle);
            return $file_size;
        }
        return readfile($file_path);
    }

    protected function header($str)
    {
        header($str);
    }

    protected function get_server_var($id)
    {
        return isset($_SERVER[$id]) ? $_SERVER[$id] : '';
    }

    protected function get_version_param()
    {
        return isset($_GET['version']) ? basename(stripslashes($_GET['version'])) : null;
    }

    protected function get_singular_param_name()
    {
        return substr($this->options['param_name'], 0, -1);
    }

    protected function get_file_name_param()
    {
        $name = $this->get_singular_param_name();
        return isset($_REQUEST[$name]) ? basename(stripslashes($_REQUEST[$name])) : null;
    }

    protected function get_file_names_params()
    {
        $params = isset($_REQUEST[$this->options['param_name']]) ?
            $_REQUEST[$this->options['param_name']] : array();
        foreach ($params as $key => $value) {
            $params[$key] = basename(stripslashes($value));
        }
        return $params;
    }

    protected function send_content_type_header()
    {
        $this->header('Vary: Accept');
        $this->header('Content-type: application/json');
    }

    public function generate_response($content)
    {
        $this->response = $content;
        $this->head();
        if ($this->get_server_var('HTTP_CONTENT_RANGE')) {
            $files = isset($content[$this->options['param_name']]) ?
                $content[$this->options['param_name']] : null;
            if ($files && is_array($files) && is_object($files[0]) && $files[0]->size) {
                $this->header('Range: 0-' . (
                        $this->fix_integer_overflow((int)$files[0]->size) - 1
                    ));
            }
        }

        return $this;
    }

    public function get_response()
    {
        return $this->response;
    }

    public function head()
    {
        $this->header('Pragma: no-cache');
        $this->header('Cache-Control: no-store, no-cache, must-revalidate');
        $this->header('X-Content-Type-Options: nosniff');
        $this->send_content_type_header();
    }

    public function post($callback)
    {
        $upload = isset($_FILES[$this->options['param_name']]) ?
            $_FILES[$this->options['param_name']] : null;
        // Parse the Content-Disposition header, if available:
        $file_name = $this->get_server_var('HTTP_CONTENT_DISPOSITION') ?
            rawurldecode(preg_replace(
                '/(^[^"]+")|("$)/',
                '',
                $this->get_server_var('HTTP_CONTENT_DISPOSITION')
            )) : null;
        // Parse the Content-Range header, which has the following form:
        // Content-Range: bytes 0-524287/2000000
        $content_range = $this->get_server_var('HTTP_CONTENT_RANGE') ?
            preg_split('/[^0-9]+/', $this->get_server_var('HTTP_CONTENT_RANGE')) : null;
        $size = $content_range ? $content_range[3] : null;
        $files = array();

        if ($upload && is_array($upload['tmp_name'])) {
            // param_name is an array identifier like "files[]",
            // $_FILES is a multi-dimensional array:
            foreach ($upload['tmp_name'] as $index => $value) {
                $files[] = $this->handle_file_upload(
                    $upload['tmp_name'][$index],
                    $file_name ? $file_name : $upload['name'][$index],
                    $size ? $size : $upload['size'][$index],
                    $upload['type'][$index],
                    $upload['error'][$index],
                    $index,
                    $content_range
                );
            }
        } else {
            // param_name is a single object identifier like "file",
            // $_FILES is a one-dimensional array:
            $files[] = $this->handle_file_upload(
                isset($upload['tmp_name']) ? $upload['tmp_name'] : null,
                $file_name ? $file_name : (isset($upload['name']) ?
                    $upload['name'] : null),
                $size ? $size : (isset($upload['size']) ?
                    $upload['size'] : $this->get_server_var('CONTENT_LENGTH')),
                isset($upload['type']) ?
                    $upload['type'] : $this->get_server_var('CONTENT_TYPE'),
                isset($upload['error']) ? $upload['error'] : null,
                null,
                $content_range
            );
        }

        $result = new \stdClass();
        $result->response = null;
        $result->success = true;
        $result->encode = true;
        $result->status = 200;
        foreach ($files as $file) {

            if (isset($callback)) {
                $callback($file, $result);
            }

            if (isset($file->uploaded_file) && file_exists($file->uploaded_file)) {
                unlink($file->uploaded_file);
            }

            $file->contents = "";
            $file->uploaded_file = "";
            unset($file->contents);
            unset($file->uploaded_file);
        }

        if ($result->success) {
            if ($result->response === null) {
                $result->response = array($this->options['param_name'] => $files);
            }
        } else {
            if ($result->status === 200) {
                $this->status = 400;
            } else {
                $this->status = $result->status;
            }
        }
        return $this->generate_response($result->response, $result->encode);
    }

}