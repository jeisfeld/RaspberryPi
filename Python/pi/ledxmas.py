#!/usr/bin/env python3
#
# Test 64x64 LED display

from colormatrix.LedDisplay import getStrip
from colormatrix.Color import Color, BLACK
from colormatrix.ImageMatrix import ImageMatrix
from colormatrix.MatrixAnimator import MatrixAnimator
from colormatrix.CandleMatrix import CandleMatrix
from time import sleep
from datetime import datetime, timedelta
from random import random
from sys import argv

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
#    elif rand < 0.6:
#        return (StarMatrix(brightness=brightness, count=randrange(4, 9)), 10 + 60 * random())
    else:
        return getNewCandleMatrix(brightness)


def getNewCandleMatrix(brightness):
    hour = datetime.now().hour + datetime.now().minute / 60
    currentday = datetime.now() - timedelta(hours = 4)
    date = currentday.day
    month = currentday.month
        
    if month == 1:
        candleCount = 3 if date > 6 else 4
    elif month == 11:
        candleCount = 1 if date >= 27 else 3
    elif month == 12:
        candleCount = 4 if date >= 18 else 3 if date >= 11 else 2 if date >= 4 else 1
    else:
        candleCount = 3

    if candleCount == 1:
        if hour > 22 or hour < 4:
            candleSize = 2
        elif hour > 18:
            candleSize = 3
        elif hour > 14:
            candleSize = 4
        elif hour > 10:
            candleSize = 5
        else:
            candleSize = 6        
    elif candleCount == 2:
        if hour > 22 or hour < 4:
            candleSize = 1
        elif hour > 18:
            candleSize = 2
        elif hour > 14:
            candleSize = 3
        elif hour > 10:
            candleSize = 4
        else:
            candleSize = 5        
    elif candleCount == 3:
        if hour > 22 or hour < 4:
            candleSize = 1
        elif hour > 16:
            candleSize = 2
        elif hour > 10:
            candleSize = 3
        else:
            candleSize = 4
    else:
        if hour > 22 or hour < 4:
            candleSize = 1
        elif hour > 14:
            candleSize = 2
        else:
            candleSize = 3

    return (CandleMatrix(brightness=brightness, candleSize=candleSize, candleCount=candleCount), 10 + 180 * random())


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
