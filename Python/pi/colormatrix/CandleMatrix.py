'''
A 8x8 matrix with 3 candles.
'''

from colormatrix.ImageMatrix import ImageMatrix
from colormatrix.Color import Color
from colormatrix.ColorTemperature import getRandomColor
from random import random, randrange, sample
from time import sleep, time
from threading import Thread

DARKRED = Color(20, 0, 0)
DARKESTYELLOW = Color(8, 8, 0)
DARKYELLOW = Color(25, 25, 0)
MIN_TEMP = 1000
MAX_TEMP = 3000


class CandleMatrix(ImageMatrix):
    '''
    classdocs
    '''

    def __init__(self, brightness=255, candleCount=3, suppressThread=False, candleSize=4, candlePositions=None):
        ImageMatrix.__init__(self)
        self._brightness = brightness
        self._candleCount = candleCount
        self._candleSize = candleSize
        
        if candlePositions == None:
            if candleCount == 1:
                xposition = randrange(7)
                self._candlePositions = []
                self._candlePositions.append((xposition, 6 - self._candleSize))
            elif candleCount == 2:
                xpositionsample = sample(range(5), 2)
                xpositionsample.sort()
                ypositionsample = sample(range(5, 7), 2)
                self._candlePositions = []
                self._candlePositions.append((xpositionsample[0], ypositionsample[0] - self._candleSize))
                self._candlePositions.append((xpositionsample[1] + 2, ypositionsample[1] - self._candleSize))                
            elif candleCount == 3:
                position = randrange(4)
                if position == 0:
                    ypositionsample = [5, 6, 4]
                elif position == 1:
                    ypositionsample = [4, 6, 5]
                elif position == 2:
                    ypositionsample = [6, 4, 5]
                else:
                    ypositionsample = [5, 4, 6]
                self._candlePositions = []
                self._candlePositions.append((0, ypositionsample[0] - self._candleSize))
                self._candlePositions.append((3, ypositionsample[1] - self._candleSize))
                self._candlePositions.append((6, ypositionsample[2] - self._candleSize))
            else:
                xpositiongap = randrange(5)
                yindicator1 = randrange(2)
                yindicator2 = randrange(2)
                self._candlePositions = []
                self._candlePositions.append((0 if xpositiongap > 0 else 1, 4 + yindicator1 - self._candleSize))
                self._candlePositions.append((2 if xpositiongap > 1 else 3, 3 + 3 * yindicator2 - self._candleSize))
                self._candlePositions.append((4 if xpositiongap > 2 else 5, 6 - 3 * yindicator2 - self._candleSize))
                self._candlePositions.append((6 if xpositiongap > 3 else 7, 5 - yindicator1 - self._candleSize))
                
        else:
            self._candlePositions = candlePositions
        
        if len(self._candlePositions) == 4:
            for (x, y) in self._candlePositions:
                self.setColorRect(x, y + 2, x, 7, DARKRED * self._brightness / 255)
        else:
            for (x, y) in self._candlePositions:
                self.setColorRect(x, y + 2, x + 1, 7, DARKRED * self._brightness / 255)
                    
        if not suppressThread:
            self._oldMatrix = [None] * candleCount
            self._newMatrix = [None] * candleCount
            self._changeTime = [None] * candleCount
            self._duration = [None] * candleCount
            self._thread = [None] * candleCount
            for index in range(candleCount):
                self._oldMatrix[index] = self._matrix
                self._newMatrix[index] = self._matrix
                self._changeTime[index] = time()
                self._duration[index] = 1
                self._thread[index] = CandleAnimator(self, index)
            for index in range(candleCount):
                self._thread[index].start()

    def setCandleColorsOneColumn(self, x, y):
        color0 = getRandomColor(MIN_TEMP, MAX_TEMP, 0, 0.1 * self._brightness / 255, 4)
        color1 = color0 + getRandomColor(MIN_TEMP, MAX_TEMP, 0, 0.4 * self._brightness / 255, 2)
        color2 = color1 + getRandomColor(MIN_TEMP, MAX_TEMP, 0, 0.4 * self._brightness / 255, 3)
        self.setColor(x, y, DARKESTYELLOW * self._brightness / 255 + color1)
        self.setColor(x, y + 1, DARKYELLOW * self._brightness / 255 + color2)
        
    def setCandleColorsOfCandle(self, i):
        self.setCandleColorsOneColumn(self._candlePositions[i][0], self._candlePositions[i][1])
        if len(self._candlePositions) < 4:
            self.setCandleColorsOneColumn(self._candlePositions[i][0] + 1, self._candlePositions[i][1])

    def getColor(self, x, y):
        if self._candleCount == 1 or x < self._candlePositions[1][0]:
            index = 0
        elif self._candleCount == 2 or x < self._candlePositions[2][0]:
            index = 1
        elif self._candleCount == 3 or x < self._candlePositions[3][0]:
            index = 2
        else:
            index = 3
            
        quota = (time() - self._changeTime[index]) / self._duration[index]
        if quota > 1:
            quota = 1

        return self._newMatrix[index][y][x] * quota + self._oldMatrix[index][y][x] * (1 - quota)
    
    def close(self):
        for index in range(self._candleCount):
            self._thread[index]._stopped = True


class CandleAnimator(Thread):
    def __init__(self, candleMatrix, index):
        Thread.__init__(self)
        self._candleMatrix = candleMatrix
        self._index = index
        self._stopped = False
    
    def run(self):
        while not self._stopped:
            newCandleMatrix = CandleMatrix(brightness=self._candleMatrix._brightness, candleCount=[self._candleMatrix._candleCount], suppressThread=True,
                                            candleSize=self._candleMatrix._candleSize, candlePositions=self._candleMatrix._candlePositions)
            newCandleMatrix.setCandleColorsOfCandle(self._index)
            duration = 0.02 + 2 * random()
            (self._candleMatrix._oldMatrix[self._index], self._candleMatrix._changeTime[self._index],
                    self._candleMatrix._newMatrix[self._index], self._candleMatrix._duration[self._index]) = \
                (self._candleMatrix._newMatrix[self._index], time(), newCandleMatrix._matrix, duration)
            sleep(duration)
    
