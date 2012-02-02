package jp.nyatla.nyartoolkit.core.rasterreader;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARRgbPixelDriver;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.utils.NyARPerspectiveParamGenerator;
import jp.nyatla.nyartoolkit.core.utils.NyARPerspectiveParamGenerator_O1;

public class CopyOfNyARPerspectiveReaderFactory
{
	/**
	 * 指定したIN/OUTに最適な{@link INyARPerspectiveReaader}を生成します。
	 * <p>入力ラスタについて
	 * 基本的には全ての{@link INyARRgbRaster}を実装したクラスを処理できますが、次の３種類のバッファを持つものを推奨します。
	 * <ul>
	 * <li>{@link NyARBufferType#INT1D_X8R8G8B8_32}
	 * <li>{@link NyARBufferType#BYTE1D_B8G8R8_24}
	 * <li>{@link NyARBufferType#BYTE1D_R8G8B8_24}
	 * </ul>
	 * </p>
	 * <p>出力ラスタについて
	 * 基本的には全ての{@link NyARBufferType#INT1D_X8R8G8B8_32}形式のバッファを持つラスタを使用してください。
	 * 他の形式でも動作しますが、低速な場合があります。
	 * </p>
	 * <p>高速化について - 
	 * 入力ラスタ形式が、{@link NyARBufferType#INT1D_X8R8G8B8_32},{@link NyARBufferType#BYTE1D_B8G8R8_24}
	 * ,{@link NyARBufferType#BYTE1D_R8G8B8_24}のものについては、他の形式よりも高速に動作します。
	 * また、出力ラスタ形式が、{@link NyARBufferType#INT1D_X8R8G8B8_32}の物については、単体サンプリングモードの時のみ、さらに高速に動作します。
	 * 他の形式のラスタでは、以上のものよりも低速転送で対応します。
	 * @param i_in_raster_type
	 * 入力ラスタの形式です。
	 * @param i_out_raster_type
	 * 出力ラスタの形式です。
	 * @return
	 */
	public INyARPerspectiveReader createDriver(int i_in_raster_type,int i_out_raster_type)
	{
		//新しいモードに対応したら書いてね。
		switch(i_in_raster_type){
		case NyARBufferType.BYTE1D_B8G8R8X8_32:
			return i_out_raster_type==NyARBufferType.INT1D_X8R8G8B8_32?
				new NyARPerspectiveReader_BYTE1D_B8G8R8X8_32_to_INT1D_X8R8G8B8_32():
				new NyARPerspectiveReader_BYTE1D_B8G8R8X8_32_to_ANY();
		case NyARBufferType.BYTE1D_B8G8R8_24:
			return i_out_raster_type==NyARBufferType.INT1D_X8R8G8B8_32?
				new NyARPerspectiveReader_BYTE1D_B8G8R8_24_to_INT1D_X8R8G8B8_32():
				new NyARPerspectiveReader_BYTE1D_B8G8R8_24_to_ANY();
		case NyARBufferType.BYTE1D_R8G8B8_24:
			return i_out_raster_type==NyARBufferType.INT1D_X8R8G8B8_32?
				new NyARPerspectiveReader_BYTE1D_R8G8B8_24_to_INT1D_X8R8G8B8_32():
				new NyARPerspectiveReader_BYTE1D_R8G8B8_24_to_ANY();
		default:
			return i_out_raster_type==NyARBufferType.INT1D_X8R8G8B8_32?
				new NyARPerspectiveReader_ANY_to_INT1D_X8R8G8B8_32():
				new NyARPerspectiveReader_ANY_to_ANY();
		}
	}
	/**
	 * ブランクドライバを返します。
	 * @return
	 */
	public INyARPerspectiveReader createBlankDriver()
	{
		return new NyARPerspectiveReader_NULL_ALLZERO();
	}
	private static CopyOfNyARPerspectiveReaderFactory _s_instance=new CopyOfNyARPerspectiveReaderFactory();
	public static CopyOfNyARPerspectiveReaderFactory getInstance()
	{
		return _s_instance;
	}
}

/**
 * このインタフェイスは、ラスタから任意四角形のパターンを取得する関数を定義します。
 * パターンは、遠近法を使ったパースペクティブ補正をかけて、ラスタ矩形に得られます。
 * <p>サンプリングモード -
 * このクラスは、２種類のサンプリングモードがあります。単体サンプルモードと、マルチサンプルモードです。
 * 単体サンプルモードは、{@link #read4Point}関数の解像度値に1を指定したときのモードです。出力1ピクセルに対して、入力1ピクセルを割り当てます。
 * マルチサンプルモードは、{@link #read4Point}関数の解像度値に2以上を指定したときのモードです。出力1ピクセルに対して、入力nピクセルの平均値を割り当てます。
 * 低解像度の出力を得る場合、マルチサンプルモードの方が良い結果が得られますが、単体サンプルモードと比較して低速になります。
 * </p>
 * 通常、このインタフェイスを持つクラスは、{@link CopyOfNyARPerspectiveReaderFactory#createInstance}を使って生成します。
 * <p>メモ-
 * この関数は、1倍の時はNyARColorPatt_Perspective,
 * n倍の時はNyARColorPatt_Perspective_O2の関数を元に作ってます。
 * </p>
 */
