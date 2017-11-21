# coding=utf-8
from utils import *
from load_data import *
from base_statis import *
import holiday


# 结果写入day_purchase_redeem.csv
def printStatis(fromDay, dayUserCount, dayPurchase, dayRedeem, dayTodayBalance, dayYesterdayBalance, dayDetail):
    for i in range(fromDay, len(dayUserCount)):
        date = timeUtil.toDateStr(i)
        s = date + '\t' + str((i - 1) % 7) + '\t' + str(holiday.flag(date))
        values = str(dayUserCount[i]) + '\t' + str(dayPurchase[i]) + '\t' + str(dayRedeem[i]) \
                 + '\t' + str(dayTodayBalance[i]) + '\t' + str(dayYesterdayBalance[i])
        for j in range(len(dayDetail[i])):
            values += '\t' + str(dayDetail[i][j])
        print(s + '\t' + values)


def meanOfPurchaseAndRedeem(fromDay, dayPurchase, dayRedeem):
    sumPurchase, sumRedeem = float(0), float(0)
    limit = min(len(dayPurchase), len(dayRedeem))
    for i in range(fromDay, limit):
        sumPurchase += dayPurchase[i]
        sumRedeem += dayRedeem[i]
    num = float(limit - fromDay)
    return sumPurchase / num, sumRedeem / num


def meanRet(fromDay, dayPurchase, dayRedeem):
    meanPurchase, meanRedeem = meanOfPurchaseAndRedeem(fromDay, dayPurchase, dayRedeem)
    for i in range(1, 32):
        print('201409%02d,%d,%d' % (i, int(meanPurchase), int(meanRedeem)))


if __name__ == '__main__':
    allUserRecords = loadUserBalance.load()
    dayUserCount, dayPurchase, dayRedeem, dayTodayBalance, dayYesterdayBalance, dayDetail\
        = baseStatis.statis(allUserRecords)
    printStatis(260, dayUserCount, dayPurchase, dayRedeem, dayTodayBalance, dayYesterdayBalance, dayDetail)
    meanRet(260, dayPurchase, dayRedeem)
