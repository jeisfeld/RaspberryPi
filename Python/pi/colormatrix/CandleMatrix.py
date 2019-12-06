'''
Created on 28.11.2019

@author: Joerg
'''

from colormatrix.ImageMatrix import ImageMatrix
from colormatrix.Color import Color
from colormatrix.ColorTemperature import getRandomColor
from random import random
from time import sleep, time
from threading import Thread

DARKRED = Color(20, 0, 0)
DARKESTYELLOW = Color(5, 5, 0)
DARKYELLOW = Color(25, 25, 0)
MIN_TEMP = 1000
MAX_TEMP = 3000


class CandleMatrix(ImageMatrix):
    '''
    classdocs
    '''

    def __init__(self, position=0, candleNumbers=[0, 1, 2], suppressThread=False):
        ImageMatrix.__init__(self)
        self._position = position
        self._candleNumbers = candleNumbers
        if position == 0:
            self.setCandlePosition(1, 2, 0)
        elif position == 1:
            self.setCandlePosition(0, 2, 1)
        elif position == 2:
            self.setCandlePosition(2, 0, 1)
        else:
            self.setCandlePosition(1, 0, 2)
        
        if not suppressThread:
            self._oldMatrix = [None] * len(candleNumbers)
            self._newMatrix = [None] * len(candleNumbers)
            self._changeTime = [None] * len(candleNumbers)
            self._duration = [None] * len(candleNumbers)
            self._thread = [None] * len(candleNumbers)
            for index in candleNumbers:
                self._oldMatrix[index] = self._matrix
                self._newMatrix[index] = self._matrix
                self._changeTime[index] = time()
                self._duration[index] = 1
                self._thread[index] = CandleAnimator(self, index)
            for index in candleNumbers:
                self._thread[index].start()
 
    def setCandlePosition(self, first, second, third):
        self._candlePositions = (first, second, third)
        self.setColorRect(0, first + 2, 1, first + 5, DARKRED)
        self.setColorRect(3, second + 2, 4, second + 5, DARKRED)
        self.setColorRect(6, third + 2, 7, third + 5, DARKRED)

    def setCandleColorsOneColumn(self, x, y):
        color1 = getRandomColor(MIN_TEMP, MAX_TEMP, 0, 0.3)
        color2 = color1 + getRandomColor(MIN_TEMP, MAX_TEMP, 0, 0.6)
        self.setColor(x, y, DARKESTYELLOW + color1)
        self.setColor(x, y + 1, DARKYELLOW + color2)
        
    def setCandleColorsOfCandle(self, i):
        self.setCandleColorsOneColumn(3 * i, self._candlePositions[i])
        self.setCandleColorsOneColumn(3 * i + 1, self._candlePositions[i])

    def getColor(self, x, y):
        if len(self._candleNumbers) < 2:
            return self._matrix[y][x]
        else:
            if x < 3:
                index = 0
            elif x < 6:
                index = 1
            else:
                index = 2
                
            quota = (time() - self._changeTime[index]) / self._duration[index]
            if quota > 1:
                quota = 1

            return self._newMatrix[index][y][x] * quota + self._oldMatrix[index][y][x] * (1 - quota)
    
    def close(self):
        for index in self._candleNumbers:
            self._thread[index]._stopped = True


class CandleAnimator(Thread):
    
    def __init__(self, candleMatrix, index):
        Thread.__init__(self)
        self._candleMatrix = candleMatrix
        self._index = index
        self._stopped = False
    
    def run(self):
        while not self._stopped:
            newCandleMatrix = CandleMatrix(self._candleMatrix._position, candleNumbers=[self._index], suppressThread=True)
            newCandleMatrix.setCandleColorsOfCandle(self._index)
            duration = 0.02 + random()
            (self._candleMatrix._oldMatrix[self._index], self._candleMatrix._changeTime[self._index], 
                    self._candleMatrix._newMatrix[self._index], self._candleMatrix._duration[self._index]) = \
                (self._candleMatrix._newMatrix[self._index], time(), newCandleMatrix._matrix, duration)
            sleep(duration)
    
