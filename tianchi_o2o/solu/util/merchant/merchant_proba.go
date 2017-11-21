package merchant

import (
	"strings"
	"../../util/convertUtil"
	"../../util/fileUtil"
	"../../util/timeUtil"
	"../../util/dirUtil"
)

type ProbaMap map[int]float64

func GetProbaOfMerchant(beginDateStr, endDateStr string) ProbaMap {
	beginDate := timeUtil.GetTimeDay(beginDateStr)
	endDate := timeUtil.GetTimeDay(endDateStr)

	merchantStatis := map[int][2]int{}
	processor := func (line string) int {
		values := strings.Split(line, ",")
		merchantId := convertUtil.StrToInt(values[1])
		couponId := convertUtil.StrToInt(values[2])
		couponGetDate := timeUtil.GetTimeDay(values[5])
		consumeDate := timeUtil.GetTimeDay(values[6])
		if merchantId <= 0 || couponId <= 0 || couponGetDate < beginDate || couponGetDate >= endDate {
			return 0
		}
		if consumeDate <= 0 || consumeDate - couponGetDate > 14 {
			doStatis(merchantStatis, merchantId, false)
		} else {
			doStatis(merchantStatis, merchantId, true)
		}
		return 0
	}
	fileUtil.ProcessLine(dirUtil.DataDir + "/src_local/ccf_offline_stage1_train.csv",
						 processor)

	merchantProba := ProbaMap{}
	for merchantId, statis := range merchantStatis {
		merchantProba[merchantId] = float64(statis[1]) / float64(statis[0] + statis[1])
	}
	return merchantProba
}

func doStatis(merchantStatis map[int][2]int, merchantId int, isUse bool) {
	statis, ok := merchantStatis[merchantId]
	if !ok {
		statis = [2]int{0, 0}
	}
	if isUse {
		statis[0]++
	} else {
		statis[1]++
	}
	merchantStatis[merchantId] = statis
}