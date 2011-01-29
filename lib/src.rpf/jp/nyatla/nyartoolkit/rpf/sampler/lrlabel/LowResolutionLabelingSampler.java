package jp.nyatla.nyartoolkit.rpf.sampler.lrlabel;

import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.*;





/**
 * 画像データのサンプラです。画像データから、輪郭線抽出のヒントを計算して、出力コンテナに格納します。
 * 入力-LowResolutionLabelingSamplerIn
 * 出力-LowResolutionLabelingSamplerOut
 */
/**
 * このクラスは、{@link NyARTracker}が使う、ラべリングクラスです。
 * 1/2^nに縮尺した画像をラべリングして、元画像の解像度でラベルデータを返します。
 * このクラスに入力されるデータは縮尺されたエッジ画像のため、その為のパラメータ調整がしてあります。
 */
public class LowResolutionLabelingSampler
{
	/**
	 * 1/n画像のラべリングをするクラス。
	 */
	private class Main_Labeling extends NyARLabeling_Rle
	{
		private int _pix;
		public int current_th;
		public LowResolutionLabelingSamplerOut current_output;
		public Main_Labeling(int i_width,int i_height,int i_pix_base) throws NyARException
		{
			super(i_width,i_height);
			this._pix=i_pix_base;
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
			if(w<10 || h<10){
				//今のところは再検出機構なし。
				return;
			}
			LowResolutionLabelingSamplerOut.Item item=current_output.prePush();
			if(item==null){
				return;
			}
			int pix=this._pix;
			item.entry_pos.x=iRefLabel.entry_x;
			item.entry_pos.y=iRefLabel.clip_t;
			item.base_area.x=iRefLabel.clip_l*pix;
			item.base_area.y=iRefLabel.clip_t*pix;
			item.base_area.w=w*pix;
			item.base_area.h=h*pix;
			item.base_area_center.x=item.base_area.x+item.base_area.w/2;
			item.base_area_center.y=item.base_area.y+item.base_area.h/2;
			item.base_area_sq_diagonal=(w*w+h*h)*(pix*pix);
			item.lebeling_th=this.current_th;
		}
		
	}
	private Main_Labeling _main_labeling;
	/**
	 * コンストラクタです。
	 * 入力画像解像度と、サンプリングパラメータを指定して、インスタンスを初期化します。
	 * @param i_width
	 * サンプリングするラスタの解像度
	 * @param i_height
	 * サンプリングするラスタの解像度
	 * @param i_pix_size
	 * 座標系の倍率係数。
	 * 例えば1/2画像(面積1/4)のサンプリング結果を元画像サイズに戻すときは、2を指定する。
	 * @throws NyARException
	 */
	public LowResolutionLabelingSampler(int i_width,int i_height,int i_pix_size) throws NyARException
	{
		this._main_labeling=new Main_Labeling(i_width/i_pix_size,i_height/i_pix_size,i_pix_size);
	}
	/**
	 * この関数は、入力ラスタをサンプリングして、o_outにラベル情報を出力します。
	 * @param i_in
	 * 入力元のデータです。
	 * @param i_th
	 * ラべリングの敷居値です。
	 * @param o_out
	 * 出力先のデータです。
	 * オブジェクトのデータは、初期化されます。
	 * @throws NyARException
	 */
	public void sampling(NyARGrayscaleRaster i_in,int i_th,LowResolutionLabelingSamplerOut o_out) throws NyARException
	{
		//クラスのパラメータ初期化
		Main_Labeling lb=this._main_labeling;
		lb.current_output=o_out;
		lb.current_th=i_th;


		//パラメータの設定
		o_out.initializeParams();
		//ラべリング
		lb.setAreaRange(10000,3);
		lb.labeling(i_in,i_th);
	}
}