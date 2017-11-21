package main

import (
	"math"
	"fmt"
	"../util/merchant"
	"../util/predict"
	"../util/dirUtil"
)

const Month_Num = 6
const Month_Start = 1
const Max_Corr_Distance = 0.02

func main() {
	var probaMaps [Month_Num]merchant.ProbaMap
	probaMaps[1] = merchant.GetProbaOfMerchant("20160101", "20160201")
	probaMaps[2] = merchant.GetProbaOfMerchant("20160201", "20160301")
	probaMaps[3] = merchant.GetProbaOfMerchant("20160301", "20160401")
	probaMaps[4] = merchant.GetProbaOfMerchant("20160401", "20160501")
	probaMaps[5] = merchant.GetProbaOfMerchant("20160501", "20160601")

	totalProbaMaps := merchant.GetProbaOfMerchant("20160201", "20160601")

	postiveMerchants := map[int]float64{}
	for i := 1; i < (Month_Num - 1); i++ {
		pm := selectPositiveCorrMerchants(probaMaps, i, Month_Num)
		for merchant, proba := range pm {
			_, ok := postiveMerchants[merchant]
			if !ok {
				postiveMerchants[merchant] = proba
			}
		}
	}
	fmt.Printf("正向商户数: %d\n", len(postiveMerchants))

	postiveCnt, negativeCnt := 0, 0
	doPredict := func(item predict.ToPredictItem) float64 {
		postiveProba, ok := postiveMerchants[item.MerchantId]
		if ok {
			postiveCnt++
			return postiveProba
		} else {
			negativeProba, ok := totalProbaMaps[item.MerchantId]
			if ok {
				negativeCnt++
				return 1 - negativeProba
			} else {
				return 0
			}
		}
	}
	predictRet := predict.Predict(doPredict)
	predict.PredictRetOutput(predictRet, dirUtil.DataDir + "/only_merchant_v2/ret.csv")
	fmt.Printf("预测中，正向数: %d, 逆向数: %d\n", postiveCnt, negativeCnt)
}

func selectPositiveCorrMerchants(probaMaps [Month_Num]merchant.ProbaMap, index, indexEnd int) map[int]float64 {
	postiveMerchants := map[int]float64{}
	for merchant, _ := range probaMaps[index] {
		probas := getAllProbasOfMerchant(merchant, index, Month_Num, probaMaps)
		flag := true
		for i := index; i < indexEnd && flag; i++ {
			for j := i + 1; j < indexEnd; j++ {
				corrDistance := calcCorrDistance(probas[i], probas[j])
				if corrDistance > Max_Corr_Distance {
					flag = false
					break
				}
			}
		}
		if flag {
			postiveMerchants[merchant] = meanProbas(probas, index, indexEnd)
		}
	}
	fmt.Printf("第%d月的正向商户数: %d\n", index, len(postiveMerchants))
	return postiveMerchants
}

func getAllProbasOfMerchant(merchant, start, num int, probaMaps [Month_Num]merchant.ProbaMap) []float64 {
	probas := make([]float64, num, num)
	for i := start; i < num; i++ {
		proba, ok := probaMaps[i][merchant]
		if !ok {
			proba = 0
		}
		probas[i] = proba
	}
	return probas
}

func calcCorrDistance(proba1, proba2 float64) float64 {
	return math.Abs(proba1 - proba2) / math.Sqrt(2)
}

func meanProbas(probas []float64, start, num int) float64 {
	var sum float64 = 0
	var cnt int = 0
	for i := start; i < num; i++ {
		sum += probas[i]
	}
	return sum / float64(cnt)
}
