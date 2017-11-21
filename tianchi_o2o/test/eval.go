package main

import (
	"fmt"
	"os"
	"io"
	"io/ioutil"
	"bufio"
	"time"
	"strconv"
	"strings"
	"./model/predict"
	"../solu/util/dirUtil"
	"../solu/util/imgUtil"
)

var rootDir = dirUtil.RootDir
var dataRootDir = rootDir + "/data"

var realFilePath = rootDir + "/data/src_local/real.csv"

const SampleNum = 100  // 100个采样点

func openFileToRead(path string) *os.File {
	file, err := os.Open(path)
	if err != nil {
		fmt.Printf("openFileToRead error, %s\n", path)
		os.Exit(-1)
	}
	return file
}

var startTimer, _ = time.Parse("20060102", "20151231")
var startTime = startTimer.Unix()
var secondNumOfOneDay int64 = 24 * 3600
func getTimeDay(s string) int {
	timer, _ := time.Parse("20060102", s)
	ret := (timer.Unix() - startTime) / secondNumOfOneDay
	if ret <= 0 {
		return -1
	}
	return int(ret)
}

func main() {
	if len(os.Args) != 2 {
		fmt.Printf("input args error\n")
		os.Exit(-1)
	}
	inputArg := os.Args[1]
	var retFileName string
	if strings.LastIndex(inputArg, ".csv") == len(inputArg) - len(".csv") {
		retFileName = dataRootDir + "/" + inputArg
	} else {
		retFileName = dataRootDir + "/" + getRetFilePathFromDir(dataRootDir + "/" + inputArg)
	}
	fmt.Printf("ret file: %s\n", retFileName)

	var predictTrueNums, predictFalseNums [SampleNum]int

	realTrues, realTrueNum, realFalseNum := getReal()
	fmt.Printf("realTrueNum: %d, realFalseNum: %d\n", realTrueNum, realFalseNum)

	retFile := openFileToRead(retFileName)
	defer retFile.Close()
	reader := bufio.NewReader(retFile)
	var userId, couponId int64
	var probability float64
	customPredicts := predict.EmptySet()
	for {
		line, err := reader.ReadString('\n')
		if err != nil {
			if err == io.EOF {
				break
			} else {
				fmt.Printf("read line error, %s\n", err.Error())
				os.Exit(-1)
			}
		}
		line = strings.TrimSpace(line)
		values := strings.Split(line, ",")
		// userId  couponId  couponGetDate  probability
		if len(values) != 4 {
			continue
		}
		userId, err = strconv.ParseInt(values[0], 10, 32)
		if err != nil {
			continue
		}
		couponId, err = strconv.ParseInt(values[1], 10, 32)
		if err != nil {
			continue
		}
		couponGetDate := getTimeDay(values[2])
		if couponGetDate <= 0 {
			continue
		}
		probability, err = strconv.ParseFloat(values[3], 64)
		if err != nil || probability < 0 || probability > 1 {
			continue
		}
		site := int(probability * SampleNum)
		if site < 0 {
			site = 0
		}
		if site >= SampleNum {
			site = SampleNum - 1
		}
		item := predict.New(int(userId), int(couponId), couponGetDate)
		if customPredicts.Contains(item) {
			continue
		}
		customPredicts.Add(item)
		if realTrues.Contains(item) {
			predictTrueNums[site]++
		} else {
			predictFalseNums[site]++
		}
	}

	auc := calcAuc(realTrueNum, realFalseNum, predictTrueNums, predictFalseNums)
	fmt.Printf("auc: %.4f\n", auc)
}

func getRetFilePathFromDir(dirPath string) string {
	dir, err := ioutil.ReadDir(dirPath)
	if err != nil {
		fmt.Printf("ReadDir error, %s, %s\n", dirPath, err.Error())
		os.Exit(-1)
	}

	retFileName := ""
	for _, file := range dir {
		if !file.IsDir() {
			continue
		}
		name := file.Name()
		if name > retFileName {
			retFileName = name
		}
	}
	if len(retFileName) == 0 {
		fmt.Printf("can't find ret file\n")
		os.Exit(-1)
	}
	return retFileName
}

func getReal() (predict.PredictSet, int, int) {
	realFile := openFileToRead(realFilePath)
	defer realFile.Close()
	reader := bufio.NewReader(realFile)
	realTrues := predict.EmptySet()
	totalItems := predict.EmptySet()
	for {
		line, err := reader.ReadString('\n')
		if err != nil {
			if err == io.EOF {
				break
			} else {
				fmt.Printf("read line error, %s\n", err.Error())
				os.Exit(-1)
			}
		}
		line = strings.TrimSpace(line)
		values := strings.Split(line, ",")
		// userId  merchantId  couponId  couponRate  distance  couponGetDate  consumeDate
		userId, _ := strconv.ParseInt(values[0], 10, 32)
		couponId, _ := strconv.ParseInt(values[2], 10, 32)
		couponGetDate, consumeDate := getTimeDay(values[5]), getTimeDay(values[6])
		item := predict.New(int(userId), int(couponId), couponGetDate)
		if totalItems.Contains(item) {
			continue
		}
		totalItems.Add(item)
		if couponGetDate <= 0 || consumeDate <= 0 || (consumeDate - couponGetDate) > 14 {
			continue
		}
		realTrues.Add(item)
	}
	return realTrues, realTrues.Size(), totalItems.Size() - realTrues.Size()
}

func calcAuc(realTrueNum int, realFalseNum int, predictTrueNums [SampleNum]int, predictFalseNums [SampleNum]int) float64 {
	for i := SampleNum - 2; i >= 0; i-- {
		predictTrueNums[i] += predictTrueNums[i+1]
		predictFalseNums[i] += predictFalseNums[i+1]
	}
	realTrueNum, realFalseNum = predictTrueNums[0], predictFalseNums[0]
	fmt.Printf("submit num, True: %d, False: %d\n", predictTrueNums[0], predictFalseNums[0])
	fmt.Printf("real num, True: %d, False: %d\n", realTrueNum, realFalseNum)

	var area int64 = 0
	var x1, y1 int64
	xs := make([]float64, 0, 0)
	ys := make([]float64, 0, 0)
	for i := SampleNum - 1; i >= 0; i-- {
		area_t := calcArea(x1, y1, int64(predictFalseNums[i]), int64(predictTrueNums[i]))
		area += area_t
		x1, y1 = int64(predictFalseNums[i]), int64(predictTrueNums[i])
		xs = append(xs, float64(x1) / float64(realFalseNum))
		ys = append(ys, float64(y1) / float64(realTrueNum))
	}

	img := imgUtil.Plot(500, 500, 1, 1, xs, ys)
	imgUtil.SaveToJpegFile(img, dirUtil.DataDir + "/plot/roc.jpg")
	return float64(area)/float64(realTrueNum)/float64(realFalseNum)
}

func calcArea(x1, y1, x2, y2 int64) int64 {
	dx := x1 - x2
	if dx < 0 {
		dx = -dx
	}
	return ((y1 + y2) * dx) >> 1
}