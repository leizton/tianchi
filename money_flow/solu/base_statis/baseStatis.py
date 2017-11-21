import sys

sys.path.append('..')

from utils import *
from load_data import *


def statis(allUserRecords):
    possibleDayNum = 600
    dayUserCount = [0.] * possibleDayNum
    dayPurchase = [0.] * possibleDayNum
    dayRedeem = [0.] * possibleDayNum
    dayTodayBalance = [0.] * possibleDayNum
    dayYesterdayBalance = [0.] * possibleDayNum
    dayDetail = [[0.] * 6] * possibleDayNum

    for _, userRecords in allUserRecords.items():
        for record in userRecords.records:
            day = record[0]
            dayUserCount[day] += 1
            dayPurchase[day] += loadUserBalance.UserRecord.totalPurchase(record)
            dayRedeem[day] += loadUserBalance.UserRecord.totalRedeem(record)
            dayTodayBalance[day] += record[1]
            dayYesterdayBalance[day] += record[2]
            dayDetail[day] = [dayDetail[day][i] + record[i + 3] for i in range(6)]

    dayUserCount = utils.trimTailZeroOfList(dayUserCount)
    dayPurchase = utils.trimTailZeroOfList(dayPurchase)
    dayRedeem = utils.trimTailZeroOfList(dayRedeem)
    dayTodayBalance = utils.trimTailZeroOfList(dayTodayBalance)
    dayYesterdayBalance = utils.trimTailZeroOfList(dayRedeem)
    dayDetail = dayDetail[0:len(dayUserCount)]
    return dayUserCount, dayPurchase, dayRedeem, dayTodayBalance, dayYesterdayBalance, dayDetail
