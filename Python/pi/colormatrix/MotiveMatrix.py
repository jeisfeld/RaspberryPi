'''
Created on 28.11.2019

@author: Joerg
'''

from copy import deepcopy
from colormatrix.AbstractMatrix import AbstractMatrix


class MotiveMatrix(AbstractMatrix):
    '''
    classdocs
    '''

    def __init__(self, motive, width=8, height=8):
        self._width = width
        self._height = height
        self._motive = deepcopy(motive)
    
    def setColor(self, x, y, color):
        self._motive[y][x] = color
        
    def getColor(self, x, y):
        return self._motive[y][x]
