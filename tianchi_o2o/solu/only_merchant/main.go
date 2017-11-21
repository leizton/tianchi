package main

import (
	"strings"
	"../util/convertUtil"
	"../util/fileUtil"
	"../util/timeUtil"
	"../util/predict"
	"../util/dirUtil"
)

var offlineTrainFilePath = dirUtil.DataDir + "/src_local/ccf_offline_stage1_train.csv"
var predictFilePath = dirUtil.DataDir + "/src_local/ccf_offline_stage1_test_revised.csv"
var predictResultFilePath = dirUtil.DataDir + "/only_merchant/ret.csv"

func main() {
	couponGetDateStart := timeUtil.GetTimeDay("20160201")

	merchantStatis := map[int][2]int{}
	statisProcessor := func (line string) int {
		values := strings.Split(line, ",")
		merchantId := convertUtil.StrToInt(values[1])
		couponId := convertUtil.StrToInt(values[2])
		couponGetDate := timeUtil.GetTimeDay(values[5])
		consumeDate := timeUtil.GetTimeDay(values[6])
		if merchantId <= 0 || couponId <= 0 || couponGetDate <= couponGetDateStart {
			return 0
		}
		if consumeDate <= 0 || consumeDate - couponGetDate > 14 {
			doStatis(merchantStatis, merchantId, false)
		} else {
			doStatis(merchantStatis, merchantId, true)
		}
		return 0
	}
	fileUtil.ProcessLine(offlineTrainFilePath, statisProcessor)

	doPredict := func(item predict.ToPredictItem) float64 {
		statis, ok := merchantStatis[item.MerchantId]
		if ok {
			return 1 - float64(statis[1]) / float64(statis[0] + statis[1])
		} else {
			return 0
		}
	}
	predictRet := predict.Predict(doPredict)
	predict.PredictRetOutput(predictRet, predictResultFilePath)
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
