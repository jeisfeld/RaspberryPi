'''
Created on 28.11.2019

@author: Joerg
'''


class Color(object):
    '''
    classdocs
    '''

    def __init__(self, red, green, blue):
        self._red = red
        self._green = green
        self._blue = blue
        
    def value(self):
        return (round(self._red) << 16) | (round(self._green) << 8) | round(self._blue)

    def __str__(self):
        return "Color(%d,%d,%d)" % (self._red, self._green, self._blue)
    
    def __add__(self, other):
        return Color(self._red + other._red, self._green + other._green, self._blue + other._blue)
    
    def __truediv__(self, divisor):
        return Color(self._red / divisor, self._green / divisor, self._blue / divisor)
    
    def __mul__(self, factor):
        return Color(self._red * factor, self._green * factor, self._blue * factor)
    
