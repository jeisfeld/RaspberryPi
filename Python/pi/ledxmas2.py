#!/usr/bin/env python3
#
# Test 64x64 LED display

from colormatrix.LedDisplay import getStrip
from colormatrix.MatrixAnimator import MatrixAnimator
from colormatrix.CandleMatrix2 import CandleMatrix2
from time import sleep
from random import random
from sys import argv

global LED_BRIGHTNESS

LED_PIN = 18  # GPIO pin connected to the pixels (18 uses PWM!).
LED_CHANNEL = 0  # set to '1' for GPIOs 13, 19, 41, 45 or 53

MIN_TEMP = 1000
MAX_TEMP = 3000


def getNewMatrix(brightness):
    return (CandleMatrix2(brightness=brightness), 10 + 180 * random())


# Main program logic follows:
if __name__ == '__main__':
    brightness = 63
    
    if len(argv) >= 2:
        brightness = int(argv[1])

    # Create NeoPixel object with appropriate configuration.
    strip = getStrip(LED_PIN, LED_CHANNEL)
    # Intialize the library (must be called once before other functions).
    strip.begin()
    
    matrixAnimator = MatrixAnimator(strip)
    currentMatrixInfo = getNewMatrix(brightness)
    matrixAnimator.setMatrix(currentMatrixInfo[0])

    matrixAnimator.start()
    
    try:
        while True:
            sleep(currentMatrixInfo[1])
            newMatrixInfo = getNewMatrix(brightness)
            matrixAnimator.moveToMatrix(newMatrixInfo[0], 2)
            currentMatrixInfo[0].close()
            currentMatrixInfo = newMatrixInfo

    except KeyboardInterrupt:
        currentMatrixInfo[0].close()
        matrixAnimator.stop()
