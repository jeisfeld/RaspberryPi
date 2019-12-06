#!/usr/bin/env python3
#
# Test 64x64 LED display

from colormatrix.LedDisplay import getStrip
from colormatrix.Color import Color, BLACK
from colormatrix.ImageMatrix import ImageMatrix
from colormatrix.MatrixAnimator import MatrixAnimator
from colormatrix.CandleMatrix import CandleMatrix
from time import sleep
from random import randrange
from sys import argv

global LED_BRIGHTNESS

# LED strip configuration:
LED_PIN = 18  # GPIO pin connected to the pixels (18 uses PWM!).
# LED_PIN        = 10      # GPIO pin connected to the pixels (10 uses SPI /dev/spidev0.0).
LED_BRIGHTNESS = 255  # Set to 0 for darkest and 255 for brightest
LED_CHANNEL = 0  # set to '1' for GPIOs 13, 19, 41, 45 or 53

MIN_TEMP = 1000
MAX_TEMP = 3000

GREEN = Color(0, 20, 0)
BROWN = Color(5, 3, 0)

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


# Main program logic follows:
if __name__ == '__main__':
    if len(argv) >= 2:
        LED_BRIGHTNESS = int(argv[1])
    

    # Create NeoPixel object with appropriate configuration.
    strip = getStrip(LED_PIN, LED_CHANNEL, LED_BRIGHTNESS)
    # Intialize the library (must be called once before other functions).
    strip.begin()
    
    matrixAnimator = MatrixAnimator(strip)
    candleMatrix = CandleMatrix(randrange(4))
    matrixAnimator.setMatrix(candleMatrix)
    matrixAnimator.start()

    matrix2 = TREE
    
    try:
        while True:
            sleep(20)
            
            matrixAnimator.moveToMatrix(matrix2, 2)
            candleMatrix.close()
            sleep(2)

            candleMatrix = CandleMatrix(randrange(4))
            matrixAnimator.moveToMatrix(candleMatrix, 2)

    except KeyboardInterrupt:
        candleMatrix.close()
        matrixAnimator.stop()
