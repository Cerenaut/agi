
class ValIncrementer:
    min = 0
    max = 0
    delta = 0
    val = 0
    counting = False

    def __init__(self, minv, maxv, deltav):
        """" The class "constructor" - It's actually an initializer """

        self.min = minv
        self.max = maxv
        self.delta = deltav
        self.counting = False

    def value(self):
        return self.val

    def reset(self):
        self.val = self.min
        self.counting = False

    def increment(self):
        """
        First call sets the counter value to the minimum.
        Subsequent calls increment it.
        :return: If the value exceeds the maximum, then return false
        """
        if not self.counting:
            self.val = self.min
            self.counting = True
        else:
            self.val += self.delta
            if self.val > self.max:
                self.counting = False

        return self.counting

