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
package jp.nyatla.nyartoolkit.core.marker.nft;


import java.io.File;
import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.marker.nft.iset.IsetFileDataParserV4;
import jp.nyatla.nyartoolkit.core.marker.nft.iset.IsetFileDataParserV5;
import jp.nyatla.nyartoolkit.core.marker.nft.iset.IsetFileDataParserV5Raw;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.j2se.ArrayUtils;
import jp.nyatla.nyartoolkit.j2se.BinaryReader;



/**
 * ARToolkitNFTの基準画像セット(ISET)を格納します。
 * AR2ImageSetTと同等の機能です。
 */
public class NyARNftIsetFile
{
	final public static int FILE_FORMAT_ARTK_V5=1;
	final public static int FILE_FORMAT_ARTK_V4=2;
	final public static int FILE_FORMAT_ARTK_V5RAW=3;
	public static NyARNftIsetFile loadFromIsetFile(InputStream i_stream,int i_file_format)
	{
		return loadFromIsetFile(BinaryReader.toArray(i_stream),i_file_format);
	}
	public static NyARNftIsetFile loadFromIsetFile(InputStream i_stream)
	{
		return loadFromIsetFile(i_stream,FILE_FORMAT_ARTK_V5);
	}
	public static NyARNftIsetFile loadFromIsetFile(File i_file,int i_file_format)
	{
		return loadFromIsetFile(BinaryReader.toArray(i_file),i_file_format);
	}
	public static NyARNftIsetFile loadFromIsetFile(File i_file)
	{
		return loadFromIsetFile(BinaryReader.toArray(i_file),FILE_FORMAT_ARTK_V5);		
	}
	
