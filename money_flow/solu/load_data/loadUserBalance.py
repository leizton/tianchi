import sys

sys.path.append('..')

from utils import *


class UserRecord:
    def __init__(self, user_id):
        self.userId = user_id
        self.records = []

    def addRecord(self,
                  day, todayBalance, yesterdayBalance,
                  purchaseZhifubao, purchaseBank, purchaseShare,
                  redeemZhifubao, redeemBank, redeemConsume):
        record = [day, todayBalance, yesterdayBalance,
                  purchaseZhifubao, purchaseBank, purchaseShare,
                  redeemZhifubao, redeemBank, redeemConsume]
        self.records.append(record)

    @staticmethod
    def totalPurchase(record):
        return sum(record[3:6])

    @staticmethod
    def totalRedeem(record):
        return sum(record[6:9])


def load():
    allUserRecords = {}
    f = open(pathUtil.dataDirPath() + '/src/user_balance_table.csv')
    f.readline()
    userId = 0
    currUserRecord = None
    for line in f:
        lst = line.split(',')
        for i in range(2, len(lst)):
            if len(lst[i]) == 0 or lst[i][0] < '0' or lst[i][0] > '9':
                lst[i] = 0.
            else:
                lst[i] = float(lst[i])

        id = int(lst[0])
        if id != userId or currUserRecord is None:
            if id in allUserRecords:
                currUserRecord = allUserRecords[id]
            else:
                currUserRecord = UserRecord(id)
                allUserRecords[id] = currUserRecord
        lst[1] = timeUtil.toDay(lst[1])
        if lst[1] <= 0:
            print('date error: ', line)
            continue
        currUserRecord.addRecord(lst[1], lst[2], lst[3],
                                 lst[6], lst[7], lst[13],
                                 lst[11], lst[12], lst[9])
    f.close()
    return allUserRecords
