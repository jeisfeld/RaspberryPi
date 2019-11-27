#!/usr/bin/env python3
#
# Test 64x64 LED display

import time
from neopixel import Adafruit_NeoPixel, Color
from math import log
import argparse

# LED strip configuration:
LED_COUNT      = 64      # Number of LED pixels.
LED_PIN        = 18      # GPIO pin connected to the pixels (18 uses PWM!).
#LED_PIN        = 10      # GPIO pin connected to the pixels (10 uses SPI /dev/spidev0.0).
LED_FREQ_HZ    = 800000  # LED signal frequency in hertz (usually 800khz)
LED_DMA        = 10      # DMA channel to use for generating signal (try 10)
LED_BRIGHTNESS = 255     # Set to 0 for darkest and 255 for brightest
LED_INVERT     = False   # True to invert the signal (when using NPN transistor level shift)
LED_CHANNEL    = 0       # set to '1' for GPIOs 13, 19, 41, 45 or 53



# Define functions which animate LEDs in various ways.
def colorWipe(strip, color, wait_ms=10):
    """Wipe color across display a pixel at a time."""
    for i in range(strip.numPixels()):
        strip.setPixelColor(i, color)
        strip.show()
        time.sleep(wait_ms/1000.0)

def colorFill(strip, color):
    """Fill display with color."""
    for i in range(strip.numPixels()):
        strip.setPixelColor(i, color)
    strip.show()

def convertColorTemperature(temperature, brightness):
    if temperature < 6600:
        red = 1;
        green = 0.39 * log(temperature / 100) - 0.634
        blue = 0.543 * log(temperature / 100 - 10) - 1.186
    else:
        red = 1.269 * (temperature / 100 - 60) ** -0.1332
        green = 1.145 * (temperature / 100 - 60) ** 0.0755
        blue = 1
    red = ensureInRange255(red*brightness)
    green = ensureInRange255(green*brightness)
    blue = ensureInRange255(blue*brightness)
    return Color(green, red, blue); # somehow, the colors are mixed up when displaying

def ensureInRange255(value):
        if value < 0:
            return 0
        elif value > 1:
            return 255
        else:
            return int(value * 255)

# Main program logic follows:
if __name__ == '__main__':
    # Process arguments
    parser = argparse.ArgumentParser()
    parser.add_argument('-c', '--clear', action='store_true', help='clear the display on exit')
    args = parser.parse_args()

    # Create NeoPixel object with appropriate configuration.
    strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMA, LED_INVERT, LED_BRIGHTNESS, LED_CHANNEL)
    # Intialize the library (must be called once before other functions).
    strip.begin()

    try:

        while True:
            colorWipe(strip, convertColorTemperature(2000, .3))
            colorWipe(strip, convertColorTemperature(3000, .3))
            colorWipe(strip, convertColorTemperature(4000, .3))
            colorWipe(strip, convertColorTemperature(6000, .3))
            colorWipe(strip, convertColorTemperature(9000, .3))


    except KeyboardInterrupt:
        colorFill(strip, Color(0,0,0))