	public static NyARNftIsetFile loadFromIsetFile(byte[] i_src,int i_file_format)
	{
		switch(i_file_format){
		case FILE_FORMAT_ARTK_V5:
		{
			IsetFileDataParserV5 iset= new IsetFileDataParserV5(i_src);
			ReferenceImage[] items=new ReferenceImage[iset.num_of_iset];
			//1stIset
			double dpi=iset.getImageDpi();
			int[] images=ArrayUtils.toIntArray_impl(iset.image);
			items[0]=new ReferenceImage(iset.image_size.w,iset.image_size.h,dpi,images);
			//2nd to end
			for(int i=1;i<iset.num_of_iset;i++){
				items[i]=new ReferenceImage(iset.image_size.w,iset.image_size.h,images,dpi,iset.sub_dpis[i-1]);
			}
			return new NyARNftIsetFile(items);
		}
		case FILE_FORMAT_ARTK_V4:
		{
			IsetFileDataParserV4 iset = new IsetFileDataParserV4(i_src);
			ReferenceImage[] items=new ReferenceImage[iset.ar2image.length];
			for(int i=0;i<items.length;i++){
				IsetFileDataParserV4.AR2ImageT tmp=iset.ar2image[i];
				items[i]=new ReferenceImage(tmp.width,tmp.height,tmp.dpi,ArrayUtils.toIntArray_impl(tmp.img));
			}
			return new NyARNftIsetFile(items);
		}
		case FILE_FORMAT_ARTK_V5RAW:
		{
			IsetFileDataParserV5Raw iset= new IsetFileDataParserV5Raw(i_src);
			ReferenceImage[] items=new ReferenceImage[iset.dpis.length];
			//1stIset
			int[] images=ArrayUtils.toIntArray_impl(iset.image);
			items[0]=new ReferenceImage(iset.image_width,iset.image_height,iset.dpis[0],images);
			//2nd to end
			for(int i=1;i<iset.dpis.length;i++){
				items[i]=new ReferenceImage(iset.image_width,iset.image_height,images,iset.dpis[0],iset.dpis[i]);
			}
			return new NyARNftIsetFile(items);		
		}
		default:
			throw new NyARRuntimeException();
		}
	}
	/**
	 * Grayscale画像からisetファイルイメージを生成します。
	 * @param i_raster
	 * @param srcdpi
	 * @param dpis
	 * @return
	 */
	public static NyARNftIsetFile genImageSet(INyARGrayscaleRaster i_raster,double srcdpi,double[] dpis)
	{		
		NyARNftIsetFile.ReferenceImage[] rlist=new NyARNftIsetFile.ReferenceImage[dpis.length];
		rlist[0]=new NyARNftIsetFile.ReferenceImage(i_raster,srcdpi,srcdpi);
		for(int i=1;i<dpis.length;i++){
			rlist[i]=new NyARNftIsetFile.ReferenceImage(i_raster,srcdpi,dpis[i]);
		}
		return new NyARNftIsetFile(rlist);
	}	
	public static NyARNftIsetFile genImageSet(INyARGrayscaleRaster i_raster,double srcdpi)
	{	
		double[] dpis=makeDpiList(i_raster.getWidth(),i_raster.getHeight(),srcdpi);
		return genImageSet(i_raster, srcdpi, dpis);
	}
	// Reads dpiMinAllowable, xsize, ysize, dpi, background, dpiMin, dpiMax.
	// Sets dpiMin, dpiMax, dpi_num, dpi_list.
	private static double[] makeDpiList(double dpiMin, double dpiMax)
	{
		int dpi_num = 1;
		// Decide how many levels we need.
		if (dpiMin == dpiMax) {
			// nothing to do
		} else {
			double dpiWork = dpiMin;
			int i;
			for (i=1;;i++) {
				dpiWork *= Math.pow(2.0f, 1.0f / 3.0f); // *= 1.25992104989487
				if (dpiWork >= dpiMax * 0.95f) {
					break;
				}
			}
			dpi_num=i+1;
		}
		double[] dpi_list = new double[dpi_num];
		// Determine the DPI values of each level.
		{
			double dpiWork = dpiMin;
			for (int i = 0; i < dpi_num; i++) {
				dpi_list[dpi_num - i - 1] = dpiWork; // Lowest value goes at tail of array, highest at head.
				dpiWork *= Math.pow(2.0f, 1.0f / 3.0f);
				if (dpiWork >= dpiMax * 0.95f)
					dpiWork = dpiMax;
			}
		}
		return dpi_list;
	}
	private static double[] makeDpiList(int xsize, int ysize, double dpi)
	{
		// Determine minimum allowable DPI, truncated to 3 decimal places.
		double dpiMinAllowable = Math.floor(((double) KPM_MINIMUM_IMAGE_SIZE / Math.min(xsize, ysize)) * dpi * 1000.0) / 1000.0f;
		return makeDpiList(dpiMinAllowable, dpi);
	}
	
	public NyARNftIsetFile(ReferenceImage[] i_items)
	{
		this.items=i_items;
	}
	
	final public ReferenceImage[] items;	
	final public static int KPM_MINIMUM_IMAGE_SIZE = 28;

	/**
	 * 現在のファイルイメージをbyte[]で返却します。
	 * @return
	 */
	public byte[] makeIsetBinary()
	{
		return this.makeIsetBinary(FILE_FORMAT_ARTK_V5);
	}
	public byte[] makeIsetBinary(int i_type)
	{
		float[] dpis=new float[this.items.length-1];
		for(int i=0;i<dpis.length;i++){
			dpis[i]=(float) this.items[i+1].dpi;
		}
		switch(i_type){
		case FILE_FORMAT_ARTK_V5:
		{
			IsetFileDataParserV5 ifp=new IsetFileDataParserV5(this.items[0],dpis);
			return ifp.makeBinary();
		}
		case FILE_FORMAT_ARTK_V5RAW:
		{
			IsetFileDataParserV5Raw ifp=new IsetFileDataParserV5Raw(this.items[0],dpis);
			return ifp.makeBinary();
		}
		default:
			throw new NyARRuntimeException();
		}
	}	
	
