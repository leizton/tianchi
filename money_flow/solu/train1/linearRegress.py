import trainData
import math
import numpy as np
from sklearn.linear_model import LinearRegression
import matplotlib.pyplot as plt

Offset = 2
Scale = 1e7


def plot(Y1, Y2, nFig=1):
    Y1 = Y1.flatten().tolist()
    Y2 = Y2.flatten().tolist()[0]
    pltx = range(0, len(Y1))
    plt.figure(nFig)
    plt.plot(pltx, Y1, '.-')
    plt.plot(pltx, Y2)
    plt.show()


def train(X, Y):
    reg = LinearRegression()
    reg.fit(X, Y)
    error = (reg.predict(X) - Y).flatten().tolist()[0]
    print('error:', math.sqrt(sum([v ** 2 for v in error]) / len(error)))
    print('score:', reg.score(X, Y))
    plot(reg.predict(X), Y)
    return reg


def predict(inp, reg):
    inp = inp.flatten().tolist()[0][-1:-8:-1]
    out = []
    for i in range(31):
        x = [np.double(((i + Offset) % 7) * Scale)] + [np.double(v) for v in inp]
        y = reg.predict(np.mat(x)).tolist()[0][0]
        out.append(y)
        inp.pop()
        inp.insert(0, y)
    return out


if __name__ == '__main__':
    purchaseX, purchaseY, redeemX, redeemY = trainData.buildTrainData(Offset, Scale)
    reg1 = train(purchaseX, purchaseY)
    reg2 = train(redeemX, redeemY)
    y1 = predict(purchaseY, reg1)
    y2 = predict(redeemY, reg2)

    for i in range(31):
        print('201409%02d,%d,%d' % (i + 1, int(y1[i]), int(y2[i])))