abstract class NyARPerspectiveReaderBase implements INyARPerspectiveReader
{
	public abstract boolean isCompatibleRaster(INyARRaster i_in_raster,INyARRaster i_out_raster);
	protected abstract void onePixel(int pk_l,int pk_t,double[] cpara,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException;
	protected abstract void multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException;
	/** */
	private static final int LOCAL_LT=1;
	/** 射影変換パラメータの記憶配列*/
	protected final double[] __pickFromRaster_cpara=new double[8];
	/** 射影変換パラメータ*/
	protected NyARPerspectiveParamGenerator _perspective_gen;
	protected NyARPerspectiveReaderBase()
	{
		this._perspective_gen=new NyARPerspectiveParamGenerator_O1(LOCAL_LT,LOCAL_LT);		
	}
	public boolean read4Point(INyARRgbRaster i_in_raster,NyARDoublePoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster o_out)throws NyARException
	{
		NyARIntSize out_size=o_out.getSize();
		int xe=out_size.w*i_edge_x/50;
		int ye=out_size.h*i_edge_y/50;

		//サンプリング解像度で分岐
		if(i_resolution==1){
			if (!this._perspective_gen.getParam((xe*2+out_size.w),(ye*2+out_size.h),i_vertex, this.__pickFromRaster_cpara)) {
				return false;
			}
			this.onePixel(xe+LOCAL_LT,ye+LOCAL_LT,this.__pickFromRaster_cpara,i_in_raster,o_out);
		}else{
			if (!this._perspective_gen.getParam((xe*2+out_size.w)*i_resolution,(ye*2+out_size.h)*i_resolution,i_vertex, this.__pickFromRaster_cpara)) {
				return false;
			}
			this.multiPixel(xe*i_resolution+LOCAL_LT,ye*i_resolution+LOCAL_LT,this.__pickFromRaster_cpara,i_resolution,i_in_raster,o_out);
		}
		return true;
	}
	public boolean read4Point(INyARRgbRaster i_in_raster,NyARIntPoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster o_out)throws NyARException
	{
		NyARIntSize out_size=o_out.getSize();
		int xe=out_size.w*i_edge_x/50;
		int ye=out_size.h*i_edge_y/50;

		//サンプリング解像度で分岐
		if(i_resolution==1){
			if (!this._perspective_gen.getParam((xe*2+out_size.w),(ye*2+out_size.h),i_vertex, this.__pickFromRaster_cpara)) {
				return false;
			}
			this.onePixel(xe+LOCAL_LT,ye+LOCAL_LT,this.__pickFromRaster_cpara,i_in_raster,o_out);
		}else{
			if (!this._perspective_gen.getParam((xe*2+out_size.w)*i_resolution,(ye*2+out_size.h)*i_resolution,i_vertex, this.__pickFromRaster_cpara)) {
				return false;
			}
			this.multiPixel(xe*i_resolution+LOCAL_LT,ye*i_resolution+LOCAL_LT,this.__pickFromRaster_cpara,i_resolution,i_in_raster,o_out);
		}
		return true;
	}
	public boolean read4Point(INyARRgbRaster i_in_raster,double i_x1,double i_y1,double i_x2,double i_y2,double i_x3,double i_y3,double i_x4,double i_y4,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster o_out)throws NyARException
	{
		NyARIntSize out_size=o_out.getSize();
		int xe=out_size.w*i_edge_x/50;
		int ye=out_size.h*i_edge_y/50;

		//サンプリング解像度で分岐
		if(i_resolution==1){
			if (!this._perspective_gen.getParam((xe*2+out_size.w),(ye*2+out_size.h),i_x1,i_y1,i_x2,i_y2,i_x3,i_y3,i_x4,i_y4, this.__pickFromRaster_cpara)) {
				return false;
			}
			this.onePixel(xe+LOCAL_LT,ye+LOCAL_LT,this.__pickFromRaster_cpara,i_in_raster,o_out);
		}else{
			if (!this._perspective_gen.getParam((xe*2+out_size.w)*i_resolution,(ye*2+out_size.h)*i_resolution,i_x1,i_y1,i_x2,i_y2,i_x3,i_y3,i_x4,i_y4, this.__pickFromRaster_cpara)) {
				return false;
			}
			this.multiPixel(xe*i_resolution+LOCAL_LT,ye*i_resolution+LOCAL_LT,this.__pickFromRaster_cpara,i_resolution,i_in_raster,o_out);
		}
		return true;
	}	
}

/**
 * ブランクReader.処理は常に失敗し、どのラスタとも互換性が無い。
 */
class NyARPerspectiveReader_NULL_ALLZERO implements INyARPerspectiveReader
{
	public boolean isCompatibleRaster(INyARRaster i_in_raster,INyARRaster i_out_raster)
	{
		return false;
	}
	public boolean read4Point(INyARRgbRaster i_in_raster,NyARDoublePoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster o_out)throws NyARException
	{
		throw new NyARException();
	}
	public boolean read4Point(INyARRgbRaster i_in_raster,NyARIntPoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster o_out)throws NyARException
	{
		throw new NyARException();
	}
	public boolean read4Point(INyARRgbRaster i_in_raster,double i_x1,double i_y1,double i_x2,double i_y2,double i_x3,double i_y3,double i_x4,double i_y4,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster o_out)throws NyARException
	{
		throw new NyARException();
	}
}





class NyARPerspectiveReader_BYTE1D_B8G8R8X8_32_to_ANY extends NyARPerspectiveReaderBase
{
	public boolean isCompatibleRaster(INyARRaster i_in_raster,INyARRaster i_out_raster)
	{
		//INのみチェック
		return i_in_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32);
	}
	protected void onePixel(int pk_l,int pk_t,double[] cpara,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		INyARRgbPixelDriver out_reader=o_out.getRgbPixelDriver();

		int in_w=i_in_raster.getWidth();
		int in_h=i_in_raster.getHeight();
		byte[] i_in_buf=(byte[])i_in_raster.getBuffer();
		
		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		double cp7_cy_1  =cp7*pk_t+1.0+cp6*pk_l;
		double cp1_cy_cp2=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4_cy_cp5=cp4*pk_t+cpara[5]+cp3*pk_l;
		int r,g,b;
		for(int iy=0;iy<out_h;iy++){
			//解像度分の点を取る。
			double cp7_cy_1_cp6_cx  =cp7_cy_1;
			double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
			double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
			
			for(int ix=0;ix<out_w;ix++){
				//1ピクセルを作成
				final double d=1/(cp7_cy_1_cp6_cx);
				int x=(int)((cp1_cy_cp2_cp0_cx)*d);
				int y=(int)((cp4_cy_cp5_cp3_cx)*d);
				if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
				if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						
				final int bp = (x + y * in_w) * 4;
				r=(i_in_buf[bp + 2] & 0xff);
				g=(i_in_buf[bp + 1] & 0xff);
				b=(i_in_buf[bp + 0] & 0xff);
				cp7_cy_1_cp6_cx+=cp6;
				cp1_cy_cp2_cp0_cx+=cp0;
				cp4_cy_cp5_cp3_cx+=cp3;

				out_reader.setPixel(ix,iy,r,g,b);
			}
			cp7_cy_1+=cp7;
			cp1_cy_cp2+=cp1;
			cp4_cy_cp5+=cp4;
		}
		return;
		
	}
	protected final void multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		int in_w=i_in_raster.getWidth();
		int in_h=i_in_raster.getHeight();
		byte[] i_in_buf=(byte[])i_in_raster.getBuffer();
		final int res_pix=i_resolution*i_resolution;
		INyARRgbPixelDriver out_reader=o_out.getRgbPixelDriver();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		for(int iy=out_h-1;iy>=0;iy--){
			//解像度分の点を取る。
			for(int ix=out_w-1;ix>=0;ix--){
				int r,g,b;
				r=g=b=0;
				int cy=pk_t+iy*i_resolution;
				int cx=pk_l+ix*i_resolution;
				double cp7_cy_1_cp6_cx_b  =cp7*cy+1.0+cp6*cx;
				double cp1_cy_cp2_cp0_cx_b=cp1*cy+cp2+cp0*cx;
				double cp4_cy_cp5_cp3_cx_b=cp4*cy+cp5+cp3*cx;
				for(int i2y=i_resolution-1;i2y>=0;i2y--){
					double cp7_cy_1_cp6_cx  =cp7_cy_1_cp6_cx_b;
					double cp1_cy_cp2_cp0_cx=cp1_cy_cp2_cp0_cx_b;
					double cp4_cy_cp5_cp3_cx=cp4_cy_cp5_cp3_cx_b;
					for(int i2x=i_resolution-1;i2x>=0;i2x--){
						//1ピクセルを作成
						final double d=1/(cp7_cy_1_cp6_cx);
						int x=(int)((cp1_cy_cp2_cp0_cx)*d);
						int y=(int)((cp4_cy_cp5_cp3_cx)*d);
						if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
						if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						
						final int bp = (x + y * in_w) * 4;
						r+=(i_in_buf[bp + 2] & 0xff);
						g+=(i_in_buf[bp + 1] & 0xff);
						b+=(i_in_buf[bp + 0] & 0xff);
						cp7_cy_1_cp6_cx+=cp6;
						cp1_cy_cp2_cp0_cx+=cp0;
						cp4_cy_cp5_cp3_cx+=cp3;
					}
					cp7_cy_1_cp6_cx_b+=cp7;
					cp1_cy_cp2_cp0_cx_b+=cp1;
					cp4_cy_cp5_cp3_cx_b+=cp4;
				}
				out_reader.setPixel(ix,iy,r/res_pix,g/res_pix,b/res_pix);
			}
		}
		return;
	}
}

