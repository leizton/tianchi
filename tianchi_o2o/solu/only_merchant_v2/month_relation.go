package main

import (
	"../util/merchant"
	"../util/imgUtil"
	"../util/dirUtil"
)

const Month_Num = 6

func main() {
	var probaMaps [Month_Num]merchant.ProbaMap
	probaMaps[1] = merchant.GetProbaOfMerchant("20160101", "20160201")
	probaMaps[2] = merchant.GetProbaOfMerchant("20160201", "20160301")
	probaMaps[3] = merchant.GetProbaOfMerchant("20160301", "20160401")
	probaMaps[4] = merchant.GetProbaOfMerchant("20160401", "20160501")
	probaMaps[5] = merchant.GetProbaOfMerchant("20160501", "20160601")
	printSize(probaMaps)

	monthRelation(probaMaps[1], probaMaps[5], dirUtil.DataDir + "/plot/1-5.jpg")
}

func printSize(probaMaps [Month_Num]merchant.ProbaMap) {
	print("merchant num: ")
	print(len(probaMaps[1]))
	for i := 2; i < 6; i++ {
		print(", ")
		print(len(probaMaps[i]))
	}
	println("")
}

func monthRelation(probaMap1, probaMap2 merchant.ProbaMap, path string) {
	xs, ys := []float64{}, []float64{}
	for merchant, proba1 := range probaMap1 {
		proba2, ok := probaMap2[merchant]
		if ok {
			xs = append(xs, proba1)
			ys = append(ys, proba2)
		}
	}
	img := imgUtil.Plot(1000, 1000, 1, 1, xs, ys)
	imgUtil.SaveToJpegFile(img, path)
}