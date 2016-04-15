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
package jp.nyatla.nyartoolkit.core.surfacetracking.rasterdriver;


import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARTemplatePatchImage;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;


/**
 * INyARGrayscaleRasterのテンプレート検索ドライバ
 * @author nyatla
 *
 */
public class NyARTemplateMatchingDriver_ANY implements INyARTemplateMatchingDriver
{
//	private final static int AR2_DEFAULT_SEARCH_SIZE = 25;
	private final static int AR2_DEFAULT_SEARCH_SIZE = 12;
	private INyARGrayscaleRaster _i_ref_raster;
	private byte[] _mbuf;
	public NyARTemplateMatchingDriver_ANY(INyARGrayscaleRaster i_ref_raster)
	{
		assert(i_ref_raster.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		this._i_ref_raster = i_ref_raster;
		this._mbuf=new byte[i_ref_raster.getWidth()*i_ref_raster.getHeight()];
		this._search_area.x=AR2_DEFAULT_SEARCH_SIZE;
		this._search_area.y=AR2_DEFAULT_SEARCH_SIZE;		
	}
	public final static int SKIP_INTERVAL = 3;
	public final static int KEEP_NUM = 3;

	/**
	 * ワークエリアの初期化
	 */
	private void initWorkArea(int i_l,int i_t,int i_r,int i_b)
	{
		int w=this._i_ref_raster.getWidth();
		byte[] b=this._mbuf;
		for (int j = i_t; j <= i_b; j++) {
			// pmf = &mfImage[j*xsize+sx];
			int i=i_r-i_l;
			int ptr=w*j+i_l;
			for (; i >=8; i-=8) {
				b[ptr]=b[ptr+1]=b[ptr+2]=b[ptr+3]=b[ptr+4]=b[ptr+5]=b[ptr+6]=b[ptr+7]=0;
				ptr+=8;
			}
			for (; i >=0; i--) {
				b[ptr]=0;
				ptr++;
			}
		}		
	}
	NyARIntPoint2d _search_area=new NyARIntPoint2d();
	/**
	 * 検索ウインドウの範囲を指定する。
	 * @param i_px
	 * @param i_py
	 */
	public void setSearchArea(int i_x,int i_y)
	{
		this._search_area.x=i_x;
		this._search_area.y=i_y;
	}
	/**
	 * n個の候補点ログを取るクラス。
	 * ARToolkitのupdateCandidate関数由来。
	 */
	private class MatchingCandidateList
	{
		public class Item{
			public int x;
			public int y;
			public int val;
		}
		/**
		 * valの高い順にnum_of_item個の値を保管する。wvalの大きさは[0]>[n]
		 */
		public Item[] items;
		/**
		 * 有効なitemsの数
		 */
		public int num_of_item;
		public MatchingCandidateList(int i_num_of_keep)
		{
			this.items=new Item[i_num_of_keep];
			for(int i=0;i<i_num_of_keep;i++){
				this.items[i]=new Item();
			}
		}
		/**
		 * ロガーを初期化する。
		 */
		public void init()
		{
			this.num_of_item=0;
		}
		/**
		 * wval昇順で候補点の追加を試行
		 * i_valが大きい方がえらい
		 * @throws NyARException 
		 */
		public boolean tryToAdd(int i_x, int i_y,int i_val)
		{
			Item[] items=this.items;
			int num=this.num_of_item;
			//ログ数0
			if(num==0){
				this.items[0].x=i_x;
				this.items[0].y=i_y;
				this.items[0].val=i_val;
				this.num_of_item=1;
				return true;
			}
			//最小値が候補よりも小さければ単純な追加
			if(items[num-1].val>=i_val){
				if(this.items.length>num){
					this.items[num].x=i_x;
					this.items[num].y=i_y;
					this.items[num].val=i_val;
					this.num_of_item++;
				}
				return false;
			}
			//最大値が候補よりも小さければ0番に挿入
			if(items[0].val<i_val){
				//シフト
				Item tmp=items[this.items.length-1];
				for(int i2=this.items.length-1;i2>0;i2--){
					items[i2]=items[i2-1];
				}
				items[0]=tmp;
				tmp.x=i_x;
				tmp.y=i_y;
				tmp.val=i_val;
				if(this.items.length>num){
					this.num_of_item++;
				}
				return true;
			}
			
			//前方から挿入処理
			for(int i=0;i<num;i++){
				if(items[i].val<i_val){
					//挿入処理
					Item tmp=items[this.items.length-1];
					for(int i2=this.items.length-1;i2>=i+1;i2--){
						items[i2]=items[i2-1];
					}
					items[i]=tmp;
					tmp.x=i_x;
					tmp.y=i_y;
					tmp.val=i_val;
					if(this.items.length>num){
						this.num_of_item++;
					}
					return true;
				}
			}
			throw new NyARRuntimeException();
		}
	}
	/**
	 * 候補点のログ取り用
	 */
	private MatchingCandidateList __ml=new MatchingCandidateList(3);
	/**
	 * N個の基準点から、最もテンプレートに一致した座標を返却する。
	 * 検索範囲は、{@link #setSearchArea}で与えたpx,pyについて、xn+i_px>=xn>=xn-i_px,yn+i_py>=yn>=yn-i_pyの矩形範囲。
	 * i_pointsそれぞれについて検索する。
	 * @param i_template
	 * 探索範囲。単三区店を中心に、
	 * @param ry
	 * @param i_points
	 * 検索する座標セット。(近い場所の場合に、同一条件の探索をキャンセルできる？)
	 * @param o_obs_point
	 * 観察座標系での一致点。returnが0の場合は無効。
	 * @return
	 * 一致率(値範囲調査中)
	 * 0の場合は一致せず。
	 * @throws NyARException
	 */
	public double ar2GetBestMatching(NyARTemplatePatchImage i_template, NyARIntPoint2d[] i_points,int i_number_of_point,
			NyARDoublePoint2d o_obs_point)
	{
		//最大テンプレートサイズの制限
		assert(i_template.xsize*i_template.ysize<100*100);
//		int yts1, yts2;
		int wval2;
		int i, j, l;
		int ii;
		int ret;

		NyARIntSize s = this._i_ref_raster.getSize();
		int yts = i_template.yts;
		int xts = i_template.xts;
		INyARGrayscaleRaster raster=this._i_ref_raster;
		
		

		//パッチの探索
		ret = 1;
		int sw=this._search_area.x;
		int sh=this._search_area.y;
		//パッチエリアの初期化
		for (ii = i_number_of_point-1; ii>=0; ii--) {
			if (i_points[ii].y < 0) {
				break;
			}
			// 検索するパッチ中心を決定
			int px = (i_points[ii].x / (SKIP_INTERVAL + 1)) * (SKIP_INTERVAL + 1) + (SKIP_INTERVAL + 1) / 2;
			int py = (i_points[ii].y / (SKIP_INTERVAL + 1)) * (SKIP_INTERVAL + 1) + (SKIP_INTERVAL + 1) / 2;
			//検索範囲を画面内に制限
			int search_left = px - sw;
			if (search_left < 0)
				search_left = 0;
			int search_right = px + sw;
			// if( ex >= xsize ) ex = xsize-1;
			if (search_right >= s.w) {
				search_right = s.w - 1;
			}
			int search_top = py - sh;
			if (search_top < 0) {
				search_top = 0;
			}
			int search_bottom = py + sh;
			if (search_bottom >= s.h) {
				search_bottom = s.h - 1;
			}
			//利用するパッチエリアの初期化
			initWorkArea(search_left,search_top,search_right,search_bottom);

		}
		MatchingCandidateList ml=this.__ml;
		ml.init();
		
		for (ii = i_number_of_point-1; ii>=0; ii--) {
			if (i_points[ii].x < 0) {
				// if( ret ){
				if (ret != 0) {
					return -1;
				} else {
					break;
				}
			}
			int px = (i_points[ii].x / (SKIP_INTERVAL + 1)) * (SKIP_INTERVAL + 1) + (SKIP_INTERVAL + 1) / 2;
			int py = (i_points[ii].y / (SKIP_INTERVAL + 1)) * (SKIP_INTERVAL + 1) + (SKIP_INTERVAL + 1) / 2;

			for (j = py - sh; j <= py + sh; j += SKIP_INTERVAL + 1) {
				if (j - yts * NyARTemplatePatchImage.AR2_TEMP_SCALE < 0) {
					continue;
				}
				// if( j + yts2*AR2_TEMP_SCALE >= ysize ){
				if (j + yts * NyARTemplatePatchImage.AR2_TEMP_SCALE >= s.h) {
					break;
				}
				for (i = px - sw; i <= px + sw; i += SKIP_INTERVAL + 1) {
					if (i - xts * NyARTemplatePatchImage.AR2_TEMP_SCALE < 0) {
						continue;
					}
					// if( i + mtemp.xts2*AR2_TEMP_SCALE >= xsize ){
					if (i + xts * NyARTemplatePatchImage.AR2_TEMP_SCALE >= s.w) {
						break;
					}
					// 既に検出済のエリア？
					if (this._mbuf[i+j*s.w] != 0) {
						// mfImage[j*xsize+i] ){
						continue;
					}
					this._mbuf[i+j*s.w]=1;//ii番目のパッチで検索済みをマーク
					int wval = ar2GetBestMatchingSubFine(raster,i_template, i, j);
					if (wval <= 0) {
						continue;
					}
					//ログへ追加
					ml.tryToAdd(i, j,wval);
					ret = 0;
				}
			}
		}

		double ret_sim=0;
		//一番スコアの良いパッチを得る
		wval2 = 0;
		ret = -1;
		for (l = ml.num_of_item-1; l>=0; l--) {
			for (j = ml.items[l].y - SKIP_INTERVAL; j <= ml.items[l].y + SKIP_INTERVAL; j++) {
				if (j - i_template.yts * NyARTemplatePatchImage.AR2_TEMP_SCALE < 0) {
					continue;
				}
				// if( j+mtemp.yts2*AR2_TEMP_SCALE >= ysize ){
				if (j + i_template.yts * NyARTemplatePatchImage.AR2_TEMP_SCALE >= s.h) {
					break;
				}
				for (i = ml.items[l].x - SKIP_INTERVAL; i <= ml.items[l].x + SKIP_INTERVAL; i++) {
					if (i - xts * NyARTemplatePatchImage.AR2_TEMP_SCALE < 0) {
						continue;
					}
					// if( i+mtemp.xts2*AR2_TEMP_SCALE >= xsize ){
					if (i + xts * NyARTemplatePatchImage.AR2_TEMP_SCALE >= s.w) {
						break;
					}
					int wval = ar2GetBestMatchingSubFine(raster,i_template, i, j);
					if (wval<=0) {
						continue;
					}
					if (wval> wval2) {
						o_obs_point.x = i;
						o_obs_point.y = j;
						wval2 = wval;
						ret_sim = (double) wval / 10000;
						ret = 0;
					}
				}
			}
		}
		return ret_sim;
	}

	/**
	 * 
	 * @param mtemp
	 * @param sx
	 * @param sy
	 * @param 評価点。エラーの場合0
	 * @return
	 * @throws NyARException 
	 */
	private static int ar2GetBestMatchingSubFine(INyARGrayscaleRaster i_raster,NyARTemplatePatchImage mtemp, int sx, int sy)
	{
		System.err.println("This function is not tested! Check accury of result  before using.");
		int[] tmp_buf = mtemp.img;
		int sum2=0;
		int sum1=0;
		int sum3=0;
		int t_ptr = 0;
		for (int j = mtemp.ysize-1; j>=0; j--) {
			int i=mtemp.xsize-1;
			for (; i>=0; i--) {
				int tn=tmp_buf[t_ptr];
				if (tn != NyARTemplatePatchImage.AR2_TEMPLATE_NULL_PIXEL) {
					int sn=i_raster.getPixel(
							(sx+(i-mtemp.xts) * NyARTemplatePatchImage.AR2_TEMP_SCALE),
							(sy+(j-mtemp.yts) * NyARTemplatePatchImage.AR2_TEMP_SCALE));
					sum2+=sn*sn;
					sum1+=sn;
					sum3+=tn*sn;
				}
				t_ptr++;
			}
		}
		sum3 -= sum1 * mtemp.sum_of_img / mtemp.valid_pixels;
		int vlen=sum2-sum1*sum1/mtemp.valid_pixels;
		if (vlen == 0){
			return 0;
		} else {
			return sum3 * 100 / mtemp.vlen * 100 / (int) Math.sqrt(vlen);
		}
		
	}		
}