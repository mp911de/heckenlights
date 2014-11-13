// -*- mode: c++; c-basic-offset: 2; indent-tabs-mode: nil; -*-
//
// This code is public domain
// (but note, that the led-matrix library this depends on is GPL v2)

#include "led-matrix.h"
#include "threaded-canvas-manipulator.h"

#include <assert.h>
#include <getopt.h>
#include <limits.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <algorithm>

using std::min;
using std::max;

using namespace rgb_matrix;

// This is an example how to use the Canvas abstraction to map coordinates.
//
// This is a Canvas that delegates to some other Canvas (typically, the RGB
// matrix).
//
// Here, we want to address four 32x32 panels as one big 64x64 panel. Physically,
// we chain them together and do a 180 degree 'curve', somewhat like this:
// [>] [>]
//         v
// [<] [<]
class LargeSquare64x64Canvas : public Canvas {
public:
    // This class takes over ownership of the delegatee.
    LargeSquare64x64Canvas(Canvas *delegatee) : delegatee_(delegatee) {
        // Our assumptions of the underlying geometry:
        assert(delegatee->height() == 32);
        assert(delegatee->width() == 128);
    }
    virtual ~LargeSquare64x64Canvas() { delete delegatee_; }
    
    virtual void Clear() { delegatee_->Clear(); }
    virtual void Fill(uint8_t red, uint8_t green, uint8_t blue) {
        delegatee_->Fill(red, green, blue);
    }
    virtual int width() const { return 64; }
    virtual int height() const { return 64; }
    virtual void SetPixel(int x, int y,
                          uint8_t red, uint8_t green, uint8_t blue) {
        if (x < 0 || x >= width() || y < 0 || y >= height()) return;
        // We have up to column 64 one direction, then folding around. Lets map
        if (y > 31) {
            x = 127 - x;
            y = 63 - y;
        }
        delegatee_->SetPixel(x, y, red, green, blue);
    }
    
private:
    Canvas *delegatee_;
};



class ImageScroller : public ThreadedCanvasManipulator {
public:
    // Scroll image with "scroll_jumps" pixels every "scroll_ms" milliseconds.
    // If "scroll_ms" is negative, don't do any scrolling.
    ImageScroller(Canvas *m, int scroll_jumps, int scroll_ms = 30)
    : ThreadedCanvasManipulator(m), scroll_jumps_(scroll_jumps),
    scroll_ms_(scroll_ms),
    horizontal_position_(0) {
    }
    
    virtual ~ImageScroller() {
        Stop();
        WaitStopped();   // only now it is safe to delete our instance variables.
    }
    
