WorkToRest = {
    '20140407': 1,
    '20140501': 1,
    '20140502': 1,
    '20140602': 1,
    '20140908': 1
}

RestToWork = {
    '20140504': 2
}


def isWorkToRest(date):
    return date in WorkToRest


def isRestToWork(date):
    return date in RestToWork

def flag(date):
    if isWorkToRest(date):
        return 1
    elif isRestToWork(date):
        return 2
    else:
        return 0
