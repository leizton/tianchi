package timeUtil

import (
	"time"
)

var startTimer, _ = time.Parse("20060102", "20151231")
var startTime = startTimer.Unix()

var secondNumOfOneDay int64 = 24 * 3600

func GetTimeDay(s string) int {
	timer, _ := time.Parse("20060102", s)
	ret := (timer.Unix() - startTime) / secondNumOfOneDay
	if ret <= 0 {
		return -1
	}
	return int(ret)
}

func DayNumToString(day int) string {
	second := startTime + int64(day) * secondNumOfOneDay
	timer := time.Unix(second, 0)
	return timer.Format("20060102")
}