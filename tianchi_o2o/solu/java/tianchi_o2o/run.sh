#! /bin/bash

# 参数是mainClass
# com.whiker.tianchi.o2o.base.CouponRateConvert

mvn clean assembly:assembly

jars=`find target/tianchi_o2o-release/ -name "*.jar"`

jarArg=`echo $jars | sed "s/ /,/g"`

spark-submit --jars $jarArg --class $1 target/tianchi_o2o.jar
