Heckenlights
=============

Heckenlights is the web-controllable christmas lights setup at http://heckenlights.org

This repository carries a set of code modules to control christmas lights using MIDI commands. The particular modules are:

1. Heckenlights Backend (Java/Spring Boot): Backend for controlling the program, accepting MIDI file submissions and providing the current playlist
2. Heckenlights Frontend (HTML/PHP): HTML frontend to display the playlist and the the MIDI submission
3. Heckenlights Messagebox (Java): Image Generator to generate images from the Twitter stream, current title and some advertising
4. Heckenlights Messagebox Controller (Python): Controller of the messagebox which retrieves the Messagebox images and sends these to a RGB matrix display
5. rpi-rgb-led-matrix (C++): Matrix display application to scroll PPM (PNM) images onto a [Medium 16x32 RGB LED matrix panel](http://www.adafruit.com/product/420) (fork of https://github.com/hzeller/rpi-rgb-led-matrix)

And one more thing:
Heckenlights uses [https://github.com/mp911de/midi-relay] to switch lights on and off. Midi-relay is an own repository.

How it works
------------
<img src="../master/images/Heckenlights.png?raw=true" width="200" align="left" style="margin: 5px"/> Heckenlights is a MIDI controllable christmas lights setup at Heckenpfad 14, Weinheim, Germany. In the beginning, I started just with some lights. The next year, I decided to add some sort of control. All available control systems are either very expensive or available to US only. Therefore I decided to add an ethernet controlled relay ([ETHRLY-16](http://www.robot-electronics.co.uk/htm/eth_rly16tech.htm)) and a controller application [https://github.com/mp911de/midi-relay]. [https://github.com/mp911de/midi-relay] can be fed with MIDI files and uses the software sequencer to play MIDI files with redirecting/mapping all note output towards the ethernet relay. midi-relay runs on a Linux box (headless) controlling two ethernet relays having several chains of christmas light connected.

Next step was adding a Web-UI and a Matrix Display for live streaming of Heckenlights itself, uploading MIDI files and watching the playlist. heckenlights-backend manages all backend-related details like when to play, store and manage MIDI submissions within [TokuMX](http://www.tokutek.com/tokumx-for-mongodb/). heckenlights-backend submits every item from the playlist to midi-relay and is the DJ in this case.

heckenlights-frontend is 'just' a HTML frontend using a small potion of PHP to communicate with heckenlights-backend over HTTP. 

<img src="../master/images/Heckenlights-Messagebox.png?raw=true" width="200" style="margin: 5px"align="left"/> Last year, I noticed it would be also nice to know what is played. Watching Heckenlights play is amazing, but if you don't know the song it's sort of awkward. Here comes the heckenlights-messagebox: The Messagebox is a RaspberryPi with a [Medium 16x32 RGB LED matrix panel](http://www.adafruit.com/product/420) connected. It runs two things: heckenlights-messagebox-controller and rpi-rgb-led-matrix. The first one pulls data from heckenlights-messagebox (PPM images containing Tweets of #Heckenlights, the current title and some details of http://heckenlights.org). rpi-rgb-led-matrix takes care of scrolling the PPM images onto the LED Matrix Display using GPIO.




Resources
---------
* [Controlling Christmas Lights using Midi](https://www.paluch.biz/blog/65-controlling-christmas-lights-using-midi.html)
* [Controlling a relay via Midi](https://www.paluch.biz/blog/64-controlling-a-relay-via-midi.html)
* [midi-relay](https://github.com/mp911de/midi-relay)


License
---------
* heckenlights-backend, heckenlights-frontend, heckenlights-messagebox and heckenlights-messagebox-controller: [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
* rpi-rgb-led-matrix [GPLv2](http://www.gnu.org/licenses/gpl-2.0.html), fork of [https://github.com/hzeller/rpi-rgb-led-matrix]
