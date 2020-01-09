from time import sleep
from math import log, exp
from colormatrix.ColorTemperature import convertColorTemperature
from neopixel import Adafruit_NeoPixel

WIDTH = 8
HEIGHT = 8
LED_COUNT = WIDTH * HEIGHT  # Number of LED pixels.


def getStrip(pin, channel, brightness=255):
    return Adafruit_NeoPixel(LED_COUNT, pin, channel, brightness)


def colorFill(strip, color):
    """Fill display with color."""
    for i in range(strip.numPixels()):
        strip.setPixelColor(i, color.value())
    strip.show()


def moveToColor(strip, temperature1, brightness1, temperature2, brightness2, duration):
    steps = max(min(duration * 400, 255), 1)
    sleepDuration = duration / steps
    logTemperatureStart = log(temperature1)
    logTemperatureDiff = (log(temperature2) - log(temperature1)) / steps;
    brightnessDiff = (brightness2 - brightness1) / steps
    
    for i in range(1, steps + 1):
        sleep(sleepDuration)
        colorFill(strip, convertColorTemperature(exp(logTemperatureStart + i * logTemperatureDiff), brightness1 + i * brightnessDiff))
