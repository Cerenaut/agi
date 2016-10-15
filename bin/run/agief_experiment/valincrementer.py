
class ValIncrementer:
    min = 0
    max = 0
    delta = 0
    val = 0
    counting = False

    # The class "constructor" - It's actually an initializer
    def __init__(self, minv, maxv, deltav):
        self.min = minv
        self.max = maxv
        self.delta = deltav
        self.counting = False

    def value(self):
        return self.val

    def reset(self):
        self.val = self.min
        self.counting = False

    # First call sets the counter value to the minimum
    # Subsequent calls increment it
    # If the value exceeds the maximum, then return false
    def increment(self):
        if not self.counting:
            self.val = self.min
            self.counting = True
        else:
            self.val += self.delta
            if self.val > self.max:
                self.counting = False

        return self.counting

