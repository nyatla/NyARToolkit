/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2006-2015 ARToolworks, Inc.
 *
 *  Author(s): Hirokazu Kato, Philip Lamb
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2016 Ryo Iizuka
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
package jp.nyatla.nyartoolkit.core.marker.nft.iset;

import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftIsetFile;
import jp.nyatla.nyartoolkit.j2se.ArrayUtils;
import jp.nyatla.nyartoolkit.j2se.BinaryReader;
import jp.nyatla.nyartoolkit.j2se.BinaryWriter;

/**
 * NyARToolKitの独自形式です。
 * <pre>
 * int Width
 * int Height
 * byte[] images[Width*Height]
 * int32 Number_of_DPI_set
 * float[] dpis[Number_of_DPI_set]
 * </pre>
 */
public class IsetFileDataParserV5Raw
{
	final public int image_width;
	final public int image_height;
	final public byte[] image;
	/** サブ画像のdpiセット*/
	final public float[] dpis;

	public IsetFileDataParserV5Raw(NyARNftIsetFile.ReferenceImage i_src_img,float[] i_sub_dpis)
	{
		this.image_width=i_src_img.width;
		this.image_height=i_src_img.height;
		this.image=ArrayUtils.toByteArray_impl(i_src_img.img);
		this.dpis=new float[i_sub_dpis.length+1];
		this.dpis[0]=(float)i_src_img.dpi;
		for(int i=0;i<i_sub_dpis.length;i++){
			this.dpis[i+1]=i_sub_dpis[i];
		}
		return;
	}
	/**
	 * @param i_src
	 * isetファイルイメージを格納したbyte配列
	 */
	public IsetFileDataParserV5Raw(byte[] i_src)
	{
		BinaryReader br=new BinaryReader(i_src,BinaryReader.ENDIAN_LITTLE);
		this.image_width=br.getInt();
		this.image_height=br.getInt();
		this.image=br.getByteArray(this.image_width*this.image_height);
		this.dpis=new float[br.getInt()];
		for(int i=0;i<this.dpis.length;i++){
			this.dpis[i]=br.getFloat();
		}
		return;
	}
	/**
	 * 格納している情報から、ファイルイメージを生成します。
	 * @param i_sub_dpis
	 * @return
	 */
	public byte[] makeBinary()
	{
		//初期メモリは2MB
		BinaryWriter bw=new BinaryWriter(BinaryReader.ENDIAN_LITTLE,2*1024*1024);
		bw.putInt(this.image_width);
		bw.putInt(this.image_height);
		bw.putByteArray(this.image);
		bw.putInt(this.dpis.length);
		bw.putFloatArray(this.dpis);
		return bw.getBinary();
	}
}