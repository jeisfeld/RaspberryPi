#!/usr/bin/env python3
#
# Test 64x64 LED display

from colormatrix.LedDisplay import getStrip
from colormatrix.Color import Color, BLACK
from colormatrix.ImageMatrix import ImageMatrix
from colormatrix.MatrixAnimator import MatrixAnimator
from colormatrix.CandleMatrix import CandleMatrix
from time import sleep
from datetime import datetime
from random import randrange, random
from sys import argv
from colormatrix.StarMatrix import StarMatrix

global LED_BRIGHTNESS

LED_PIN = 18  # GPIO pin connected to the pixels (18 uses PWM!).
LED_CHANNEL = 0  # set to '1' for GPIOs 13, 19, 41, 45 or 53

MIN_TEMP = 1000
MAX_TEMP = 3000

GREEN = Color(0, 30, 0)
BROWN = Color(12, 6, 0)

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


def getNewMatrix(brightness=None):
    if brightness == None:
        brightness = getBrightnessByHour()

    rand = random()
    
    if rand < 0.2:
        return (TREE * brightness / 255, 2 + 10 * random())
    elif rand < 0.6:
        return getNewCandleMatrix(brightness)
    else:
        return (StarMatrix(brightness=brightness, count=randrange(4, 9)), 10 + 60 * random())


def getNewCandleMatrix(brightness):
    hour = datetime.now().hour + datetime.now().minute / 60

    if hour > 19 or hour < 3:
        candleSize = 1
    elif hour > 18:
        candleSize = 2
    elif hour > 17:
        candleSize = 3
    else:
        candleSize = 4
    return (CandleMatrix(brightness=brightness, candleSize=candleSize, candleCount=3), 10 + 180 * random())

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
        initialBrightness = brightness
    else:
        brightness = None
        initialBrightness = getBrightnessByHour()

    # Create NeoPixel object with appropriate configuration.
    strip = getStrip(LED_PIN, LED_CHANNEL)
    # Intialize the library (must be called once before other functions).
    strip.begin()
    
    matrixAnimator = MatrixAnimator(strip)
    currentMatrixInfo = getNewCandleMatrix(initialBrightness)
#    currentMatrixInfo = (StarMatrix(brightness=63, count=randrange(4, 9)), 10 + 180 * random())
#    currentMatrixInfo = (TREE * 63 / 255, 10 + 180 * random())
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