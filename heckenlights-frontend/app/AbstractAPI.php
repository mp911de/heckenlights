<?php

abstract class AbstractAPI
{
    /**
     * Property: method
     * The HTTP method this request was made in, either GET, POST, PUT or DELETE
     */
    protected $method = '';
    /**
     * Property: endpoint
     * The Model requested in the URI. eg: /files
     */
    protected $endpoint = '';
    /**
     * Property: args
     * Any additional URI components after the endpoint and verb have been removed, in our
     * case, an integer ID for the resource. eg: /<endpoint>/<verb>/<arg0>/<arg1>
     * or /<endpoint>/<arg0>
     */
    protected $args = Array();
    /**
     * Property: file
     * Stores the input of the PUT request
     */
    protected $file = Null;

    protected $status = 200;

    /**
     * Constructor: __construct
     * Allow for CORS, assemble and pre-process the data
     */
    public function __construct($request)
    {
        //header("Access-Control-Allow-Orgin: *");
        //header("Access-Control-Allow-Methods: *");

        $this->args = explode('/', rtrim($request, '/'));
        $this->endpoint = array_shift($this->args);

        $this->method = $_SERVER['REQUEST_METHOD'];
        if ($this->method == 'POST' && array_key_exists('HTTP_X_HTTP_METHOD', $_SERVER)) {
            if ($_SERVER['HTTP_X_HTTP_METHOD'] == 'DELETE') {
                $this->method = 'DELETE';
            } else if ($_SERVER['HTTP_X_HTTP_METHOD'] == 'PUT') {
                $this->method = 'PUT';
            } else {
                throw new Exception("Unexpected Header");
            }
        }

        switch ($this->method) {
            case 'DELETE':
            case 'POST':
                $this->request = $this->_cleanInputs($_POST);
                break;
            case 'GET':
                $this->request = $this->_cleanInputs($_GET);
                break;
            case 'PUT':
                $this->request = $this->_cleanInputs($_GET);
                break;
            default:
                $this->_response('Invalid Method', 405);
                break;
        }
    }

    public function processAPI()
    {
        try {
            $methodname = strtolower($this->method);
            if ((int)method_exists($this, $methodname) > 0) {
                return $this->_response($this->{$methodname}($this->args));
            }

            throw new InvalidArgumentException();
        } catch (InvalidArgumentException $e) {
            $this->status = 400;
            return $this->_error('Bad Request' . ($e->getMessage() != null ? ": " . $e->getMessage() : ""));
        } catch (Exception $e) {
            $this->status = 500;
            return $this->_error($e->getMessage());
        }
    }

    private function _error($message)
    {
        return $this->_response(Array('error' => $message, 'status' => $this->status));
    }


    private function _response($data)
    {
        if ($data == null && $this->status == 200) {
            $this->status = 204;
            header("HTTP/1.1 " . $this->status . " " . $this->_requestStatus($this->status));
            return null;
        } else {
            header("HTTP/1.1 " . $this->status . " " . $this->_requestStatus($this->status));
            header("Content-Type: application/json");
            return json_encode($data);
        }
    }

    private function _cleanInputs($data)
    {
        $clean_input = Array();
        if (is_array($data)) {
            foreach ($data as $k => $v) {
                $clean_input[$k] = $this->_cleanInputs($v);
            }
        } else {
            $clean_input = trim(strip_tags($data));
        }
        return $clean_input;
    }

    private function _requestStatus($code)
    {
        $status = array(
            100 => 'Continue',
            101 => 'Switching Protocols',
            200 => 'OK',
            201 => 'Created',
            202 => 'Accepted',
            203 => 'Non-Authoritative Information',
            204 => 'No Content',
            205 => 'Reset Content',
            206 => 'Partial Content',
            300 => 'Multiple Choices',
            301 => 'Moved Permanently',
            302 => 'Found',
            303 => 'See Other',
            304 => 'Not Modified',
            305 => 'Use Proxy',
            306 => '(Unused)',
            307 => 'Temporary Redirect',
            400 => 'Bad Request',
            401 => 'Unauthorized',
            402 => 'Payment Required',
            403 => 'Forbidden',
            404 => 'Not Found',
            405 => 'Method Not Allowed',
            406 => 'Not Acceptable',
            407 => 'Proxy Authentication Required',
            408 => 'Request Timeout',
            409 => 'Conflict',
            410 => 'Gone',
            411 => 'Length Required',
            412 => 'Precondition Failed',
            413 => 'Request Entity Too Large',
            414 => 'Request-URI Too Long',
            415 => 'Unsupported Media Type',
            416 => 'Requested Range Not Satisfiable',
            417 => 'Expectation Failed',
            429 => 'Too Many Requests',
            500 => 'Internal Server Error',
            501 => 'Not Implemented',
            502 => 'Bad Gateway',
            503 => 'Service Unavailable',
            504 => 'Gateway Timeout',
            505 => 'HTTP Version Not Supported');
        return ($status[$code]) ? $status[$code] : $status[500];
    }

    /**
     * @param int $status
     */
    public function setStatus($status)
    {
        $this->status = $status;
    }


}