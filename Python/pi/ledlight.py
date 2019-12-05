#!/usr/bin/env python3
#
# Test 64x64 LED display

from time import sleep
from neopixel import Adafruit_NeoPixel
from Color import Color
from ColorMatrix import ColorMatrix
from math import log, exp
from colorTemperature import convertColorTemperature
from random import randrange, random

# LED strip configuration:
WIDTH = 8
HEIGHT = 8
LED_COUNT = WIDTH * HEIGHT  # Number of LED pixels.
LED_PIN = 18  # GPIO pin connected to the pixels (18 uses PWM!).
# LED_PIN        = 10      # GPIO pin connected to the pixels (10 uses SPI /dev/spidev0.0).
LED_BRIGHTNESS = 100  # Set to 0 for darkest and 255 for brightest
LED_CHANNEL = 0  # set to '1' for GPIOs 13, 19, 41, 45 or 53


# Define functions which animate LEDs in various ways.
def colorWipe(strip, color, wait_ms=10):
    """Wipe color across display a pixel at a time."""
    for i in range(strip.numPixels()):
        strip.setPixelColor(i, color.value())
        strip.show()
        sleep(wait_ms / 1000.0)


def colorFill(strip, color):
    """Fill display with color."""
    for i in range(strip.numPixels()):
        strip.setPixelColor(i, color.value())
    strip.show()


def matrixFill(strip, matrix):
    for x in range(WIDTH):
        for y in range(HEIGHT):
            strip.setPixelColor(x + y * WIDTH, matrix.getColor(x, y).value())
    strip.show()


def moveToColor(strip, temperature1, brightness1, temperature2, brightness2, duration):
    steps = max(min(duration * 400, 100), 1)
    sleepDuration = duration / steps
    logTemperatureStart = log(temperature1)
    logTemperatureDiff = (log(temperature2) - log(temperature1)) / steps;
    brightnessDiff = (brightness2 - brightness1) / steps
    
    for i in range(1, steps + 1):
        sleep(sleepDuration)
        colorFill(strip, convertColorTemperature(exp(logTemperatureStart + i * logTemperatureDiff), brightness1 + i * brightnessDiff))


def moveToMatrix(strip, matrix1, matrix2, duration):
    steps = int(max(min(duration * 400, 100), 1))
    sleepDuration = duration / steps
    
    for i in range(1, steps + 1):
        sleep(sleepDuration)
        for x in range(WIDTH):
            for y in range(HEIGHT):
                strip.setPixelColor(x + y * WIDTH, matrix1.getCombinedColor(matrix2, i / steps, x, y).value())
        strip.show()


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
    strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_CHANNEL, LED_BRIGHTNESS)
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
