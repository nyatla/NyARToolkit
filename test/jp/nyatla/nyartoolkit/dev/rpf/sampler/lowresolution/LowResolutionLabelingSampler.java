package jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution;

import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARLabeling_Rle;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.raster.*;


/**
 * 画像データのサンプラです。画像データから、輪郭線抽出のヒントを計算して、出力コンテナに格納します。
 * 入力-LowResolutionLabelingSamplerIn
 * 出力-LowResolutionLabelingSamplerOut
 */
public class LowResolutionLabelingSampler
{
	/**
	 * 解像度を変換してラべリングするクラス。
	 * 入力-Raw GrayScale
	 * 出力-ピラミッド画像,輪郭エントリーポイント
	 * @note
	 * 将来的に最低解像度値(i_depth)をサポートする可能性あり。現在はi_depth=4で固定
	 */
	class Main_Labeling extends NyARLabeling_Rle
	{
		public NyARGrayscaleRaster current_gs;
		public LowResolutionLabelingSamplerOut current_output;
		public Main_Labeling(int i_width,int i_height) throws NyARException
		{
			super(i_width,i_height);
		}
		/**
		 * @Override
		 */
		protected void onLabelFound(NyARRleLabelFragmentInfo iRefLabel)throws NyARException
		{
			//widthとheightの計算
			int w=iRefLabel.clip_r-iRefLabel.clip_l;
			int h=iRefLabel.clip_b-iRefLabel.clip_t;
			//1*1(1bitPixelの5*5)以下の場合は、検出不能
			//未実装部分:2*2(1bitPixelの8*8)以下の場合は、解像度1で再検出
			//未実装部分:3*3,4*4(1bitPixelの12*12,16*16)以下の場合は、解像度2で再検出
			if(w<5 || h<5){
				//今のところは再検出機構なし。
				return;
			}
			LowResolutionLabelingSamplerOut.Item item=current_output.prePush();
			if(item==null){
				return;
			}
			item.entry_pos.x=iRefLabel.entry_x;
			item.entry_pos.y=iRefLabel.clip_t;
			item.ref_raster=this.current_gs;
			item.base_area.x=iRefLabel.clip_l*4;
			item.base_area.y=iRefLabel.clip_t*4;
			item.base_area.w=w*4;
			item.base_area.h=h*4;
			item.base_area_sq_diagonal=(w*w+h*h)*(4*4);
		}
		
	}
	private Main_Labeling _main_labeling;
	public LowResolutionLabelingSampler(int i_width,int i_height) throws NyARException
	{
		this._main_labeling=new Main_Labeling(i_width,i_height);
	}
	/**
	 * i_inのデータをサンプリングして、o_outにサンプル値を作成します。この関数は、o_outを初期化します。
	 * @param i_in
	 * 入力元のデータです。
	 * @param o_out
	 * 出力先のデータです。
	 * @throws NyARException
	 */
	public void Sampling(LowResolutionLabelingSamplerIn i_in,LowResolutionLabelingSamplerOut o_out) throws NyARException
	{
		//ラスタを取得(Depth3=2^2解像度のデータ)
		NyARGrayscaleRaster raster4=i_in.getRasterByDepth(3);
		int th=1;
		//クラスのパラメータ初期化
		this._main_labeling.current_gs=raster4;
		this._main_labeling.current_output=o_out;

		//パラメータの設定
		o_out.initializeParams(i_in);
		//ラべリング
		this._main_labeling.labeling(raster4,th);
	}
}







