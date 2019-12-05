#!/usr/bin/env python3
#
# Test 64x64 LED display

from time import sleep
from neopixel import Adafruit_NeoPixel
from Color import Color
from MotiveMatrix import MotiveMatrix
from math import log, exp
from colorTemperature import convertColorTemperature
from random import randrange, random

# LED strip configuration:
WIDTH = 8
HEIGHT = 8
LED_COUNT = WIDTH * HEIGHT  # Number of LED pixels.
LED_PIN = 18  # GPIO pin connected to the pixels (18 uses PWM!).
# LED_PIN        = 10      # GPIO pin connected to the pixels (10 uses SPI /dev/spidev0.0).
LED_BRIGHTNESS = 150  # Set to 0 for darkest and 255 for brightest
LED_CHANNEL = 0  # set to '1' for GPIOs 13, 19, 41, 45 or 53

DARKRED = Color(20, 0, 0)
DARKESTYELLOW = Color(10, 10, 0)
DARKYELLOW = Color(50, 50, 0)
GREEN = Color(0, 96, 0)
BROWN = Color(79,51,0)
BLACK = Color(0, 0, 0)

CANDLES = [
    [BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, DARKESTYELLOW, DARKESTYELLOW],
    [DARKESTYELLOW, DARKESTYELLOW, BLACK, BLACK, BLACK, BLACK, DARKYELLOW, DARKYELLOW],
    [DARKYELLOW, DARKYELLOW, BLACK, DARKESTYELLOW, DARKESTYELLOW, BLACK, DARKRED, DARKRED],
    [DARKRED, DARKRED, BLACK, DARKYELLOW, DARKYELLOW, BLACK, DARKRED, DARKRED],
    [DARKRED, DARKRED, BLACK, DARKRED, DARKRED, BLACK, DARKRED, DARKRED],
    [DARKRED, DARKRED, BLACK, DARKRED, DARKRED, BLACK, DARKRED, DARKRED],
    [DARKRED, DARKRED, BLACK, DARKRED, DARKRED, BLACK, BLACK, BLACK],
    [BLACK, BLACK, BLACK, DARKRED, DARKRED, BLACK, BLACK, BLACK]
    ]

TREE = MotiveMatrix([
    [BLACK,BLACK,BLACK,GREEN,BLACK,BLACK,BLACK,BLACK],
    [BLACK,BLACK,BLACK,GREEN,BLACK,BLACK,BLACK,BLACK],
    [BLACK,BLACK,GREEN,GREEN,GREEN,BLACK,BLACK,BLACK],
    [BLACK,BLACK,GREEN,GREEN,GREEN,BLACK,BLACK,BLACK],
    [BLACK,GREEN,GREEN,GREEN,GREEN,GREEN,BLACK,BLACK],
    [BLACK,GREEN,GREEN,GREEN,GREEN,GREEN,BLACK,BLACK],
    [BLACK,BLACK,BLACK,BROWN,BLACK,BLACK,BLACK,BLACK],
    [BLACK,BLACK,BLACK,BROWN,BLACK,BLACK,BLACK,BLACK]
    ])


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


def getRandomColor(factor):
    return convertColorTemperature(int(exp(7 + random())), random() * factor)

def setCandleColorsOneColumn(matrix,x,y):
    color1 = getRandomColor(0.2)
    color2 = color1 + getRandomColor(0.4)
    matrix.setColor(x,y,DARKESTYELLOW + color1)
    matrix.setColor(x,y+1,DARKYELLOW + color2)

def setCandleColorsOneCandle(matrix,i):
    if i==1:
        (x,y)=(0,1)
    elif i==2:
        (x,y)=(3,2)
    elif i==3:
        (x,y)=(6,0)
    setCandleColorsOneColumn(matrix,x,y)
    setCandleColorsOneColumn(matrix,x+1,y)
        


def getCandleMatrix():
    matrix = MotiveMatrix(CANDLES)
    setCandleColorsOneCandle(matrix,1)
    setCandleColorsOneCandle(matrix,2)
    setCandleColorsOneCandle(matrix,3)
    return matrix
    



# Main program logic follows:
if __name__ == '__main__':
    # Create NeoPixel object with appropriate configuration.
    strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_CHANNEL, LED_BRIGHTNESS)
    # Intialize the library (must be called once before other functions).
    strip.begin()
    
    matrix = getCandleMatrix()
    matrix2 = TREE
    matrix2 = getCandleMatrix()
    matrixFill(strip, matrix)
    
    try:
        while True:
#            moveToMatrix(strip, matrix, matrix2, 1)
#            moveToMatrix(strip, matrix2, matrix, 1)
            newMatrix = getCandleMatrix()
            moveToMatrix(strip, matrix, newMatrix, 0.1)
            matrix = newMatrix

    except KeyboardInterrupt:
        colorFill(strip, Color(0, 0, 0))
