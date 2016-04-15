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

import java.io.IOException;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftIsetFile;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.j2se.ArrayUtils;
import jp.nyatla.nyartoolkit.j2se.BinaryReader;
import jp.nyatla.nyartoolkit.j2se.BinaryWriter;
import jp.nyatla.nyartoolkit.j2se.JpegIO;

/**
 * ARToolKitV5形式のIsetファイルを読み出します。
 * <pre>
 * //ファイル形式
 * int32 as numofiset
 * uint8 as jpeg binary
 * float[numofiset-1] as dpi[n+1]
 * </pre>
 */
public class IsetFileDataParserV5
{
	final public int num_of_iset;
	final public byte[] image;
	final public float image_dpi_x;
	final public float image_dpi_y;
	final public int image_unit;
	final public NyARIntSize image_size;
	/** サブ画像のdpiセット*/
	final public float[] sub_dpis;

	public IsetFileDataParserV5(NyARNftIsetFile.ReferenceImage i_src_img,float[] i_sub_dpis)
	{
		this.image=ArrayUtils.toByteArray_impl(i_src_img.img);
		this.image_dpi_x=(float) i_src_img.dpi;
		this.image_dpi_y=(float)i_src_img.dpi;
		this.image_size=new NyARIntSize(i_src_img.width,i_src_img.height);
		this.image_unit=1;
		this.sub_dpis=i_sub_dpis;
		this.num_of_iset=i_sub_dpis.length+1;
		return;
	}
	/**
	 * @param i_src
	 * isetファイルイメージを格納したbyte配列
	 */
	public IsetFileDataParserV5(byte[] i_src)
	{
		BinaryReader br=new BinaryReader(i_src,BinaryReader.ENDIAN_LITTLE);
		//read raw data
		int size=br.size();
		int noi=br.getInt();
		int jpeg_size=size-4-(4*(noi-1));
		byte[] jpeg=br.getByteArray(jpeg_size);
		float[] ldpi=br.getFloatArray(noi-1);
		
		JpegIO.DecodeResult d;
		try {
			d = JpegIO.decode(jpeg);
		} catch (IOException e) {
			//この例外はファイルアクセスを伴わないから握りつぶしてOK
			throw new NyARRuntimeException(e);
		}
		this.image=d.img;
		this.image_dpi_x=d.x_density;
		this.image_dpi_y=d.y_density;
		this.image_unit=d.density_unit;
		this.image_size=new NyARIntSize(d.width,d.height);
		this.num_of_iset=noi;
		this.sub_dpis=ldpi;
		return;
	}
	/**
	 * 格納している情報から、ファイルイメージを生成します。
	 * @param i_sub_dpis
	 * @return
	 */
	public byte[] makeBinary()
	{
		try {
			//初期メモリは2MB
			BinaryWriter bw=new BinaryWriter(BinaryReader.ENDIAN_LITTLE,2*1024*1024);
			//dpiセット+1
			bw.putInt(this.sub_dpis.length+1);
			//jpgイメージ
			bw.putByteArray(JpegIO.encode(this.image_size.w,this.image_size.h,(int)this.image_dpi_x,(int)this.image_dpi_y,this.image_unit,this.image,0.8f));
			//サブdpi
			bw.putFloatArray(this.sub_dpis);
			return bw.getBinary();
		} catch (IOException e) {
			throw new NyARRuntimeException(e);
		}
	}

	public double getImageDpi()
	{
		if(this.image_unit==1 && this.image_dpi_x==this.image_dpi_y){
			return this.image_dpi_x;
		}else if(this.image_unit==2 && this.image_dpi_x==this.image_dpi_y){
			return this.image_dpi_x*2.54;
		}else if(this.image_unit>2 && this.image_dpi_x==0 && this.image_dpi_y==0){
			return this.image_unit;
		}else{
			return 0;
		}
	}
}
