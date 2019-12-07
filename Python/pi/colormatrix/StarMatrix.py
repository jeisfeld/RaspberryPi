'''
Created on 28.11.2019

@author: Joerg
'''

from colormatrix.ImageMatrix import ImageMatrix
from colormatrix.Color import Color
from colormatrix.ColorTemperature import getRandomBrightness, getRandomColorTemperature, convertColorTemperature
from colormatrix.LedDisplay import HEIGHT, WIDTH
from random import random, shuffle
from time import sleep, time
from math import exp, log
from threading import Thread

DARKRED = Color(20, 0, 0)
DARKESTYELLOW = Color(5, 5, 0)
DARKYELLOW = Color(25, 25, 0)
MIN_TEMP = 2000
MAX_TEMP = 6000
BRIGHTNESS_VARIATION = 0.3
TEMPERATURE_VARIATION = 0.5

class StarMatrix(ImageMatrix):
    '''
    classdocs
    '''

    def __init__(self, count=5, suppressThread=False):
        ImageMatrix.__init__(self)
        self.definePositions(count)
        
        if not suppressThread:
            self._changeTime = {}
            self._duration = {}
            self._thread = []
            self._oldSpecialValues = {}
            self._newSpecialValues = {}
            self._starBrightness = {}
            self._starTemperature = {}
            
            for position in self._positions:
                self._changeTime[position] = time()
                self._duration[position] = 1
                self._starBrightness[position] = getRandomBrightness(0.1, 0.5)
                self._starTemperature[position] = getRandomColorTemperature(MIN_TEMP, MAX_TEMP)
                self._oldSpecialValues[position] = self._matrix[position[1]][position[0]]
                self._newSpecialValues[position] = self._matrix[position[1]][position[0]]
                self._thread.append(StarAnimator(self, position))
            for thread in self._thread:
                thread.start()

    def definePositions(self, count):
        rows = list(range(HEIGHT))
        cols = list(range(WIDTH))
        shuffle(rows)
        shuffle(cols)
        
        self._positions = []
        for index in range(count):
            self._positions.append((cols[index], rows[index]))
            

    def getColor(self, x, y):
        position = (x, y)
        if position in self._positions:
            quota = (time() - self._changeTime[position]) / self._duration[position]
            if quota > 1:
                quota = 1
            return self._newSpecialValues[position] * quota + self._oldSpecialValues[position] * (1 - quota)
        else:
            return self._matrix[y][x]
    
    def close(self):
        for thread in self._thread:
            thread._stopped = True


class StarAnimator(Thread):
    
    def __init__(self, starMatrix, position):
        Thread.__init__(self)
        self._starMatrix = starMatrix
        self._position = position
        self._stopped = False
    
    def run(self):
        while not self._stopped:
            newBrightness = (1-BRIGHTNESS_VARIATION * random()) * self._starMatrix._starBrightness[self._position]
            newTemperature = exp(log(self._starMatrix._starTemperature[self._position]) + (random() - 0.5) * TEMPERATURE_VARIATION)
            newColor = convertColorTemperature(newTemperature, newBrightness)
            duration = 0.1 + 3 * random()
            (self._starMatrix._oldSpecialValues[self._position], self._starMatrix._changeTime[self._position],
                    self._starMatrix._newSpecialValues[self._position], self._starMatrix._duration[self._position]) = \
                (self._starMatrix._newSpecialValues[self._position], time(), newColor, duration)
            sleep(duration)
    
