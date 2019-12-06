#!/usr/bin/env python3
#
# Test 64x64 LED display

from colormatrix.LedDisplay import getStrip
from colormatrix.ColorMatrix import ColorMatrix
from colormatrix.MatrixAnimator import MatrixAnimator
from colormatrix.ColorTemperature import getRandomColor
from random import randrange, random
from sys import argv

global LED_BRIGHTNESS

# LED strip configuration:
LED_PIN = 18  # GPIO pin connected to the pixels (18 uses PWM!).
# LED_PIN        = 10      # GPIO pin connected to the pixels (10 uses SPI /dev/spidev0.0).
LED_BRIGHTNESS = 255  # Set to 0 for darkest and 255 for brightest
LED_CHANNEL = 0  # set to '1' for GPIOs 13, 19, 41, 45 or 53

MIN_TEMP = 1000
MAX_TEMP = 4000
MIN_BR = 0.2
MAX_BR = 1



def getRandomMatrix():
    matrix = ColorMatrix(getRandomColor(MIN_TEMP, MAX_TEMP, MIN_BR, MAX_BR))
    for _ in range(randrange(1, 5)):
        matrix.setColor(7 * random(), 7 * random(), getRandomColor(MIN_TEMP, MAX_TEMP, MIN_BR, MAX_BR))
    return matrix


# Main program logic follows:
if __name__ == '__main__':
    if len(argv) >= 2:
        LED_BRIGHTNESS = int(argv[1])
        
    # Create NeoPixel object with appropriate configuration.
    strip = getStrip(LED_PIN, LED_CHANNEL, LED_BRIGHTNESS)
    # Intialize the library (must be called once before other functions).
    strip.begin()
    
    
    matrixAnimator = MatrixAnimator(strip)
    matrixAnimator.start()
    
    try:
        while True:
            matrixAnimator.moveToMatrix(getRandomMatrix(), 1 + 2 * random())
    except KeyboardInterrupt:
        matrixAnimator.stop()