class NyARPerspectiveReader_BYTE1D_B8G8R8X8_32_to_INT1D_X8R8G8B8_32 extends NyARPerspectiveReader_BYTE1D_B8G8R8X8_32_to_ANY
{
	public final boolean isCompatibleRaster(INyARRaster i_in_raster,INyARRaster i_out_raster)
	{
		//INのみチェック
		return i_in_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8X8_32) && i_out_raster.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32);
	}	
	protected void onePixel(int pk_l,int pk_t,double[] cpara,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		assert(o_out.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
		int in_w=i_in_raster.getWidth();
		int in_h=i_in_raster.getHeight();
		byte[] i_in_buf=(byte[])i_in_raster.getBuffer();

		int[] pat_data=(int[])o_out.getBuffer();
		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		double cp7_cy_1  =cp7*pk_t+1.0+cp6*pk_l;
		double cp1_cy_cp2=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4_cy_cp5=cp4*pk_t+cpara[5]+cp3*pk_l;
		int r,g,b;
		int p=0;
		for(int iy=0;iy<out_h;iy++){
			//解像度分の点を取る。
			double cp7_cy_1_cp6_cx  =cp7_cy_1;
			double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
			double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
			
			for(int ix=0;ix<out_w;ix++){
				//1ピクセルを作成
				final double d=1/(cp7_cy_1_cp6_cx);
				int x=(int)((cp1_cy_cp2_cp0_cx)*d);
				int y=(int)((cp4_cy_cp5_cp3_cx)*d);
				if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
				if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						
				final int bp = (x + y * in_w) * 4;
				r=(i_in_buf[bp + 2] & 0xff);
				g=(i_in_buf[bp + 1] & 0xff);
				b=(i_in_buf[bp + 0] & 0xff);
				cp7_cy_1_cp6_cx+=cp6;
				cp1_cy_cp2_cp0_cx+=cp0;
				cp4_cy_cp5_cp3_cx+=cp3;
				pat_data[p]=(r<<16)|(g<<8)|((b&0xff));
				p++;
			}
			cp7_cy_1+=cp7;
			cp1_cy_cp2+=cp1;
			cp4_cy_cp5+=cp4;
		}
		return;		
	}

}

