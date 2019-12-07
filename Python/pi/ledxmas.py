#!/usr/bin/env python3
#
# Test 64x64 LED display

from colormatrix.LedDisplay import getStrip
from colormatrix.Color import Color, BLACK
from colormatrix.ImageMatrix import ImageMatrix
from colormatrix.MatrixAnimator import MatrixAnimator
from colormatrix.CandleMatrix import CandleMatrix
from time import sleep
from random import randrange, random
from sys import argv
from colormatrix.StarMatrix import StarMatrix

global LED_BRIGHTNESS

# LED strip configuration:
LED_PIN = 18  # GPIO pin connected to the pixels (18 uses PWM!).
# LED_PIN        = 10      # GPIO pin connected to the pixels (10 uses SPI /dev/spidev0.0).
LED_BRIGHTNESS = 255  # Set to 0 for darkest and 255 for brightest
LED_CHANNEL = 0  # set to '1' for GPIOs 13, 19, 41, 45 or 53

MIN_TEMP = 1000
MAX_TEMP = 3000

GREEN = Color(0, 20, 0)
BROWN = Color(8, 5, 0)

TREE = ImageMatrix([
    [BLACK, BLACK, BLACK, GREEN, GREEN, BLACK, BLACK, BLACK],
    [BLACK, BLACK, BLACK, GREEN, GREEN, BLACK, BLACK, BLACK],
    [BLACK, BLACK, GREEN, GREEN, GREEN, GREEN, BLACK, BLACK],
    [BLACK, BLACK, GREEN, GREEN, GREEN, GREEN, BLACK, BLACK],
    [BLACK, GREEN, GREEN, GREEN, GREEN, GREEN, GREEN, BLACK],
    [BLACK, GREEN, GREEN, GREEN, GREEN, GREEN, GREEN, BLACK],
    [BLACK, BLACK, BLACK, BROWN, BROWN, BLACK, BLACK, BLACK],
    [BLACK, BLACK, BLACK, BROWN, BROWN, BLACK, BLACK, BLACK]
    ])


def getNewMatrix():
    rand = random()
    
    if rand < 0.2:
        return (TREE, 2 + 10 * random())
    elif rand < 0.6:
        return (CandleMatrix(randrange(4)), 10 + 180 * random())
    else:
        return (StarMatrix(randrange(4,9)), 10 + 60 * random())



# Main program logic follows:
if __name__ == '__main__':
    if len(argv) >= 2:
        LED_BRIGHTNESS = int(argv[1])
    

    # Create NeoPixel object with appropriate configuration.
    strip = getStrip(LED_PIN, LED_CHANNEL, LED_BRIGHTNESS)
    # Intialize the library (must be called once before other functions).
    strip.begin()
    
    matrixAnimator = MatrixAnimator(strip)
    currentMatrixInfo = (CandleMatrix(randrange(4)), 10 + 180 * random())
    matrixAnimator.setMatrix(currentMatrixInfo[0])

    matrixAnimator.start()
    
    try:
        while True:
            sleep(currentMatrixInfo[1])
            newMatrixInfo = getNewMatrix()
            matrixAnimator.moveToMatrix(newMatrixInfo[0], 2)
            currentMatrixInfo[0].close()
            currentMatrixInfo = newMatrixInfo

    except KeyboardInterrupt:
        currentMatrixInfo[0].close()
        matrixAnimator.stop()
