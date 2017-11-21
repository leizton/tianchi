package convertUtil

import (
	"strconv"
)

func StrToInt(s string) int {
	id, err := strconv.ParseInt(s, 10, 32)
	if err != nil {
		return -1
	}
	return int(id)
}