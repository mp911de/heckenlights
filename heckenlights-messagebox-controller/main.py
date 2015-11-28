#!/usr/bin/env python

import os, requests, json, hashlib, logging, PIL.Image, time, glob, subprocess, sys, ConfigParser, shlex
import RPi.GPIO as GPIO


__author__ = 'mark'


messagebox_url = ""
tempdir = ""
Config = ConfigParser.ConfigParser()
Config.read("config.ini")

messagebox_url = Config.get("settings", "messagebox_url")
tempdir = Config.get("settings", "tempdir")
scrollspeed_ms_per_pixel = Config.getfloat("settings", "scrollspeed_ms_per_pixel")
led_matrix_executable = Config.get("settings", "led_matrix_executable")


def get_display_content():
    for fl in glob.glob("%s/*.ppm" % tempdir):
        os.remove(fl)

    logging.info("Requesting %s" % messagebox_url)

    try:
        r = requests.get(messagebox_url)
        logging.debug("Status %d" % r.status_code)

        if r.status_code >= 400:
            logging.info("Cannot load image, status %d" % r.status_code)
            return None

        if r.status_code >= 300 and r.status_code <= 399:
            logging.info("Redirect/Requesting %s" % r.headers['Location'])
            r = requests.get(r.headers['Location'])

        if r.status_code >= 200 and r.status_code <= 299:
            logging.info("Location %s" % r.url)

        if 'X-Content' in r.headers:
            logging.info("Content %s" % r.headers['X-Content'])

        m = hashlib.md5()
        m.update(r.content)
        hash = m.hexdigest();

        filename = "%s/%s.ppm" % (tempdir, hash);
        logging.debug("Write content to %s" % filename)

        f = open(filename, 'w')
        f.write(r.content)
        f.close()
        return filename
    except BaseException as e:
        logging.warning("Cannot retrieve data {0}: {1}".format(e.__class__.__name__, e.message))
    except:
        logging.warning("Cannot retrieve data {0}".format(sys.exc_info()[0]))


    return None


def get_width(filename):
    try:
        image = PIL.Image.open(filename)
        return image.size[0]
    except BaseException as e:
        logging.warning("Cannot read image data {0}: {1}".format(filename, e.message))

    return None

def main():
    if not (os.path.exists(tempdir)):
        os.mkdir(tempdir)

    filename = get_display_content()

    if filename is None:
        logging.error("Cannot retrieve initial file")
        return 1

    while True:

        if filename is not None:
            width = get_width(filename)
            Config.read("config.ini")
            scrollspeed_ms_per_pixel = Config.getfloat("settings", "scrollspeed_ms_per_pixel")
            cmdline = Config.get("settings", "led_matrix_args")
            if width is not None:
                sleep_time = min(width, max(width - 32, 0)) * scrollspeed_ms_per_pixel
                command_line=  "%s %s -m %d0.0 %s" % (led_matrix_executable, cmdline, scrollspeed_ms_per_pixel * 100, filename)
                logging.debug("Command line: " + command_line)
                args = shlex.split(command_line)
                process = subprocess.Popen(args)
                logging.info("Width: %d px, Sleep-Time %d sec (org %d), File: %s" % (width, sleep_time, (width * scrollspeed_ms_per_pixel), filename))

                time.sleep(sleep_time)
            else:
                time.sleep(10)

            filename = get_display_content()

            process.wait()
        else:
            time.sleep(10)
            filename = get_display_content()

def cleanup():
    GPIO.setmode(GPIO.BCM)
    GPIO.setwarnings(False)
    GPIO.setup(1, GPIO.OUT)
    GPIO.setup(2, GPIO.OUT)
    GPIO.setup(3, GPIO.OUT)
    GPIO.setup(4, GPIO.OUT)
    GPIO.setup(5, GPIO.OUT)
    GPIO.setup(6, GPIO.OUT)
    GPIO.setup(7, GPIO.OUT)
    GPIO.setup(8, GPIO.OUT)
    GPIO.cleanup()

if __name__ == '__main__':
    logging.basicConfig(format='%(asctime)s %(levelname)s %(message)s', level=logging.INFO)
    try:
        main()
    except KeyboardInterrupt:
        cleanup()
        raise
