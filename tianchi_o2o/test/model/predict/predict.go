package predict

type PredictItem struct {
	UserId int
	CouponId int
	CouponGetDate int
}

type PredictSet map[PredictItem]int

func New(userId, couponId, couponGetDate int) PredictItem {
	return PredictItem{userId, couponId, couponGetDate}
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