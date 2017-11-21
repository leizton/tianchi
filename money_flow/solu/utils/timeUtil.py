import datetime


StartDate = datetime.datetime(2013, 6, 30)


def toDay(date):
    if len(date) != 8:
        return -1
    year = int(date[0:4])
    month = int(date[4:6])
    day = int(date[6:8])
    diff = (datetime.datetime(year, month, day) - StartDate).days
    if diff < 0:
        return -1
    return diff


def toDate(day):
    return StartDate + datetime.timedelta(days=day)


def toDateStr(day):
    date = toDate(day)
    return '%04d%02d%02d' % (date.year,date.month,date.day)