class NyARPerspectiveReader_BYTE1D_B8G8R8_24_to_ANY extends NyARPerspectiveReaderBase
{
	public boolean isCompatibleRaster(INyARRaster i_in_raster,INyARRaster i_out_raster)
	{
		//INのみチェック
		return i_in_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24);
	}
	protected void onePixel(int pk_l,int pk_t,double[] cpara,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		INyARRgbPixelDriver out_reader=o_out.getRgbPixelDriver();
		int in_w=i_in_raster.getWidth();
		int in_h=i_in_raster.getHeight();
		byte[] i_in_buf=(byte[])i_in_raster.getBuffer();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		double cp7_cy_1  =cp7*pk_t+1.0+cp6*pk_l;
		double cp1_cy_cp2=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4_cy_cp5=cp4*pk_t+cpara[5]+cp3*pk_l;
		int r,g,b;
		for(int iy=0;iy<out_h;iy++){
			//解像度分の点を取る。
			double cp7_cy_1_cp6_cx  =cp7_cy_1;
			double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
			double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
			
			for(int ix=0;ix<out_w;ix++){
				//1ピクセルを作成
				final double d=1/(cp7_cy_1_cp6_cx);
				int x=(int)((cp1_cy_cp2_cp0_cx)*d);
				int y=(int)((cp4_cy_cp5_cp3_cx)*d);
				if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
				if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						
				final int bp = (x + y * in_w) * 3;
				r=(i_in_buf[bp + 2] & 0xff);
				g=(i_in_buf[bp + 1] & 0xff);
				b=(i_in_buf[bp + 0] & 0xff);
				cp7_cy_1_cp6_cx+=cp6;
				cp1_cy_cp2_cp0_cx+=cp0;
				cp4_cy_cp5_cp3_cx+=cp3;

				out_reader.setPixel(ix,iy,r,g,b);
			}
			cp7_cy_1+=cp7;
			cp1_cy_cp2+=cp1;
			cp4_cy_cp5+=cp4;
		}
		return;		
	}
	protected void multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		final int res_pix=i_resolution*i_resolution;

		INyARRgbPixelDriver out_reader=o_out.getRgbPixelDriver();
		int in_w=i_in_raster.getWidth();
		int in_h=i_in_raster.getHeight();
		byte[] i_in_buf=(byte[])i_in_raster.getBuffer();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		for(int iy=out_h-1;iy>=0;iy--){
			//解像度分の点を取る。
			for(int ix=out_w-1;ix>=0;ix--){
				int r,g,b;
				r=g=b=0;
				int cy=pk_t+iy*i_resolution;
				int cx=pk_l+ix*i_resolution;
				double cp7_cy_1_cp6_cx_b  =cp7*cy+1.0+cp6*cx;
				double cp1_cy_cp2_cp0_cx_b=cp1*cy+cp2+cp0*cx;
				double cp4_cy_cp5_cp3_cx_b=cp4*cy+cp5+cp3*cx;
				for(int i2y=i_resolution-1;i2y>=0;i2y--){
					double cp7_cy_1_cp6_cx  =cp7_cy_1_cp6_cx_b;
					double cp1_cy_cp2_cp0_cx=cp1_cy_cp2_cp0_cx_b;
					double cp4_cy_cp5_cp3_cx=cp4_cy_cp5_cp3_cx_b;
					for(int i2x=i_resolution-1;i2x>=0;i2x--){
						//1ピクセルを作成
						final double d=1/(cp7_cy_1_cp6_cx);
						int x=(int)((cp1_cy_cp2_cp0_cx)*d);
						int y=(int)((cp4_cy_cp5_cp3_cx)*d);
						if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
						if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						
						final int bp = (x + y * in_w) * 3;
						r+=(i_in_buf[bp + 2] & 0xff);
						g+=(i_in_buf[bp + 1] & 0xff);
						b+=(i_in_buf[bp + 0] & 0xff);
						cp7_cy_1_cp6_cx+=cp6;
						cp1_cy_cp2_cp0_cx+=cp0;
						cp4_cy_cp5_cp3_cx+=cp3;
					}
					cp7_cy_1_cp6_cx_b+=cp7;
					cp1_cy_cp2_cp0_cx_b+=cp1;
					cp4_cy_cp5_cp3_cx_b+=cp4;
				}
				out_reader.setPixel(ix,iy,r/res_pix,g/res_pix,b/res_pix);
			}
		}
		return;		
	}
	
}
class NyARPerspectiveReader_BYTE1D_B8G8R8_24_to_INT1D_X8R8G8B8_32 extends NyARPerspectiveReader_BYTE1D_B8G8R8_24_to_ANY
{
	public boolean isCompatibleRaster(INyARRaster i_in_raster,INyARRaster i_out_raster)
	{
		//INのみチェック
		return i_in_raster.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24) && i_out_raster.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32);
	}
	protected void onePixel(int pk_l,int pk_t,double[] cpara,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		assert(o_out.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
		int in_w=i_in_raster.getWidth();
		int in_h=i_in_raster.getHeight();
		byte[] i_in_buf=(byte[])i_in_raster.getBuffer();
		int[] pat_data=(int[])o_out.getBuffer();
		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		double cp7_cy_1  =cp7*pk_t+1.0+cp6*pk_l;
		double cp1_cy_cp2=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4_cy_cp5=cp4*pk_t+cpara[5]+cp3*pk_l;
		int r,g,b;
		int p=0;
		for(int iy=0;iy<out_h;iy++){
			//解像度分の点を取る。
			double cp7_cy_1_cp6_cx  =cp7_cy_1;
			double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
			double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
			
			for(int ix=0;ix<out_w;ix++){
				//1ピクセルを作成
				final double d=1/(cp7_cy_1_cp6_cx);
				int x=(int)((cp1_cy_cp2_cp0_cx)*d);
				int y=(int)((cp4_cy_cp5_cp3_cx)*d);
				if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
				if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						
				final int bp = (x + y * in_w) * 3;
				r=(i_in_buf[bp + 2] & 0xff);
				g=(i_in_buf[bp + 1] & 0xff);
				b=(i_in_buf[bp + 0] & 0xff);
				cp7_cy_1_cp6_cx+=cp6;
				cp1_cy_cp2_cp0_cx+=cp0;
				cp4_cy_cp5_cp3_cx+=cp3;
				pat_data[p]=(r<<16)|(g<<8)|((b&0xff));
				p++;
			}
			cp7_cy_1+=cp7;
			cp1_cy_cp2+=cp1;
			cp4_cy_cp5+=cp4;
		}
		return;		
	}
}

