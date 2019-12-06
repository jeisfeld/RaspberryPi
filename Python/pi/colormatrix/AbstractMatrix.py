'''
Created on 05.12.2019

@author: Joerg
'''

from colormatrix.LedDisplay import WIDTH, HEIGHT
from time import sleep
from types import MethodType
from copy import copy
from colormatrix.Color import BLACK


class AbstractMatrix(object):
    '''
    classdocs
    '''
    
    def display(self, strip):
        matrixFill(strip, self)
        
    def getColor(self):  # This method must be overridden in any specific implementation
        return BLACK

    def __add__(self, other):
        def getColor(newself, x, y):
            return self.getColor(x, y) + other.getColor(x, y)
        result = copy(self)
        result.getColor = MethodType(getColor, result)
        return result
    
    def __truediv__(self, divisor):
        def getColor(newself, x, y):
            return self.getColor(x, y) / divisor
        result = copy(self)
        result.getColor = MethodType(getColor, result)
        return result
    
    def __mul__(self, factor):
        def getColor(newself, x, y):
            return self.getColor(x, y) * factor
        result = copy(self)
        result.getColor = MethodType(getColor, result)
        return result

    def getCombinedColor(self, other, quota, x, y):
        colorself = self.getColor(x, y)
        colorother = other.getColor(x, y)
        return colorother * quota + colorself * (1 - quota)

    def getCombinedMatrix(self, other, quota):
        return other * quota + self * (1 - quota)

    def __init__(self, params):
        '''
        Constructor
        '''

    def close(self):
        pass

def matrixFill(strip, matrix):
    for x in range(WIDTH):
        for y in range(HEIGHT):
            strip.setPixelColor(x + y * WIDTH, matrix.getColor(x, y).value())
    strip.show()


def moveToMatrix(strip, matrix1, matrix2, duration):
    steps = int(max(min(duration * 400, 100), 1))
    sleepDuration = duration / steps
    
    for i in range(1, steps + 1):
        sleep(sleepDuration)
        for x in range(WIDTH):
            for y in range(HEIGHT):
                strip.setPixelColor(x + y * WIDTH, matrix1.getCombinedColor(matrix2, i / steps, x, y).value())
        strip.show()
        
    
