/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2013-2015 Daqri, LLC.
 *  Author(s): Chris Broaddus
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 *  Copyright (C)2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid;

import jp.nyatla.nyartoolkit.core.kpm.pyramid.GaussianScaleSpacePyramid;

public class DoGPyramid {
	public DoGPyramid(int i_width, int i_height, int i_num_of_octaves, int i_num_scales_per_octaves)
	{
		this.mNumOctaves = i_num_of_octaves;
		this.mNumScalesPerOctave = i_num_scales_per_octaves;

		// Allocate DoG images 同一サイズのDoG画像ピラミッドを作る
		this.mImages = new LaplacianImage[this.mNumOctaves * this.mNumScalesPerOctave];
		for (int i = 0; i < this.mNumOctaves; i++) {
			for (int j = 0; j < this.mNumScalesPerOctave; j++) {
				this.mImages[i * this.mNumScalesPerOctave + j] = new LaplacianImage(i_width >> i, i_height >> i);// 多分あってるんじゃないか的な
			}
		}
	}
	/**
	 * Compute the Difference-of-Gaussian from a Gaussian Pyramid.
	 */
	public void compute(GaussianScaleSpacePyramid pyramid) {
		for (int i = 0; i < this.mNumOctaves; i++) {
			for (int j = 0; j < this.mNumScalesPerOctave; j++) {
				this.mImages[i * mNumScalesPerOctave + j].difference_image_binomial(pyramid.get(i, j), pyramid.get(i, j + 1));
			}
		}
	}


	/**
	 * Get a Laplacian image at an index.
	 */
	public LaplacianImage get(int index) {
		return mImages[index];
	}

	/**
	 * Get the number of octaves and scales.
	 */
	public int numOctaves() {
		return this.mNumOctaves;
	}

	public int numScalePerOctave() {
		return this.mNumScalesPerOctave;
	}

	public int size() {
		return this.mImages.length;
	}

	/**
	 * Get the octave from the Laplacian image index.
	 */
	public int octaveFromIndex(int index) {
		return (int)Math.floor((Math.log((mImages[0].getWidth() / this.mImages[index].getWidth())) / Math.log(2))+0.5);

	}

	/**
	 * Get the scale from the Laplacian image index.
	 */
	public int scaleFromIndex(int index) {
		return index % this.mNumScalesPerOctave;
	}

	// DoG images
	private LaplacianImage[] mImages;

	// Number of octaves and scales
	private int mNumOctaves;
	private int mNumScalesPerOctave;


}