class NyARPerspectiveReader_BYTE1D_R8G8B8_24_to_ANY extends NyARPerspectiveReaderBase
{
	public boolean isCompatibleRaster(INyARRaster i_in_raster,INyARRaster i_out_raster)
	{
		//INのみチェック
		return i_in_raster.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24);
	}
	protected void onePixel(int pk_l,int pk_t,double[] cpara,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		INyARRgbPixelDriver out_reader=o_out.getRgbPixelDriver();
		int in_w=i_in_raster.getWidth();
		int in_h=i_in_raster.getHeight();
		byte[] i_in_buf=(byte[])i_in_raster.getBuffer();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		double cp7_cy_1  =cp7*pk_t+1.0+cp6*pk_l;
		double cp1_cy_cp2=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4_cy_cp5=cp4*pk_t+cpara[5]+cp3*pk_l;
		int r,g,b;
		for(int iy=0;iy<out_h;iy++){
			//解像度分の点を取る。
			double cp7_cy_1_cp6_cx  =cp7_cy_1;
			double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
			double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
			
			for(int ix=0;ix<out_w;ix++){
				//1ピクセルを作成
				final double d=1/(cp7_cy_1_cp6_cx);
				int x=(int)((cp1_cy_cp2_cp0_cx)*d);
				int y=(int)((cp4_cy_cp5_cp3_cx)*d);
				if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
				if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						
				final int bp = (x + y * in_w) * 3;
				r=(i_in_buf[bp + 0] & 0xff);
				g=(i_in_buf[bp + 1] & 0xff);
				b=(i_in_buf[bp + 2] & 0xff);
				cp7_cy_1_cp6_cx+=cp6;
				cp1_cy_cp2_cp0_cx+=cp0;
				cp4_cy_cp5_cp3_cx+=cp3;

				out_reader.setPixel(ix,iy,r,g,b);
			}
			cp7_cy_1+=cp7;
			cp1_cy_cp2+=cp1;
			cp4_cy_cp5+=cp4;
		}
		return;		
	}
	protected void multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		final int res_pix=i_resolution*i_resolution;

		INyARRgbPixelDriver out_reader=o_out.getRgbPixelDriver();
		int in_w=i_in_raster.getWidth();
		int in_h=i_in_raster.getHeight();
		byte[] i_in_buf=(byte[])i_in_raster.getBuffer();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		for(int iy=out_h-1;iy>=0;iy--){
			//解像度分の点を取る。
			for(int ix=out_w-1;ix>=0;ix--){
				int r,g,b;
				r=g=b=0;
				int cy=pk_t+iy*i_resolution;
				int cx=pk_l+ix*i_resolution;
				double cp7_cy_1_cp6_cx_b  =cp7*cy+1.0+cp6*cx;
				double cp1_cy_cp2_cp0_cx_b=cp1*cy+cp2+cp0*cx;
				double cp4_cy_cp5_cp3_cx_b=cp4*cy+cp5+cp3*cx;
				for(int i2y=i_resolution-1;i2y>=0;i2y--){
					double cp7_cy_1_cp6_cx  =cp7_cy_1_cp6_cx_b;
					double cp1_cy_cp2_cp0_cx=cp1_cy_cp2_cp0_cx_b;
					double cp4_cy_cp5_cp3_cx=cp4_cy_cp5_cp3_cx_b;
					for(int i2x=i_resolution-1;i2x>=0;i2x--){
						//1ピクセルを作成
						final double d=1/(cp7_cy_1_cp6_cx);
						int x=(int)((cp1_cy_cp2_cp0_cx)*d);
						int y=(int)((cp4_cy_cp5_cp3_cx)*d);
						if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
						if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						
						final int bp = (x + y * in_w) * 3;
						r+=(i_in_buf[bp + 0] & 0xff);
						g+=(i_in_buf[bp + 1] & 0xff);
						b+=(i_in_buf[bp + 2] & 0xff);
						cp7_cy_1_cp6_cx+=cp6;
						cp1_cy_cp2_cp0_cx+=cp0;
						cp4_cy_cp5_cp3_cx+=cp3;
					}
					cp7_cy_1_cp6_cx_b+=cp7;
					cp1_cy_cp2_cp0_cx_b+=cp1;
					cp4_cy_cp5_cp3_cx_b+=cp4;
				}
				out_reader.setPixel(ix,iy,r/res_pix,g/res_pix,b/res_pix);
			}
		}
		return;		
	}
}

