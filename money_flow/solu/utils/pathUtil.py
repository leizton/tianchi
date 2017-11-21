import os

ProjectName = 'money_flow'


def dataDirPath():
    pwd = os.getcwd()
    ind = pwd.index(ProjectName)
    return pwd[0:ind] + ProjectName + '/data'
