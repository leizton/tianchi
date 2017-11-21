package predict

import (
	"bufio"
	"fmt"
	"../fileUtil"
	"../timeUtil"
)

type PredictItem struct {
	UserId int
	CouponId int
	CouponGetDate int
}

type PredictSet map[PredictItem]int
type PredictRet map[PredictItem]float64

func BuildPredictItem(item ToPredictItem) PredictItem {
	return PredictItem{item.UserId, item.CouponId, item.CouponGetDate}
}

func EmptySet() PredictSet {
	return PredictSet{}
}

func (set PredictSet) Add(item PredictItem) int {
	num, ok := set[item]
	if !ok {
		num = 1
	} else {
		num++
	}
	set[item] = num
	return num
}

func (set PredictSet) Contains(item PredictItem) bool {
	_, exist := set[item]
	return exist
}

func (set PredictSet) Size() int {
	return len(set)
}

func Predict(doPredict func(ToPredictItem) float64) PredictRet {
	toPredictItems := LoadToPredict()
	predictRet := PredictRet{}
	for _, item := range toPredictItems {
		predictItem := BuildPredictItem(item)
		probability := doPredict(item)
		if probability >= 0 && probability <= 1 {
			predictRet[predictItem] = probability
		}
	}
	return predictRet
}

func PredictRetOutput(predictRet PredictRet, predictResultFilePath string) {
	predictResultFile := fileUtil.OpenFileToWrite(predictResultFilePath)
	defer predictResultFile.Close()
	predictResultWriter := bufio.NewWriter(predictResultFile)
	defer predictResultWriter.Flush()

	for item, probability := range predictRet {
		fmt.Fprintf(predictResultWriter, "%d,%d,%s,%.4f\n",
			item.UserId, item.CouponId, timeUtil.DayNumToString(item.CouponGetDate), probability)
	}
}