    // _very_ simplified. Can only read binary P6 PPM. Expects newlines in headers
    // Not really robust. Use at your own risk :)
    // This allows reload of an image while things are running, e.g. you can
    // life-update the content.
    bool LoadPPM(const char *filename) {
        FILE *f = fopen(filename, "r");
        if (f == NULL) return false;
        char header_buf[256];
        const char *line = ReadLine(f, header_buf, sizeof(header_buf));
#define EXIT_WITH_MSG(m) { fprintf(stderr, "%s: %s |%s", filename, m, line); \
fclose(f); return false; }
        if (sscanf(line, "P6 ") == EOF)
            EXIT_WITH_MSG("Can only handle P6 as PPM type.");
        line = ReadLine(f, header_buf, sizeof(header_buf));
        int new_width, new_height;
        if (!line || sscanf(line, "%d %d ", &new_width, &new_height) != 2)
            EXIT_WITH_MSG("Width/height expected");
        int value;
        line = ReadLine(f, header_buf, sizeof(header_buf));
        if (!line || sscanf(line, "%d ", &value) != 1 || value != 255)
            EXIT_WITH_MSG("Only 255 for maxval allowed.");
        const size_t pixel_count = new_width * new_height;
        Pixel *new_image = new Pixel [ pixel_count ];
        assert(sizeof(Pixel) == 3);   // we make that assumption.
        if (fread(new_image, sizeof(Pixel), pixel_count, f) != pixel_count) {
            line = "";
            EXIT_WITH_MSG("Not enough pixels read.");
        }
#undef EXIT_WITH_MSG
        fclose(f);
        fprintf(stderr, "Read image '%s' with %dx%d\n", filename,
                new_width, new_height);
        horizontal_position_ = 0;
        MutexLock l(&mutex_new_image_);
        new_image_.Delete();  // in case we reload faster than is picked up
        new_image_.image = new_image;
        new_image_.width = new_width;
        new_image_.height = new_height;
        return true;
    }
    
    void Run() {
        const int screen_height = canvas()->height();
        const int screen_width = canvas()->width();

        while (running()) {
            {
                MutexLock l(&mutex_new_image_);
                if (new_image_.IsValid()) {
                    current_image_.Delete();
                    current_image_ = new_image_;
                    new_image_.Reset();
                }
            }
			
           
			
            if (!current_image_.IsValid()) {
                usleep(200 * 1000);
                continue;
            }
            for (int x = 0; x < screen_width; ++x) {
                for (int y = 0; y < screen_height; ++y) {
                    const Pixel &p = current_image_.getPixel(
                                                             (horizontal_position_ + x), y);
                    canvas()->SetPixel(x, y, p.red, p.green, p.blue);
                }
            }
			
            if (horizontal_position_ >= current_image_.width) {
                Stop();
				return;
            }
            
            horizontal_position_ += scroll_jumps_;

            if (horizontal_position_ < 0) {
                horizontal_position_ = current_image_.width;
            }
			

            if (scroll_ms_ <= 0) {
                // No scrolling. We don't need the image anymore.
                current_image_.Delete();
            } else {
                usleep(scroll_ms_ * 1000);
            }
        }
    }
    
private:
    struct Pixel {
        Pixel() : red(0), green(0), blue(0){}
        uint8_t red;
        uint8_t green;
        uint8_t blue;
    };
    
    struct Image {
        Image() : width(-1), height(-1), image(NULL) {}
        ~Image() { Delete(); }
        void Delete() { delete [] image; Reset(); }
        void Reset() { image = NULL; width = -1; height = -1; }
        inline bool IsValid() { return image && height > 0 && width > 0; }
        const Pixel &getPixel(int x, int y) {
            static Pixel black;
            if (x < 0 || x >= width || y < 0 || y >= height) return black;
            return image[x + width * y];
        }
        
        int width;
        int height;
        Pixel *image;
    };
    
    // Read line, skip comments.
    char *ReadLine(FILE *f, char *buffer, size_t len) {
        char *result;
        do {
            result = fgets(buffer, len, f);
        } while (result != NULL && result[0] == '#');
        return result;
    }
    
    const int scroll_jumps_;
    const int scroll_ms_;
    
    // Current image is only manipulated in our thread.
    Image current_image_;
    
    // New image can be loaded from another thread, then taken over in main thread.
    Mutex mutex_new_image_;
    Image new_image_;
    
    int32_t horizontal_position_;
};



static int usage(const char *progname) {
    fprintf(stderr, "usage: %s <options> [optional parameter]\n",
            progname);
    fprintf(stderr, "Options:\n"
            "\t-r <rows>     : Display rows. 16 for 16x32, 32 for 32x32. "
            "Default: 32\n"
            "\t-c <chained>  : Daisy-chained boards. Default: 1.\n"
            "\t-L            : 'Large' display, composed out of 4 times 32x32\n"
            "\t-p <pwm-bits> : Bits used for PWM. Something between 1..11\n"
            "\t-l            : Don't do luminance correction (CIE1931)\n"
            "\t-d            : run as daemon. Use this when starting in\n"
            "\t                /etc/init.d, but also when running without\n"
            "\t                terminal (e.g. cron).\n"
            "\t-t <seconds>  : Run for these number of seconds, then exit.\n"
            "\t       (if neither -d nor -t are supplied, waits for finishing the scroller)\n");
    fprintf(stderr, "Example:\n\t%s runtext.ppm\n"
            "Scrolls the runtext once\n", progname);
    return 1;
}

