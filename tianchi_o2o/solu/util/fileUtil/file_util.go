package fileUtil

import (
	"os"
	"fmt"
	"bufio"
	"io"
	"strings"
)

func OpenFileToRead(path string) *os.File {
	file, err := os.Open(path)
	if err != nil {
		fmt.Printf("openFileToRead error, %s\n", path)
		os.Exit(-1)
	}
	return file
}

func OpenFileToWrite(path string) *os.File {
	file, err := os.OpenFile(path, os.O_RDWR | os.O_CREATE | os.O_TRUNC, 0666)
	if err != nil {
		fmt.Printf("openFileToWrite error, %s\n", path)
		os.Exit(-1)
	}
	return file
}

func ProcessLine(path string, processor func(string) int) {
	file := OpenFileToRead(path)
	defer file.Close()
	onlineReader := bufio.NewReader(file)
	for {
		line, err := onlineReader.ReadString('\n')
		if err != nil && err != io.EOF {
			fmt.Printf("read line error, path: %s, err: %s\n", path, err.Error())
			return
		}
		line = strings.TrimSpace(line)
		if len(line) > 0 {
			ret := processor(line)
			if ret != 0 {
				fmt.Printf("processLine stop, ret: %d, path: %s, line: %s", ret, path, line)
				break
			}
		}
		if err != nil && err == io.EOF {
			break
		}
	}
}