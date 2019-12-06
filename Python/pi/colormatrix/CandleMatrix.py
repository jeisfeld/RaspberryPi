'''
Created on 28.11.2019

@author: Joerg
'''

from colormatrix.ImageMatrix import ImageMatrix
from colormatrix.Color import Color
from colormatrix.ColorTemperature import getRandomColor
from random import randrange
from copy import deepcopy
from time import sleep, time
from threading import Thread

DARKRED = Color(20, 0, 0)
DARKESTYELLOW = Color(5, 5, 0)
DARKYELLOW = Color(25, 25, 0)
MIN_TEMP = 1000
MAX_TEMP = 3000
FLAME_DURATION = 0.2


class CandleMatrix(ImageMatrix):
    '''
    classdocs
    '''

    def __init__(self, position=randrange(4), suppressThread = False):
        ImageMatrix.__init__(self)
        self._position = position
        if position == 0:
            self.setCandlePosition(1, 2, 0)
        elif position == 1:
            self.setCandlePosition(0, 2, 1)
        elif position == 2:
            self.setCandlePosition(2, 0, 1)
        else:
            self.setCandlePosition(1, 0, 2)
            
        self._newMatrix = self._matrix
        self._changeTime = time()
        
        if not suppressThread:
            self._thread = CandleAnimator(self)
            self._thread.start()

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
        
    def setCandleColorsOneCandle(self, i):
        self.setCandleColorsOneColumn(3 * i, self._candlePositions[i])
        self.setCandleColorsOneColumn(3 * i + 1, self._candlePositions[i])

    def setCandleColors(self):
        self.setCandleColorsOneCandle(0)
        self.setCandleColorsOneCandle(1)
        self.setCandleColorsOneCandle(2)
    
    def getUpdatedFlames(self):
        matrix = deepcopy(self)
        matrix.setCandleColorsOneCandle(0)
        matrix.setCandleColorsOneCandle(1)
        matrix.setCandleColorsOneCandle(2)
        return matrix

    def getColor(self, x, y):
        quota = (time() - self._changeTime) / FLAME_DURATION
        if quota > 1:
            quota = 1
        return self._newMatrix[y][x] * quota + self._matrix[y][x] * (1 - quota)
    
    def close(self):
        self._thread._stopped = True


class CandleAnimator(Thread):
    
    def __init__(self, candle):
        Thread.__init__(self)
        self._candle = candle
        self._stopped = False
    
    def run(self):
        while not self._stopped:
            newCandleMatrix = CandleMatrix(self._candle._position, suppressThread=True)
            newCandleMatrix.setCandleColors()
            (self._candle._matrix, self._candle._changeTime, self._candle._newMatrix) = (self._candle._newMatrix, time(), newCandleMatrix._matrix)
            sleep(FLAME_DURATION)
    
