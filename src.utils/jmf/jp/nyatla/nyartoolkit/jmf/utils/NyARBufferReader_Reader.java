/* 
 * PROJECT: NyARToolkit JMF utilities.
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
package jp.nyatla.nyartoolkit.jmf.utils;

import jp.nyatla.nyartoolkit.core.rasterreader.INyARBufferReader;
import jp.nyatla.nyartoolkit.core.rasterreader.INyARRgbPixelReader;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public abstract class NyARBufferReader_Reader implements INyARRgbPixelReader,INyARBufferReader
{
	protected byte[] _ref_buf;
	protected NyARIntSize _ref_size;
	protected int _buf_type;
	protected NyARBufferReader_Reader(NyARIntSize i_ref_size,int i_buf_type)
	{
		this._ref_size =i_ref_size;
		this._buf_type=i_buf_type;
		return;
	}
	protected NyARBufferReader_Reader(NyARIntSize i_ref_size)
	{
		this._ref_size =i_ref_size;
		return;
	}
	final public Object getBuffer()
	{
		return this._ref_buf;
	}
	final public int getBufferType()
	{
		return this._buf_type;
	}
	final public boolean isEqualBufferType(int i_type_value)
	{
		return this._buf_type==i_type_value;
	}
	public abstract void changeBuffer(javax.media.Buffer i_buffer);

}