	/**
	 * ワーク関数
	 * @param x
	 * @return
	 */
	private static int lroundf(double x){
		return (int) ((x)>=0.0f?(long)((x)+0.5f):(long)((x)-0.5f));
	}	
	public static class ReferenceImage
	{
		public double dpi;
		public int width;
		public int height;
		public final int[] img;
		public ReferenceImage(int i_w,int i_h,double i_dpi,int[] i_ref_buf)
		{
			this.width=i_w;
			this.height=i_h;
			this.dpi=i_dpi;
			this.img=i_ref_buf;
		}
		public ReferenceImage(int i_w,int i_h,double i_dpi)
		{
			this(i_w,i_h,i_dpi,new int[i_w*i_h]);
		}
		
		/**
		 * レイヤ2以降のイメージを生成する。
		 * idxは{@link #dpi }のlength-1まで。
		 * @param i_idx
		 * @return
		 */
		public ReferenceImage(int i_w,int i_h,int[] i_src,double i_src_dpi,double i_dest_dpi)
		{
			//int1Dラスタのラッパーを通して実行する。
			this(NyARGrayscaleRaster.createInstance(i_w, i_h,NyARBufferType.INT1D_GRAY_8,i_src),i_src_dpi,i_dest_dpi);
		    return;		
		}
		/**
		 * GrayscaleRasterから任意サイズのパッチイメージを生成する。
		 * @param i_src
		 * @param i_src_dpi
		 * @param i_dest_dpi
		 */
		public ReferenceImage(INyARGrayscaleRaster i_src,double i_src_dpi,double i_dest_dpi)
		{
			this(
				(int)lroundf(i_src.getWidth() * i_dest_dpi / i_src_dpi),
				(int)lroundf(i_src.getHeight() * i_dest_dpi / i_src_dpi),
				i_dest_dpi);
		
		    int p2 = 0;//dst->imgBW;
		    int wx=this.width;
		    int wy=this.height;
		    int sh=i_src.getHeight();
		    int sw=i_src.getWidth();

		    for(int jj = 0; jj < wy; jj++ ) {
		        int sy = (int)lroundf( jj    * i_src_dpi / dpi);
		        int ey = (int)lroundf((jj+1) * i_src_dpi / dpi) - 1;
		        if( ey >= sh ){
		        	ey = sh - 1;
		        }
		        for(int ii = 0; ii < wx; ii++ ) {
		        	int sx = (int)lroundf( ii    * i_src_dpi / dpi);
		        	int ex = (int)lroundf((ii+1) * i_src_dpi / dpi) - 1;
		            if( ex >= sw ){
		            	ex = sw - 1;
		            }
		            int co =0;
		            int value = 0;
		            for(int jjj = sy; jjj <= ey; jjj++ ) {
		                for(int iii = sx; iii <= ex; iii++ ) {
		                    value += i_src.getPixel(iii,jjj) & 0xff;
		                    co++;
		                }
		            }
		            this.img[p2++] = (value / co);
		        }
		    }
		    return;		
		}
	}
	

	public static void main(String[] args){
		NyARNftIsetFile f=NyARNftIsetFile.loadFromIsetFile(new File("../Data/pinball.iset5"));
		for(int i=0;i<f.items.length;i++){
			int s=f.items[i].width*f.items[i].height;
			long sum=0;
			for(int i2=0;i2<s;i2++){
				sum+=(f.items[i].img[i2] & 0xff);
			}
			System.out.println(f.items[i].dpi+","+f.items[i].width+","+f.items[i].height+","+Long.toString(sum));
		}
		NyARNftIsetFile f1=loadFromIsetFile(new File("../Data/pinball.iset5"));
//		NyARNftIsetFile f2=loadFromIsetFile(f1.makeFileImage(),FILE_FORMAT_ARTK_V5);
//
//		byte[] d=iset.makeFileImage(new float[]{20,40});
//		IsetFileDataParserV5 iset2= new IsetFileDataParserV5(d);
		
		return;
	}	
}









