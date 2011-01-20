package jp.nyatla.nyartoolkit.core.rasterfilter.gs2bin;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * このクラスは、固定敷居値を用いて、画像を２値化します。
 * 入力できるラスタ、出力できるラスタの形式は以下の通りです。
 * <ul>
 * <li> 入力形式 - {@link NyARBufferType#INT1D_GRAY_8}
 * <li> 出力形式 - {@link NyARBufferType#INT1D_BIN_8}
 * </ul>
 */
public class NyARRasterFilter_ConstantThreshold implements INyARRasterFilter_Gs2Bin
{
	/** 敷居値。*/
	protected int _threshold;
	/**
	 * コンストラクタです。
	 * 固定式位置の初期値、入力、出力ラスタの画素形式を指定して、フィルタを作成します。
	 * @param i_initial_threshold
	 * 敷居値の初期値です。0&lt;n&lt;256の値を指定します。
	 * @param i_in_raster_type
	 * 入力ラスタの形式です。
	 * @param i_out_raster_type
	 * 出力ラスタの形式です。
	 * @throws NyARException
	 */
	public NyARRasterFilter_ConstantThreshold(int i_initial_threshold,int i_in_raster_type,int i_out_raster_type) throws NyARException
	{
		assert(i_in_raster_type==NyARBufferType.INT1D_GRAY_8);
		assert(i_out_raster_type==NyARBufferType.INT1D_BIN_8);
		//初期化
		this._threshold=i_initial_threshold;
		
	}
	/**
	 * コンストラクタです。
	 * 入力ラスタ形式={@link NyARBufferType#INT1D_GRAY_8},出力ラスタ形式={@link NyARBufferType#INT1D_BIN_8},
	 * 初期敷居値=0でインスタンスを生成します。
	 * @throws NyARException
	 */
	public NyARRasterFilter_ConstantThreshold() throws NyARException
	{
		this._threshold=0;
	}

	/**
	 * この関数は、敷居値をセットします。
	 * @param i_threshold
	 * セットする敷居値。0以上、256未満である事。
	 */
	public void setThreshold(int i_threshold)
	{
		this._threshold = i_threshold;
	}
	/**
	 * この関数は、固定敷居値で画像を２値化します。
	 * 現在の敷居値以下の画素が0になり、その他の画素は1になります。
	 */
	public void doFilter(NyARGrayscaleRaster i_input, NyARBinRaster i_output) throws NyARException
	{
		assert(i_input.getBufferType()==NyARBufferType.INT1D_GRAY_8);
		assert(i_output.getBufferType()==NyARBufferType.INT1D_BIN_8);
		int[] out_buf = (int[]) i_output.getBuffer();
		int[] in_buf = (int[]) i_input.getBuffer();
		NyARIntSize s=i_input.getSize();
		
		final int th=this._threshold;
		int bp =s.w*s.h-1;
		final int pix_count   =s.h*s.w;
		final int pix_mod_part=pix_count-(pix_count%8);
		for(bp=pix_count-1;bp>=pix_mod_part;bp--){
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
		}
		//タイリング
		for (;bp>=0;) {
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
			out_buf[bp]=(in_buf[bp] & 0xff)<=th?0:1;
			bp--;
		}
		return;			
	}
}
