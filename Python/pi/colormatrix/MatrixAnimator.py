'''
Created on 05.12.2019

@author: Joerg
'''

from threading import Thread
from time import sleep, time
from colormatrix.ColorMatrix import ColorMatrix
from colormatrix.Color import BLACK
from colormatrix.LedDisplay import colorFill


class MatrixAnimator(Thread):
    '''
    classdocs
    '''

    def __init__(self, strip):
        Thread.__init__(self)
        self._strip = strip
        self._stopped = False
        self._matrix = ColorMatrix(BLACK)
        self._moveData = None

    def stop(self):    
        self._stopped = True
        
    def setMatrix(self, matrix):
        self._matrix = matrix
        
    def moveToMatrix(self, newMatrix, duration):
        self._moveData = ( time(), duration, newMatrix)
        sleep(duration)
        self._matrix = newMatrix
        self._moveData = None
        
    def getCurrentMatrix(self):
        if(self._moveData):
            currentTime = time()
            quota = (currentTime - self._moveData[0]) / self._moveData[1]
            if quota > 1:
                quota = 1
            return self._matrix.getCombinedMatrix(self._moveData[2], quota)
        else:
            return self._matrix
        
        
    def run(self):
        while not self._stopped:
            self.getCurrentMatrix().display(self._strip)
            sleep(0.02)
        colorFill(self._strip, BLACK)
        
        
