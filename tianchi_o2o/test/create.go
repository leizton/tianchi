package main

import(
	"strings"
	"fmt"
	"os"
	"bufio"
	"io"
	"../solu/util/dirUtil"
)

var rootDir = dirUtil.RootDir
var dataSrcDirPath = rootDir + "/data/src"
var dataSrcLocalDirPath = rootDir + "/data/src_local"

var trainInputPath = dataSrcDirPath + "/ccf_offline_stage1_train.csv"
var onlineTrainInputPath = dataSrcDirPath + "/ccf_online_stage1_train.csv"

var trainOutputPath = dataSrcLocalDirPath + "/ccf_offline_stage1_train.csv"
var testOutputPath = dataSrcLocalDirPath + "/ccf_offline_stage1_test_revised.csv"
var realOutputPath = dataSrcLocalDirPath + "/real.csv"
var onlineTrainOutputPath = dataSrcLocalDirPath + "/ccf_online_stage1_train.csv"

func main() {
	trainInputFile := openFileToRead(trainInputPath)
	defer trainInputFile.Close()

	trainOutputFile := openFileToWrite(trainOutputPath)
	defer trainOutputFile.Close()
	testOutputFile := openFileToWrite(testOutputPath)
	defer trainOutputFile.Close()
	realOutputFile := openFileToWrite(realOutputPath)
	defer trainOutputFile.Close()

	trainWriter := bufio.NewWriter(trainOutputFile)
	defer trainWriter.Flush()
	testWriter := bufio.NewWriter(testOutputFile)
	defer testWriter.Flush()
	realWriter := bufio.NewWriter(realOutputFile)
	defer realWriter.Flush()

	reader := bufio.NewReader(trainInputFile)
	for {
		line, err := reader.ReadString('\n')
		line = strings.TrimSpace(line)
		processLine(line, trainWriter, testWriter, realWriter)
		if err != nil {
			if err == io.EOF {
				break
			} else {
				fmt.Printf("read line error, %s\n", err.Error())
				return
			}
		}
	}

	onlineTrainInputFile := openFileToRead(onlineTrainInputPath)
	defer onlineTrainInputFile.Close()
	onlineTrainOutputFile := openFileToWrite(onlineTrainOutputPath)
	defer onlineTrainOutputFile.Close()
	onlineTrainWriter := bufio.NewWriter(onlineTrainOutputFile)
	defer onlineTrainWriter.Flush()
	onlineReader := bufio.NewReader(onlineTrainInputFile)
	for {
		line, err := onlineReader.ReadString('\n')
		line = strings.TrimSpace(line)
		trainProcessLine(line, onlineTrainWriter)
		if err != nil {
			if err == io.EOF {
				break
			} else {
				fmt.Printf("read line error, %s\n", err.Error())
				return
			}
		}
	}
}

func isAfterTestStartTime(date string) bool {
	return date != "null" && date > "20160600";
}

func openFileToRead(path string) *os.File {
	file, err := os.Open(path)
	if err != nil {
		fmt.Printf("openFileToRead error, %s\n", path)
		os.Exit(-1)
	}
	return file
}

func openFileToWrite(path string) *os.File {
	file, err := os.OpenFile(path, os.O_RDWR | os.O_CREATE | os.O_TRUNC, 0666)
	if err != nil {
		fmt.Printf("openFileToWrite error, %s\n", path)
		os.Exit(-1)
	}
	return file
}

func processLine(line string, trainWriter *bufio.Writer, testWriter *bufio.Writer, realWriter *bufio.Writer) {
	values := strings.Split(line, ",")
	if len(values) != 7 {
		return
	}
	couponDate := values[5]
	consumeDate := values[6]
	if couponDate == "null" && consumeDate == "null" {
		return
	}
	if isAfterTestStartTime(couponDate) {
		// 写入用于预测的testOutputPath
		testWriter.WriteString(strings.Join(values[0:6], ","))
		testWriter.WriteString("\n")
		// 写入实际结果realOutputPath
		realWriter.WriteString(line)
		realWriter.WriteString("\n")
	} else if couponDate == "null" && isAfterTestStartTime(consumeDate) {
		return
	} else {
		// 写入线下训练数据trainOutputPath
		trainWriter.WriteString(line)
		trainWriter.WriteString("\n")
	}
}

func trainProcessLine(line string, writer *bufio.Writer) {
	values := strings.Split(line, ",")
	if len(values) != 7 || isAfterTestStartTime(values[5]) || isAfterTestStartTime(values[6]) {
		return
	}
	writer.WriteString(line)
	writer.WriteString("\n")
}