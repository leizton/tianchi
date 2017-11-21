package predict

import (
	"strings"
	"../convertUtil"
	"../fileUtil"
	"../timeUtil"
	"../../util/dirUtil"
)

type ToPredictItem struct {
	UserId int
	MerchantId int
	CouponId int
	CouponRate string
	Distance int
	CouponGetDate int
}

func NewToPredictItem(userId int, merchantId int, couponId int, couponRate string, distance int, couponGetDate int) ToPredictItem {
	return ToPredictItem{userId, merchantId, couponId, couponRate, distance, couponGetDate}
}

func LoadToPredict() []ToPredictItem {
	toPredicts := make([]ToPredictItem, 0, 1000)
	processor := func (line string) int {
		values := strings.Split(line, ",")
		userId := convertUtil.StrToInt(values[0])
		merchantId := convertUtil.StrToInt(values[1])
		couponId := convertUtil.StrToInt(values[2])
		distance := convertUtil.StrToInt(values[4])
		couponGetDate := timeUtil.GetTimeDay(values[5])

		item := NewToPredictItem(userId, merchantId, couponId, values[3], distance, couponGetDate)
		toPredicts = append(toPredicts, item)
		return 0
	}
	fileUtil.ProcessLine(dirUtil.DataDir + "/src_local/ccf_offline_stage1_test_revised.csv",
						 processor)
	return toPredicts
}