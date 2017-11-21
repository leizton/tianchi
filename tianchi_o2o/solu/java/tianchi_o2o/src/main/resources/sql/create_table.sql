USE tianchi_o2o;

DROP TABLE IF EXISTS offline_train;

CREATE TABLE offline_train (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  user_id INT NOT NULL COMMENT '用户ID',
  merchant_id INT NOT NULL COMMENT '商户ID',
  coupon_id INT NOT NULL COMMENT '优惠券ID',
  coupon_rate FLOAT(2, 2) NOT NULL COMMENT '优惠率',
  distance INT NOT NULL COMMENT '距离',
  coupon_get_date INT NOT NULL COMMENT '领取优惠券日期',
  consume_date INT NOT NULL COMMENT '消费日期',
  PRIMARY KEY (id)
) ENGINE=myisam DEFAULT CHARSET utf8 COMMENT '线下记录';

CREATE TABLE forecast (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  user_id INT NOT NULL COMMENT '用户ID',
  merchant_id INT NOT NULL COMMENT '商户ID',
  coupon_id INT NOT NULL COMMENT '优惠券ID',
  coupon_rate FLOAT(2, 2) NOT NULL COMMENT '优惠率',
  distance INT NOT NULL COMMENT '距离',
  coupon_get_date INT NOT NULL COMMENT '领取优惠券日期',
  PRIMARY KEY (id)
) ENGINE=myisam DEFAULT CHARSET utf8 COMMENT '预测样本';

CREATE TABLE bayes_train (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  user_id INT NOT NULL COMMENT '用户ID',
  merchant_id INT NOT NULL COMMENT '商户ID',
  coupon_id INT NOT NULL COMMENT '优惠券ID',
  discount INT NOT NULL COMMENT '优惠率',
  distance INT NOT NULL COMMENT '距离',
  action INT NOT NULL COMMENT '用户行为结果',
  coupon_get_date INT NOT NULL COMMENT '领取优惠券日期',
  consume_date INT NOT NULL COMMENT '消费日期',
  PRIMARY KEY (id)
) ENGINE=myisam DEFAULT CHARSET utf8 COMMENT 'bayes训练数据';

CREATE TABLE user_action_mean (
  user_id INT NOT NULL COMMENT '用户ID',
  action_mean INT NOT NULL COMMENT 'action均值',
  PRIMARY KEY (user_id)
) ENGINE=myisam DEFAULT CHARSET utf8 COMMENT '用户action_mean表';

CREATE TABLE merchant_action_mean (
  merchant_id INT NOT NULL COMMENT '商户ID',
  action_mean INT NOT NULL COMMENT 'action均值',
  PRIMARY KEY (merchant_id)
) ENGINE=myisam DEFAULT CHARSET utf8 COMMENT '商户action_mean表';

CREATE TABLE bayes_train_v (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  user_id INT NOT NULL COMMENT '用户ID',
  merchant_id INT NOT NULL COMMENT '商户ID',
  coupon_id INT NOT NULL COMMENT '优惠券ID',
  discount INT NOT NULL COMMENT '优惠率',
  distance INT NOT NULL COMMENT '距离',
  action INT NOT NULL COMMENT '用户行为结果',
  coupon_get_date INT NOT NULL COMMENT '领取优惠券日期',
  consume_date INT NOT NULL COMMENT '消费日期',
  PRIMARY KEY (id)
) ENGINE=myisam DEFAULT CHARSET utf8 COMMENT 'bayes训练数据';

CREATE TABLE bayes_train_norm (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  user_id INT NOT NULL COMMENT '用户ID',
  merchant_id INT NOT NULL COMMENT '商户ID',
  coupon_id INT NOT NULL COMMENT '优惠券ID',
  user_grade INT NOT NULL DEFAULT 0 COMMENT '用户等级',
  merchant_grade INT NOT NULL DEFAULT 0 COMMENT '商户等级',
  discount_grade INT NOT NULL COMMENT '优惠率类别',
  distance_grade INT NOT NULL COMMENT '距离类别',
  action INT NOT NULL COMMENT '用户行为结果',
  PRIMARY KEY (id)
) ENGINE=myisam DEFAULT CHARSET utf8 COMMENT 'bayes训练类别';

CREATE TABLE bayes_statis (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  flag INT NOT NULL DEFAULT 0 COMMENT '0不是条件概率, 1是条件概率, 2是groupBy用户行为结果的count(*)',
  action INT NOT NULL DEFAULT 0 COMMENT '用户行为结果',
  var_code INT NOT NULL DEFAULT 0 COMMENT '变量编号',
  var_value INT NOT NULL DEFAULT 0 DEFAULT 0 COMMENT '变量值',
  var_num INT NOT NULL DEFAULT 0 COMMENT '变量数目',
  total_num INT NOT NULL DEFAULT 0 COMMENT '总数',
  PRIMARY KEY (id)
) ENGINE=myisam DEFAULT CHARSET utf8 COMMENT 'bayes统计表';

CREATE TABLE bayes_forecast (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  user_id INT NOT NULL COMMENT '用户ID',
  merchant_id INT NOT NULL COMMENT '商户ID',
  coupon_id INT NOT NULL COMMENT '优惠券ID',
  discount INT NOT NULL COMMENT '优惠率',
  distance INT NOT NULL COMMENT '距离',
  coupon_get_date INT NOT NULL COMMENT '领取优惠券日期',
  user_grade INT NOT NULL DEFAULT 0 COMMENT '用户等级',
  merchant_grade INT NOT NULL DEFAULT 0 COMMENT '商户等级',
  discount_grade INT NOT NULL COMMENT '优惠率类别',
  distance_grade INT NOT NULL COMMENT '距离类别',
  probability FLOAT(4, 4) NOT NULL COMMENT '概率',
  PRIMARY KEY (id)
) ENGINE=myisam DEFAULT CHARSET utf8 COMMENT 'bayes预测样本';