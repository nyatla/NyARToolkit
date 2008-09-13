/* 
 * PROJECT: NyARToolkit Java3D utilities.
 * --------------------------------------------------------------------------------
 * The MIT License
 * Copyright (c) 2008 nyatla
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/nyartoolkit/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
package jp.nyatla.nyartoolkit.java3d.utils;

import java.awt.image.*;

import javax.media.format.RGBFormat;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.jmf.utils.*;

/**
 * 
 * このクラスは、Java3Dと互換性のあるNyARToolkitのラスタイメージを保持します。
 *
 */
public class J3dNyARRaster_RGB extends JmfNyARRaster_RGB
{
	private ImageComponent2D imc2d;

	private byte[] i2d_buf;

	private BufferedImage bufferd_image;

	/**
	 * JMFのキャプチャ画像をこのクラスのBufferedImageにコピーします。
	 * @param i_buffer
	 * 画像の格納されたバッファを指定して下さい。
	 * 画像サイズはコンストラクタで与えたパラメタと同じサイズである必要があります。
	 */
	public void setBuffer(javax.media.Buffer i_buffer) throws NyARException
	{
		//メモ：この時点では、ref_dataにはi_bufferの参照値が入ってる。
		synchronized (this) {
			//キャプチャデータをi2dのバッファにコピーする。（これ省略したいなあ…。）
			System.arraycopy((byte[]) i_buffer.getData(), 0, this.i2d_buf, 0, this.i2d_buf.length);
		}
		int buffer_type = analyzeBufferType((RGBFormat) i_buffer.getFormat());
		this._ref_buf = this.i2d_buf;
		this._reader.changeBuffer(buffer_type, this._ref_buf);
	}

	public J3dNyARRaster_RGB(NyARParam i_cparam)
	{
		super(i_cparam.getScreenSize());

		//RGBのラスタを作る。
		this.bufferd_image = new BufferedImage(this._size.w, this._size.h, BufferedImage.TYPE_3BYTE_BGR);
		i2d_buf = ((DataBufferByte) bufferd_image.getRaster().getDataBuffer()).getData();
		this.imc2d = new ImageComponent2D(ImageComponent2D.FORMAT_RGB, this.bufferd_image, true, true);
		imc2d.setCapability(ImageComponent.ALLOW_IMAGE_WRITE);
	}

	/**
	 * 自身の格納しているImageComponent2Dオブジェクトを作り直します。
	 * Java3D1.5がDirectXで動いた（らしいとき）に、ImageComponent2Dのインスタンス
	 * IDが異ならないと、Behavior内でイメージの更新を通知できない事象に対応するために実装してあります。
	 * Behavior内でgetImageComponent2()関数を実行する直前に呼び出すことで、この事象を回避することができます。
	 * 
	 */
	public void renewImageComponent2D()
	{
		this.imc2d = new ImageComponent2D(ImageComponent2D.FORMAT_RGB, this.bufferd_image, true, true);
		this.imc2d.setCapability(ImageComponent.ALLOW_IMAGE_WRITE);
	}

	public ImageComponent2D getImageComponent2D()
	{
		return this.imc2d;
	}
}
