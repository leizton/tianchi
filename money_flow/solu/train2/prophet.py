import sys

sys.path.append('..')

import matplotlib.pyplot as plt
import pandas as pd
import datetime

from train1 import trainData

from fbprophet import Prophet


def plot(ser, start=0, show=True, nFig=1):
    plt.figure(nFig)
    plt.plot(range(start, start + len(ser)), ser, '-')
    if show:
        plt.show()


def formatTime(time_src):
    time = list(time_src)
    for i in xrange(len(time)):
        time[i] = datetime.datetime.strptime(time[i], '%Y%m%d').strftime('%Y-%m-%d')
    return time


def prophet(time, ser):
    df = pd.DataFrame({'ds': time, 'y': ser})
    df['cap'] = 7
    m = Prophet(growth='linear')
    m.fit(df)

    future = m.make_future_dataframe(periods=1)
    future['cap'] = 7
    forecast = m.predict(future)
    return forecast['yhat'][len(forecast['yhat']) - 1]


def iter(time, ser, iterNum=1):
    for i in xrange(iterNum):
        ser.append(prophet(time[i:len(time) - 1], ser[i:len(ser) - 1]))
        nextDate = datetime.datetime.strptime(time[-1], '%Y-%m-%d') + datetime.timedelta(days=1)
        time.append(nextDate.strftime('%Y-%m-%d'))
    return ser


if __name__ == '__main__':
    time_src, purchase_src, redeem_src = trainData.loadPurchaseAndRedeemData()
    srclen = len(time_src)

    time = formatTime(time_src)
    purchase = list(purchase_src)
    purchase = iter(time, purchase, 31)

    time = formatTime(time_src)
    redeem = list(redeem_src)
    redeem = iter(time, redeem, 31)

    for i in xrange(srclen, len(purchase)):
        print('201409%02d,%d,%d' % (i - srclen + 1, int(purchase[i]), int(redeem[i])))

    plot(purchase[0:srclen], show=False)
    plot(purchase[srclen:], start=srclen, show=False)
    plot(redeem[0:srclen], show=False, nFig=2)
    plot(redeem[srclen:], start=srclen, nFig=2)
