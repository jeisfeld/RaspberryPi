#!/usr/bin/env python3
#
# Test 64x64 LED display

from colormatrix.LedDisplay import getStrip
from colormatrix.MatrixAnimator import MatrixAnimator
from colormatrix.CandleMatrix import CandleMatrix
from time import sleep
from datetime import datetime
from random import random
from sys import argv

global LED_BRIGHTNESS

LED_PIN = 18  # GPIO pin connected to the pixels (18 uses PWM!).
LED_CHANNEL = 0  # set to '1' for GPIOs 13, 19, 41, 45 or 53

MIN_TEMP = 1000
MAX_TEMP = 3000


def getNewMatrix(brightness=None):
    hour = datetime.now().hour + datetime.now().minute / 60
    if hour > 22 or hour < 3:
        candleSize = 1
    elif hour > 18:
        candleSize = 2
    elif hour > 14:
        candleSize = 3
    elif hour > 10:
        candleSize = 4
    else:
        candleSize = 5
    
    if brightness == None:
        brightness = getBrightnessByHour()
    
    return (CandleMatrix(brightness=brightness, candleSize=candleSize, candleCount=2), 10 + 180 * random())

def getBrightnessByHour():
    hour = datetime.now().hour + datetime.now().minute / 60

    if hour > 8.5 and hour < 16:
        return 63
    elif hour > 16.5 or hour < 8:
        return 31
    elif hour < 9:
        return 31 + 64 * (hour - 8) 
    else:
        return 63 - 64 * (hour - 16) 


# Main program logic follows:
if __name__ == '__main__':
    brightness = 63
    
    if len(argv) >= 2:
        brightness = int(argv[1])
    else:
        brightness = None

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
