#!/bin/bash

mysql -uroot -pwh1991 -Dtianchi_o2o -e"select distance,count(id) from bayes_train_v where action>0 group by distance" > distanceHist

mysql -uroot -pwh1991 -Dtianchi_o2o -e"select action_mean,count(user_id) from user_grade where action>0 group by action_mean" > actionMeanHist