class NyARPerspectiveReader_BYTE1D_R8G8B8_24_to_INT1D_X8R8G8B8_32 extends NyARPerspectiveReader_BYTE1D_R8G8B8_24_to_ANY
{
	public boolean isCompatibleRaster(INyARRaster i_in_raster,INyARRaster i_out_raster)
	{
		return i_in_raster.isEqualBufferType(NyARBufferType.BYTE1D_R8G8B8_24) && i_out_raster.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32);
	}
	protected void onePixel(int pk_l,int pk_t,int in_w,int in_h,double[] cpara,byte[] i_in_buf,INyARRgbRaster o_out)throws NyARException
	{
		assert(o_out.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
		int[] pat_data=(int[])o_out.getBuffer();
		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		double cp7_cy_1  =cp7*pk_t+1.0+cp6*pk_l;
		double cp1_cy_cp2=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4_cy_cp5=cp4*pk_t+cpara[5]+cp3*pk_l;
		int r,g,b;
		int p=0;
		for(int iy=0;iy<out_h;iy++){
			//解像度分の点を取る。
			double cp7_cy_1_cp6_cx  =cp7_cy_1;
			double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
			double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
			
			for(int ix=0;ix<out_w;ix++){
				//1ピクセルを作成
				final double d=1/(cp7_cy_1_cp6_cx);
				int x=(int)((cp1_cy_cp2_cp0_cx)*d);
				int y=(int)((cp4_cy_cp5_cp3_cx)*d);
				if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
				if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						
				final int bp = (x + y * in_w) * 3;
				r=(i_in_buf[bp + 0] & 0xff);
				g=(i_in_buf[bp + 1] & 0xff);
				b=(i_in_buf[bp + 2] & 0xff);
				cp7_cy_1_cp6_cx+=cp6;
				cp1_cy_cp2_cp0_cx+=cp0;
				cp4_cy_cp5_cp3_cx+=cp3;
				pat_data[p]=(r<<16)|(g<<8)|((b&0xff));
				p++;
			}
			cp7_cy_1+=cp7;
			cp1_cy_cp2+=cp1;
			cp4_cy_cp5+=cp4;
		}
		return;
	}	
}
class NyARPerspectiveReader_ANY_to_ANY extends NyARPerspectiveReaderBase
{
	private final int[] __pickFromRaster_rgb_tmp = new int[3];
	
