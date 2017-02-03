import numpy

class ValueSeries:
    series = []
    idx = 0
    overflow = False

    def __init__(self, series):
        self.idx = 0
        self.overflow = False
        self.series = series

    @classmethod
    def from_range(cls, minv, maxv, deltav):
        series = numpy.arange(minv, maxv, deltav)
        return cls(series)

    def value(self):
        return self.series[self.idx]

    def overflowed(self):
        return self.overflow

    def reset(self):
        self.idx = 0
        self.overflow = False

    def next_val(self):
        """ Iterate through If the value exceeds the maximum, then return false """

        if not self.overflow:
            self.idx += 1

            if self.idx >= len(self.series):
                self.idx = 0
                self.overflow = True

        return self.overflow