int main(int argc, char *argv[]) {
    bool as_daemon = false;
    int runtime_seconds = -1;
    int rows = 16;
    int chain = 1;
    int scroll_ms = 80;
    int pwm_bits = -1;
    bool large_display = false;
    bool do_luminance_correct = true;
    
    const char *filename = NULL;
    
    int opt;
    while ((opt = getopt(argc, argv, "dlD:t:r:p:c:m:L")) != -1) {
        switch (opt) {
                
            case 'd':
                as_daemon = true;
                break;
                
            case 't':
                runtime_seconds = atoi(optarg);
                break;
                
            case 'r':
                rows = atoi(optarg);
                break;
                
            case 'c':
                chain = atoi(optarg);
                break;
                
            case 'm':
                scroll_ms = atoi(optarg);
                break;
                
            case 'p':
                pwm_bits = atoi(optarg);
                break;
                
            case 'l':
                do_luminance_correct = !do_luminance_correct;
                break;
                
            case 'L':
                // The 'large' display assumes a chain of four displays with 32x32
                chain = 4;
                rows = 32;
                large_display = true;
                break;
                
            default: /* '?' */
                return usage(argv[0]);
        }
    }
    
    if (optind < argc) {
        filename = argv[optind];
    }
    
    if (getuid() != 0) {
        fprintf(stderr, "Must run as root to be able to access /dev/mem\n"
                "Prepend 'sudo' to the command:\n\tsudo %s ...\n", argv[0]);
        return 1;
    }
    
    if (rows != 16 && rows != 32) {
        fprintf(stderr, "Rows can either be 16 or 32\n");
        return 1;
    }
    
    if (chain < 1) {
        fprintf(stderr, "Chain outside usable range\n");
        return 1;
    }
    if (chain > 8) {
        fprintf(stderr, "That is a long chain. Expect some flicker.\n");
    }
    
    // Initialize GPIO pins. This might fail when we don't have permissions.
    GPIO io;
    if (!io.Init())
        return 1;
    
    // Start daemon before we start any threads.
    if (as_daemon) {
        if (fork() != 0)
            return 0;
        close(STDIN_FILENO);
        close(STDOUT_FILENO);
        close(STDERR_FILENO);
    }
    
    // The matrix, our 'frame buffer' and display updater.
    RGBMatrix *matrix = new RGBMatrix(&io, rows, chain);
    matrix->set_luminance_correct(do_luminance_correct);
    if (pwm_bits >= 0 && !matrix->SetPWMBits(pwm_bits)) {
        fprintf(stderr, "Invalid range of pwm-bits\n");
        return 1;
    }
    
    Canvas *canvas = matrix;
    
    if (large_display) {
        // Mapping the coordinates of a 32x128 display mapped to a square of 64x64
        canvas = new LargeSquare64x64Canvas(canvas);
    }
    
    // The ThreadedCanvasManipulator objects are filling
    // the matrix continuously.
    ThreadedCanvasManipulator *image_gen = NULL;
    fprintf(stderr, "args %s %s\n", argv[0],argv[1] );
    
    if (filename) {
        ImageScroller *scroller = new ImageScroller(canvas,
                                                    1,
                                                    scroll_ms);
        if (!scroller->LoadPPM(filename)){
            fprintf(stderr, "Cannot load PPM image from file %s\n", filename);
            delete canvas;
            return 1;
        }

        image_gen = scroller;
    } else {
        fprintf(stderr, "ImageScroller requires PPM image as parameter\n");
        delete canvas;
        return 1;
    }
    
    if (image_gen == NULL)
        return usage(argv[0]);
    
    // Image generating is created. Now start the thread.
    image_gen->Start();
    
    // Now, the image genreation runs in the background. We can do arbitrary
    // things here in parallel. In this demo, we're essentially just
    // waiting for one of the conditions to exit.
    if (as_daemon) {
        sleep(runtime_seconds > 0 ? runtime_seconds : INT_MAX);
    } else if (runtime_seconds > 0) {
        sleep(runtime_seconds);
    } else {
        printf("Waiting for thread stop\n");
        while (image_gen->running()) {
            sleep(1);
        }
        image_gen->Stop();
    }
    
    // Stop image generating thread.
    delete image_gen;
    delete canvas;
    
    return 0;
}