	public boolean isCompatibleRaster(INyARRaster i_in_raster,INyARRaster i_out_raster)
	{
		return (i_in_raster instanceof INyARRgbRaster) && (i_out_raster instanceof INyARRgbRaster);
	}
	protected void onePixel(int pk_l,int pk_t,double[] cpara,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		final int[] rgb_tmp = this.__pickFromRaster_rgb_tmp;
		INyARRgbPixelDriver out_reader=o_out.getRgbPixelDriver();
		int in_w=i_in_raster.getWidth();
		int in_h=i_in_raster.getHeight();
		INyARRgbPixelDriver i_in_reader=i_in_raster.getRgbPixelDriver();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		double cp7_cy_1  =cp7*pk_t+1.0+cp6*pk_l;
		double cp1_cy_cp2=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4_cy_cp5=cp4*pk_t+cpara[5]+cp3*pk_l;
		
		for(int iy=0;iy<out_h;iy++){
			//解像度分の点を取る。
			double cp7_cy_1_cp6_cx  =cp7_cy_1;
			double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
			double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
			
			for(int ix=0;ix<out_w;ix++){
				//1ピクセルを作成
				final double d=1/(cp7_cy_1_cp6_cx);
				int x=(int)((cp1_cy_cp2_cp0_cx)*d);
				int y=(int)((cp4_cy_cp5_cp3_cx)*d);
				if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
				if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						
				i_in_reader.getPixel(x, y, rgb_tmp);
				cp7_cy_1_cp6_cx+=cp6;
				cp1_cy_cp2_cp0_cx+=cp0;
				cp4_cy_cp5_cp3_cx+=cp3;

				out_reader.setPixel(ix,iy,rgb_tmp);
			}
			cp7_cy_1+=cp7;
			cp1_cy_cp2+=cp1;
			cp4_cy_cp5+=cp4;
		}
		return;		
	}
	protected void multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		final int res_pix=i_resolution*i_resolution;

		final int[] rgb_tmp = this.__pickFromRaster_rgb_tmp;
		INyARRgbPixelDriver out_reader=o_out.getRgbPixelDriver();
		int in_w=i_in_raster.getWidth();
		int in_h=i_in_raster.getHeight();
		INyARRgbPixelDriver i_in_reader=i_in_raster.getRgbPixelDriver();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		for(int iy=out_h-1;iy>=0;iy--){
			//解像度分の点を取る。
			for(int ix=out_w-1;ix>=0;ix--){
				int r,g,b;
				r=g=b=0;
				int cy=pk_t+iy*i_resolution;
				int cx=pk_l+ix*i_resolution;
				double cp7_cy_1_cp6_cx_b  =cp7*cy+1.0+cp6*cx;
				double cp1_cy_cp2_cp0_cx_b=cp1*cy+cp2+cp0*cx;
				double cp4_cy_cp5_cp3_cx_b=cp4*cy+cp5+cp3*cx;
				for(int i2y=i_resolution-1;i2y>=0;i2y--){
					double cp7_cy_1_cp6_cx  =cp7_cy_1_cp6_cx_b;
					double cp1_cy_cp2_cp0_cx=cp1_cy_cp2_cp0_cx_b;
					double cp4_cy_cp5_cp3_cx=cp4_cy_cp5_cp3_cx_b;
					for(int i2x=i_resolution-1;i2x>=0;i2x--){
						//1ピクセルを作成
						final double d=1/(cp7_cy_1_cp6_cx);
						int x=(int)((cp1_cy_cp2_cp0_cx)*d);
						int y=(int)((cp4_cy_cp5_cp3_cx)*d);
						if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
						if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						
						i_in_reader.getPixel(x, y, rgb_tmp);
						r+=rgb_tmp[0];
						g+=rgb_tmp[1];
						b+=rgb_tmp[2];
						cp7_cy_1_cp6_cx+=cp6;
						cp1_cy_cp2_cp0_cx+=cp0;
						cp4_cy_cp5_cp3_cx+=cp3;
					}
					cp7_cy_1_cp6_cx_b+=cp7;
					cp1_cy_cp2_cp0_cx_b+=cp1;
					cp4_cy_cp5_cp3_cx_b+=cp4;
				}
				out_reader.setPixel(ix,iy,r/res_pix,g/res_pix,b/res_pix);
			}
		}
		return;		
	}
}
class NyARPerspectiveReader_ANY_to_INT1D_X8R8G8B8_32 extends NyARPerspectiveReaderBase
{
	private final int[] __pickFromRaster_rgb_tmp = new int[3];
	
