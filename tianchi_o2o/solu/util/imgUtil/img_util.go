package imgUtil

import (
	"image"
	"image/color"
	"image/draw"
	"image/jpeg"
	"os"
	"fmt"
)

var blue color.RGBA = color.RGBA{0, 0, 255, 255}

func Plot(width, height, xMax, yMax float64, xs, ys []float64) *image.RGBA {
	img := image.NewRGBA(image.Rect(0, 0, int(width), int(height)))
	draw.Draw(img, img.Bounds(), &image.Uniform{color.RGBA{255, 255, 255, 255}}, image.ZP, draw.Src)
	for i := 0; i < len(xs); i++ {
		px, py := int(width*xs[i]/xMax), int(height - height*ys[i]/yMax)
		plotPoint(img, px, py)
	}
	return img
}

func plotPoint(img *image.RGBA, px, py int) {
	img.Set(px, py, blue)
	img.Set(px+1, py, blue)
	img.Set(px, py+1, blue)
	img.Set(px+1, py+1, blue)
}

func SaveToJpegFile(img *image.RGBA, path string) {
	file, err := os.OpenFile(path, os.O_RDWR | os.O_CREATE | os.O_TRUNC, 0666)
	if err != nil {
		fmt.Printf("SaveToJpegFile error when create jpeg file, path: %s", path)
		os.Exit(-1)
	}
	err = jpeg.Encode(file, img, &jpeg.Options{100})
	if err != nil {
		fmt.Printf("SaveToJpegFile error when jpeg encode, %s", err.Error())
		os.Exit(-1)
	}
}