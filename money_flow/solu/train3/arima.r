srcData = read.table('day_purchase_redeem.csv', header = F)

# purchase <- xts::xts(srcData$V5, seq(as.POSIXct("2014-03-17"), len=length(srcData$V5), by="day"))
purchase <- ts(srcData$V5, start = c(2014), frequency = 365)
purchase.dif <- diff(diff(purchase), 7)
acf(purchase.dif)   # 截尾
pacf(purchase.dif)  # 拖尾
# 加法模型: ARIMA(1, (1,7), 1)
purchase.fit <- arima(purchase, order=c(1,1,1), seasonal=list(order=c(0,1,0), period=7))
print(Box.test(purchase.fit$residual, lag=6))
print(Box.test(purchase.fit$residual, lag=12))
    p-value=1.418e-07，过小，残差不是白噪声
# 乘积模型: ARIMA(1, 1, 1) x (1,1,1)7
purchase.fit <- arima(purchase, order=c(1,1,1), seasonal=list(order=c(1,1,1), period=7))
tsdiag(purchase.fit)
    残差acf没有自相关性，p值大于0.5
purchase.fc <- forecast::forecast(purchase.fit, h=31)
plot(purchase.fc)
print(purchase.fc$mean)

redeem <- ts(srcData$V6, start = c(2014), frequency = 365)
redeem.fit <- arima(redeem, order=c(1,1,1), seasonal=list(order=c(1,1,1), period=7))
redeem.fc <- forecast::forecast(redeem.fit, h=31)

for(i in 1:31) print(sprintf('201409%02d,%.0f,%.0f', i, purchase.fc$mean[i], redeem.fc$mean[i]))