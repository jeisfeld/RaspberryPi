#!/usr/bin/env python3
#
# Test 64x64 LED display

from colormatrix.LedDisplay import getStrip, colorFill
from colormatrix.Color import Color
from colormatrix.AbstractMatrix import matrixFill, moveToMatrix
from colormatrix.ColorMatrix import ColorMatrix
from math import exp
from colormatrix.ColorTemperature import convertColorTemperature
from random import randrange, random

# LED strip configuration:
LED_PIN = 18  # GPIO pin connected to the pixels (18 uses PWM!).
# LED_PIN        = 10      # GPIO pin connected to the pixels (10 uses SPI /dev/spidev0.0).
LED_BRIGHTNESS = 100  # Set to 0 for darkest and 255 for brightest
LED_CHANNEL = 0  # set to '1' for GPIOs 13, 19, 41, 45 or 53


def getRandomColor():
    return convertColorTemperature(int(exp(7 + random())), random() * 0.8 + 0.2)


def getRandomMatrix():
    matrix = ColorMatrix(getRandomColor())
    for _ in range(randrange(1, 5)):
        matrix.setColor(7 * random(), 7 * random(), getRandomColor())
    return matrix


# Main program logic follows:
if __name__ == '__main__':
    # Create NeoPixel object with appropriate configuration.
    strip = getStrip(LED_PIN, LED_CHANNEL, LED_BRIGHTNESS)
    # Intialize the library (must be called once before other functions).
    strip.begin()
    
    matrix = getRandomMatrix()
    matrixFill(strip, matrix)
    
    try:
        while True:
            newMatrix = getRandomMatrix()
            moveToMatrix(strip, matrix, newMatrix, 1 + 5 * random())
            matrix = newMatrix

    except KeyboardInterrupt:
        colorFill(strip, Color(0, 0, 0))
