import os, requests, json, hashlib, logging, PIL.Image, time, glob, subprocess, sys, ConfigParser

__author__ = 'mark'


messagebox_url = ""
tempdir = ""
Config = ConfigParser.ConfigParser()
Config.read("config.ini")

messagebox_url = Config.get("settings", "messagebox_url")
tempdir = Config.get("settings", "tempdir")
scrollspeed_ms_per_pixel = Config.get("settings", "scrollspeed_ms_per_pixel")


def get_display_content():
    logging.info("Requesting %s" % messagebox_url)

    try:
        r = requests.get(messagebox_url)
        logging.debug("Status %d" % r.status_code)

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
    image = PIL.Image.open(filename)
    return image.size[0]


def main():
    if not (os.path.exists(tempdir)):
        os.mkdir(tempdir)

    for fl in glob.glob("%s/*.ppm" % tempdir):
        os.remove(fl)

    filename = get_display_content()

    if filename is None:
        logging.error("Cannot retrieve initial file")
        return 1

    while True:

        if filename is not None:
            width = get_width(filename)
            sleep_time = min(width, max(width - 10, 0)) * scrollspeed_ms_per_pixel

            process = subprocess.Popen(("echo Hello World", filename), shell=True)
            logging.info("Width: %d px, Sleep-Time %d sec" % (width, sleep_time))

            time.sleep(sleep_time)
            filename = get_display_content()

            process.wait()
        else:
            time.sleep(10)
            filename = get_display_content()



if __name__ == '__main__':
    logging.basicConfig(format='%(asctime)s %(levelname)s %(message)s', level=logging.INFO)
    main()


        



