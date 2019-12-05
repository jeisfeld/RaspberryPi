'''
Created on 28.11.2019

@author: Joerg
'''

from colormatrix.Color import Color
from colormatrix.AbstractMatrix import AbstractMatrix
from math import sqrt
from colormatrix.ColorTemperature import convertColorTemperature


class ColorMatrix(AbstractMatrix):
    '''
    classdocs
    '''

    def __init__(self, baseColor=Color(0, 0, 0), width=8, height=8):
        self._width = width
        self._height = height
        self._size = max(width, height)
        self._colors = {(0, 0): baseColor, (width - 1, 0):baseColor, (0, height - 1):baseColor, (width - 1, height - 1):baseColor}
    
    def setColor(self, x, y, color):
        self._colors[(x, y)] = color
        self._size = min(self._size, max(x + 1, self._width - x, y + 1, self._width - y))
        
    def getColor(self, x, y):
        weights = {}
        
        for (keyx, keyy), color in self._colors.items():
            distance = sqrt((keyx - x) ** 2 + (keyy - y) ** 2)
            weight = self._size - 1 - distance
            if weight > 0:
                weights[color] = weight
            
        sumweights = sum(weights.values())
        
        result = Color(0, 0, 0)
        for color, weight in weights.items():
            result += color * weight
        
        return result / sumweights
