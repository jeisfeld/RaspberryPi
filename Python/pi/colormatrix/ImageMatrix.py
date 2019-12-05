'''
Created on 28.11.2019

@author: Joerg
'''

from copy import deepcopy
from colormatrix.AbstractMatrix import AbstractMatrix
from colormatrix.Color import BLACK

BLACKMATRIX = [
    [BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK],
    [BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK],
    [BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK],
    [BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK],
    [BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK],
    [BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK],
    [BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK],
    [BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK, BLACK]
    ]


class ImageMatrix(AbstractMatrix):
    '''
    classdocs
    '''

    def __init__(self, motive=BLACKMATRIX, width=8, height=8):
        self._width = width
        self._height = height
        self._matrix = deepcopy(motive)
    
    def setColor(self, x, y, color):
        self._matrix[y][x] = color
        
    def setColorRect(self, x1, y1, x2, y2, color):
        for x in range(x1, x2 + 1):
            for y in range(y1, y2 + 1):
                self.setColor(x, y, color)
    
    def getColor(self, x, y):
        return self._matrix[y][x]