	public boolean isCompatibleRaster(INyARRaster i_in_raster,INyARRaster i_out_raster)
	{
		return (i_in_raster instanceof INyARRgbRaster) && (i_out_raster.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
	}
	protected void onePixel(int pk_l,int pk_t,double[] cpara,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		assert(o_out.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
		final int[] rgb_tmp = this.__pickFromRaster_rgb_tmp;
		int[] pat_data=(int[])o_out.getBuffer();
		int in_w=i_in_raster.getWidth();
		int in_h=i_in_raster.getHeight();
		INyARRgbPixelDriver i_in_reader=i_in_raster.getRgbPixelDriver();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		double cp7_cy_1  =cp7*pk_t+1.0+cp6*pk_l;
		double cp1_cy_cp2=cp1*pk_t+cpara[2]+cp0*pk_l;
		double cp4_cy_cp5=cp4*pk_t+cpara[5]+cp3*pk_l;
		int p=0;
		for(int iy=out_h-1;iy>=0;iy--){
			//解像度分の点を取る。
			double cp7_cy_1_cp6_cx  =cp7_cy_1;
			double cp1_cy_cp2_cp0_cx=cp1_cy_cp2;
			double cp4_cy_cp5_cp3_cx=cp4_cy_cp5;
			
			for(int ix=out_w-1;ix>=0;ix--){
				//1ピクセルを作成
				final double d=1/(cp7_cy_1_cp6_cx);
				int x=(int)((cp1_cy_cp2_cp0_cx)*d);
				int y=(int)((cp4_cy_cp5_cp3_cx)*d);
				if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
				if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						
				i_in_reader.getPixel(x, y, rgb_tmp);
				cp7_cy_1_cp6_cx+=cp6;
				cp1_cy_cp2_cp0_cx+=cp0;
				cp4_cy_cp5_cp3_cx+=cp3;

				pat_data[p]=(rgb_tmp[0]<<16)|(rgb_tmp[1]<<8)|((rgb_tmp[2]&0xff));
				p++;
			}
			cp7_cy_1+=cp7;
			cp1_cy_cp2+=cp1;
			cp4_cy_cp5+=cp4;
		}
		return;		
	}
	
	protected void multiPixel(int pk_l,int pk_t,double[] cpara,int i_resolution,INyARRgbRaster i_in_raster,INyARRgbRaster o_out)throws NyARException
	{
		assert(o_out.isEqualBufferType(NyARBufferType.INT1D_X8R8G8B8_32));
		final int res_pix=i_resolution*i_resolution;

		final int[] rgb_tmp = this.__pickFromRaster_rgb_tmp;
		int[] pat_data=(int[])o_out.getBuffer();
		int in_w=i_in_raster.getWidth();
		int in_h=i_in_raster.getHeight();
		INyARRgbPixelDriver i_in_reader=i_in_raster.getRgbPixelDriver();

		//ピクセルリーダーを取得
		double cp0=cpara[0];
		double cp3=cpara[3];
		double cp6=cpara[6];
		double cp1=cpara[1];
		double cp4=cpara[4];
		double cp7=cpara[7];
		double cp2=cpara[2];
		double cp5=cpara[5];
		
		int out_w=o_out.getWidth();
		int out_h=o_out.getHeight();
		int p=(out_w*out_h-1);
		for(int iy=out_h-1;iy>=0;iy--){
			//解像度分の点を取る。
			for(int ix=out_w-1;ix>=0;ix--){
				int r,g,b;
				r=g=b=0;
				int cy=pk_t+iy*i_resolution;
				int cx=pk_l+ix*i_resolution;
				double cp7_cy_1_cp6_cx_b  =cp7*cy+1.0+cp6*cx;
				double cp1_cy_cp2_cp0_cx_b=cp1*cy+cp2+cp0*cx;
				double cp4_cy_cp5_cp3_cx_b=cp4*cy+cp5+cp3*cx;
				for(int i2y=i_resolution-1;i2y>=0;i2y--){
					double cp7_cy_1_cp6_cx  =cp7_cy_1_cp6_cx_b;
					double cp1_cy_cp2_cp0_cx=cp1_cy_cp2_cp0_cx_b;
					double cp4_cy_cp5_cp3_cx=cp4_cy_cp5_cp3_cx_b;
					for(int i2x=i_resolution-1;i2x>=0;i2x--){
						//1ピクセルを作成
						final double d=1/(cp7_cy_1_cp6_cx);
						int x=(int)((cp1_cy_cp2_cp0_cx)*d);
						int y=(int)((cp4_cy_cp5_cp3_cx)*d);
						if(x<0){x=0;}else if(x>=in_w){x=in_w-1;}
						if(y<0){y=0;}else if(y>=in_h){y=in_h-1;}
						
						i_in_reader.getPixel(x, y, rgb_tmp);
						r+=rgb_tmp[0];
						g+=rgb_tmp[1];
						b+=rgb_tmp[2];
						cp7_cy_1_cp6_cx+=cp6;
						cp1_cy_cp2_cp0_cx+=cp0;
						cp4_cy_cp5_cp3_cx+=cp3;
					}
					cp7_cy_1_cp6_cx_b+=cp7;
					cp1_cy_cp2_cp0_cx_b+=cp1;
					cp4_cy_cp5_cp3_cx_b+=cp4;
				}
				r/=res_pix;
				g/=res_pix;
				b/=res_pix;
				pat_data[p]=((r&0xff)<<16)|((g&0xff)<<8)|((b&0xff));
				p--;
			}
		}
		return;		
	}
}




