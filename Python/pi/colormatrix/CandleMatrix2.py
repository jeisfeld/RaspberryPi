'''
A 8x8 matrix with 2 candles.
'''

from colormatrix.ImageMatrix import ImageMatrix
from colormatrix.Color import Color
from colormatrix.ColorTemperature import getRandomColor
from random import random, sample
from time import sleep, time
from threading import Thread

DARKRED = Color(20, 0, 0)
DARKESTYELLOW = Color(8, 8, 0)
DARKYELLOW = Color(25, 25, 0)
MIN_TEMP = 1000
MAX_TEMP = 3000


class CandleMatrix2(ImageMatrix):
    '''
    classdocs
    '''

    def __init__(self, brightness=255, candleNumbers=[0, 1], suppressThread=False, candlePositions=None):
        ImageMatrix.__init__(self)
        self._brightness = brightness
        self._candleNumbers = candleNumbers
        
        if candlePositions == None:
            xpositionsample = sample(range(5), 2)
            xpositionsample.sort()
            ypositionsample = sample(range(3), 2)
            self._candlePositions = []
            self._candlePositions.append((xpositionsample[0], ypositionsample[0]))
            self._candlePositions.append((xpositionsample[1] + 2, ypositionsample[1]))
        else:
            self._candlePositions = candlePositions
        
        for (x, y) in self._candlePositions:
            self.setColorRect(x, y + 2, x + 1, y + 5, DARKRED * self._brightness / 255)
        
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

    def setCandleColorsOneColumn(self, x, y):
        color0 = getRandomColor(MIN_TEMP, MAX_TEMP, 0, 0.1 * self._brightness / 255, 4)
        color1 = color0 + getRandomColor(MIN_TEMP, MAX_TEMP, 0, 0.4 * self._brightness / 255, 2)
        color2 = color1 + getRandomColor(MIN_TEMP, MAX_TEMP, 0, 0.4 * self._brightness / 255, 3)
        self.setColor(x, y, DARKESTYELLOW * self._brightness / 255 + color1)
        self.setColor(x, y + 1, DARKYELLOW * self._brightness / 255 + color2)
        
    def setCandleColorsOfCandle(self, i):
        self.setCandleColorsOneColumn(self._candlePositions[i][0], self._candlePositions[i][1])
        self.setCandleColorsOneColumn(self._candlePositions[i][0] + 1, self._candlePositions[i][1])

    def getColor(self, x, y):
        if len(self._candleNumbers) < 2:
            return self._matrix[y][x]
        else:
            if x < self._candlePositions[1][0]:
                index = 0
            else:
                index = 1
                
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
            newCandleMatrix = CandleMatrix2(brightness=self._candleMatrix._brightness, candleNumbers=[self._index], suppressThread=True, candlePositions=self._candleMatrix._candlePositions)
            newCandleMatrix.setCandleColorsOfCandle(self._index)
            duration = 0.02 + 2 * random()
            (self._candleMatrix._oldMatrix[self._index], self._candleMatrix._changeTime[self._index],
                    self._candleMatrix._newMatrix[self._index], self._candleMatrix._duration[self._index]) = \
                (self._candleMatrix._newMatrix[self._index], time(), newCandleMatrix._matrix, duration)
            sleep(duration)
    
