import sys

sys.path.append('..')

from utils import *
import numpy as np
import matplotlib.pyplot as plt


def loadData():
    data = []
    f = open(pathUtil.dataDirPath() + '/src/day_purchase_redeem.csv')
    for line in f:
        d = line.split('\t')
        if len(d) == 14:
            data.append([d[0]] + [float(v) for v in d[1:len(d)]])
    f.close()
    return data


def plotData(inds, data=None):
    if data is None:
        data = loadData()
    for ind in inds:
        y = [d[ind] for d in data]
        plt.plot(range(0, len(y)), y, '.-')
    plt.show()


def loadPurchaseAndRedeemData():
    time, dayPurchase, dayRedeem = [], [], []
    for data in loadData():
        time.append(data[0])
        dayPurchase.append(data[4])
        dayRedeem.append(data[5])
    return time, dayPurchase, dayRedeem


def buildTrainData(offset, scale):
    _, dayPurchase, dayRedeem = loadPurchaseAndRedeemData()
    purchaseXtrain, purchaseYtrain = [], []
    redeemXtrain, redeemYtrain = [], []
    for i in range(7, len(dayPurchase)):
        lst1, lst2 = [0.] * 8, [0.] * 8
        lst1[0] = lst2[0] = float(((i + offset) % 7) * scale)
        for j in range(1, 8):
            lst1[j] = dayPurchase[i - j]
            lst2[j] = dayRedeem[i - j]
        purchaseXtrain.append(lst1)
        redeemXtrain.append(lst2)
        purchaseYtrain.append(dayPurchase[i])
        redeemYtrain.append(dayRedeem[i])
    return np.mat(purchaseXtrain), np.mat(purchaseYtrain).transpose(), \
           np.mat(redeemXtrain), np.mat(redeemYtrain).transpose()
