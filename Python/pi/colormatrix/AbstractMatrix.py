'''
Created on 05.12.2019

@author: Joerg
'''

from colormatrix.LedDisplay import WIDTH, HEIGHT
from time import sleep

class AbstractMatrix(object):
    '''
    classdocs
    '''
    
    def getCombinedColor(self, other, quota, x, y):
        colorself = self.getColor(x, y)
        colorother = other.getColor(x, y)
        return colorother * quota + colorself * (1 - quota)



    def __init__(self, params):
        '''
        Constructor
        '''
        
        
        

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
