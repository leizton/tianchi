def trimTailZeroOfList(lst):
    limit = len(lst) + 1
    newlen = 0
    for i in range(-1, -limit, -1):
        if lst[i] != 0:
            newlen = limit + i
            break
    return lst[0:newlen]
