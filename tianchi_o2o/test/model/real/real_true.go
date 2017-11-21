package real

import (
	"sort"
)

type RealTrueElem struct {
	UserId int
	CouponId int
	CouponGetDate int
}

func (self RealTrueElem) Equal(e RealTrueElem) bool {
	return self.UserId == e.UserId && self.CouponId == e.CouponId && self.CouponGetDate == e.CouponGetDate
}

func (self RealTrueElem) Less(e RealTrueElem) bool {
	if self.UserId == e.UserId {
		if self.CouponId == e.CouponId {
			return self.CouponGetDate < e.CouponGetDate
		}
		return self.CouponId < e.CouponId
	}
	return self.UserId < e.UserId
}

type RealTrueArray []RealTrueElem

func (arr RealTrueArray) Len() int {
    return len(arr)
}

func (arr RealTrueArray) Less(i, j int) bool {
	return arr[i].Less(arr[j])
}

func (arr RealTrueArray) Swap(i, j int) {
    arr[i].UserId, arr[j].UserId = arr[j].UserId, arr[i].UserId
    arr[i].CouponId, arr[j].CouponId = arr[j].CouponId, arr[i].CouponId
    arr[i].CouponGetDate, arr[j].CouponGetDate = arr[j].CouponGetDate, arr[i].CouponGetDate
}

func (arr RealTrueArray) Sort() {
	sort.Sort(arr)
}

/**
	对于
	sort.Search(sortedArr.Len(),
				func (i int) bool { return !sortedArr[i].Less(e) })
	当e不存在时不是返回-1
	本函数当e不存在时返回-1
 */
func (sortedArr RealTrueArray) Search(e RealTrueElem) int {
	low, high := 0, sortedArr.Len()
	mid := (low + high) >> 1

	for low < high && !sortedArr[mid].Equal(e) {
		if sortedArr[mid].Less(e) {
			low = mid + 1
		} else {
			high = mid - 1
		}
		mid = (low + high) >> 1
	}
	if mid >= 0 && mid < sortedArr.Len() && sortedArr[mid].Equal(e) {
		return mid
	}
	return -1
}