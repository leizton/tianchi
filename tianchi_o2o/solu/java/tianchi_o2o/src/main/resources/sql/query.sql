-- 查询用户有多少条记录, 列出记录条数最多的前10个用户
select user_id, count(id) as cnt from offline_train group by user_id order by cnt desc limit 10;

-- 过滤bayes_train的数据
insert into bayes_train_v(user_id,merchant_id,coupon_id,discount,distance,action,coupon_get_date,consume_date)
  select user_id,merchant_id,coupon_id,discount,distance,action,coupon_get_date,consume_date from bayes_train where distance>=0 and discount>0;
