# 原始数据
1. 线下消费  
  userId  merchantId  couponId  couponRate  distance  couponGetDate  consumeDate
2. 线上行为表  
	userId  merchantId  action  couponId  couponRate  couponGetDate  consumeDate
3. 预测样本表  
	userId  merchantId  couponId  couponRate  distance  couponGetDate
4. 预测结果表  
	userId  couponId  couponGetDate probability
5. distance 用户离线下商户的距离  
  [0, 10], 0小于500米, 10大于5000米
6. couponRate 优惠券的优惠率  
  0.0~1.0  折扣  
  100:30   满100减30

# solution_v1 bayes
### 1. 统一优惠率  
A:a用 (1+d)*(A-a)/A 转成折扣, d>=0  
> 表: offline_train  
  user_id  merchant_id  coupon_id  coupon_rate  distance  coupon_get_date  consume_date  
> 预测样本表: forecast  
  user_id  merchant_id  coupon_id  coupon_rate  distance  coupon_get_date

### 2. CouponMerge，优惠券和优惠率合并成变量discount  
无券看成优惠率是0  
优惠率离散化, [0.95, 1)=>1, [0.90, 0.95)=>2, 以此类推, (0, 0.05)=>20  
优惠券id和商户id可以用于协同过滤

### 3. 用户行为结果变量action  
优惠率和action的对应

| discount | action |   action含义          |
|:--------:|:------:|:---------------------:|
|    >0    |    4   |  用券在5天内消费        |
|    >0    |    2   |  用券在6~15天内消费     |
|    >0    |    1   |  用券在15天外消费       |
|    =0    |    0   |  无券,普通消费          |
|    >0    |   -1   |  有券,且往后15天内无消费 |
|    >0    |   -2   |  有券,消费时不用这张券   |
|    =0    |   -4   |  无券,无消费           |

> 表: bayes_train  
  user_id  merchant_id  coupon_id  discount  distance  action  coupon_get_date  consume_date

### 4. 表bayes_train 变成 表bayes_train_v
  insert into bayes_train_v select * from bayes_train where distance>=0 and discount>0;

### 5. distance的二值化  
1. distance在action>0和<0时的直方图  
   * hist_1  
     select distance,count(id) from bayes_train_v where action>0 group by distance;  
   * hist_2  
     select distance,count(id) from bayes_train_v where action<0 group by distance;

2. hist_1 和 hist_2 的比值

| distance | hist_1 / hist_2 |
|:--------:|:---------------:|
|     0    |      0.155      |
|     1    |      0.064      |
|     2    |      0.045      |
|     3    |      0.035      |
|     4    |      0.033      |
|     5    |      0.028      |
|     6    |      0.025      |
|     7    |      0.022      |
|     8    |      0.024      |
|     9    |      0.024      |
|    10    |      0.015      |

| distance_grade | threshold=2                |
|:--------------:|:--------------------------:|
|       0        | 0<=distance<2              |
|       1        | distance>=2 or distance=-1 |

### 6. discount的化简
1. 在action>0和<0时的直方图  
   hist_1: select discount, count(id) from bayes_train where action>0 group by discount;  
   hist_2: select discount, count(id) from bayes_train where action<0 group by discount;

2. hist_1 和 hist_2 的比值  
   两条sql语句结果做除法  
   discount取(1 5 10)的比值较高, 其余的转化率很低

| discount | hist_1 / hist_2 |
|:--------:|:---------------:|
|     1    |      0.16       |
|     2    |      0.04       |
|     3    |      0.01       |
|     4    |      0.01       |
|     5    |      0.15       |
|     6    |      0.03       |
|     7    |      0.03       |
|     8    |      0.06       |
|    10    |      0.15       |

| discount_grade |     discount    |
|:--------------:|:---------------:|
|        0       |        1        |
|        1       |      2 3 4      |
|        2       |        5        |
|        3       |     6 7 8 9     |
|        4       |    10 11 ...    |

### 7. 通过行为对用户和商户评级
> 表: user_action_mean  
  user_id  action_mean  
> 表: merchant_action_mean  
  merchant_id  action_mean

* 过滤action=0  
  user_action_mean = bayes_train.groupBy(user_id).map( (int) ( sum(action)/num * 100 ) )
* 按action>0和<0统计action_mean的直方图
* 用kmeans聚成6类(action的取值总类数)  
  初始点: -200 -100 0 100 200 400  
* 不在表中的user和merchant的grade取值2(action_mean=0)

### 8. 用于bayes分类的数据
> 表: bayes_train_norm  
  user_id  merchant_id  user_grade  merchant_grade  discount_grade  distance_grade  

### 9. 统计
> 表: bayes_statis  
  flag  action  var_code  var_value  var_num  total_num

### 10. 流程

| table_name           | comment                          |
|:--------------------:|:--------------------------------:|
| offline_train        | 统一优惠率, 满减转double            |
| bayes_train          | 计算action                        |
| bayes_train_v        | 过滤distance>=0 and discount>0    |
| user_action_mean     | bayes_train_v里每个用户action的均值 |
| merchant_action_mean | bayes_train_v里每个商户action的均值 |
| cluster              | 聚类计算user_grade/merchant_grade |
| bayes_train_norm     | 补充bayes_train_v的4个grade变量取值 |
| bayes_statis         | 对bayes_train_norm做统计          |
