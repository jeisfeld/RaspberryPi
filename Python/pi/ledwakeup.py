#!/usr/bin/env python3
#
# Test 64x64 LED display

from colormatrix.LedDisplay import getStrip, moveToColor, colorFill
from colormatrix.Color import Color, BLACK
from time import sleep

global LED_BRIGHTNESS

# LED strip configuration:
LED_PIN = 18  # GPIO pin connected to the pixels (18 uses PWM!).
# LED_PIN        = 10      # GPIO pin connected to the pixels (10 uses SPI /dev/spidev0.0).
LED_BRIGHTNESS = 255  # Set to 0 for darkest and 255 for brightest
LED_CHANNEL = 0  # set to '1' for GPIOs 13, 19, 41, 45 or 53




# Main program logic follows:
if __name__ == '__main__':
        
    # Create NeoPixel object with appropriate configuration.
    strip = getStrip(LED_PIN, LED_CHANNEL, LED_BRIGHTNESS)
    # Intialize the library (must be called once before other functions).
    strip.begin()
    
    try:
        moveToColor(strip, 1000, 0, 5000, 1, 60)
    
        while True:
            sleep(1)
            
    except KeyboardInterrupt:
        colorFill(strip, BLACK)
