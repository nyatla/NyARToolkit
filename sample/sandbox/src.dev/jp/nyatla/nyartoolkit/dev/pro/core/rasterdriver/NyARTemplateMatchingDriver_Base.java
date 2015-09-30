package jp.nyatla.nyartoolkit.dev.pro.core.rasterdriver;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.NyARTemplatePatchImage;

/**
 * INyARGrayscaleRasterの�?ンプレート検索ドライ�?
 * @author nyatla
 *
 */
public class NyARTemplateMatchingDriver_Base implements INyARTemplateMatchingDriver
{
	private final static int AR2_DEFAULT_SEARCH_SIZE = 25;
	private INyARGrayscaleRaster _i_ref_raster;
	private byte[] _mbuf;
	public NyARTemplateMatchingDriver_Base(INyARGrayscaleRaster i_ref_raster)
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
	 * ワークエリアの初期�?
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
	 * 検索ウインドウの�?囲を指定する�??
	 * @param i_px
	 * @param i_py
	 */
	public void setSearchArea(int i_x,int i_y)
	{
		this._search_area.x=i_x;
		this._search_area.y=i_y;
	}
	/**
	 * n個�?�候補点ログを取るクラス�?
	 * ARToolkitのupdateCandidate関数由来�?
	 */
	private class MatchingCandidateList
	{
		public class Item{
			public int x;
			public int y;
			public int val;
		}
		/**
		 * valの高い�?にnum_of_item個�?�値を保管する。wvalの大きさは[0]>[n]
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
		 * ロガーを�?�期化する�??
		 */
		public void init()
		{
			this.num_of_item=0;
		}
		/**
		 * wval�?�?で候補点の追�?を試�?
		 * i_valが大きい方がえらい
		 * @throws NyARRuntimeException 
		 */
		public boolean tryToAdd(int i_x, int i_y,int i_val) throws NyARRuntimeException
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
			//�?小�?�が�?�補よりも小さければ単純な追�?
			if(items[num-1].val>=i_val){
				if(this.items.length>num){
					this.items[num].x=i_x;
					this.items[num].y=i_y;
					this.items[num].val=i_val;
					this.num_of_item++;
				}
				return false;
			}
			//�?大値が�?�補よりも小さければ0番に挿入
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
			
			//前方から挿入処�?
			for(int i=0;i<num;i++){
				if(items[i].val<i_val){
					//挿入処�?
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
	 * 100x100以上�?�パッチ�?�?��ﾘです�??
	 */
	private int[] wimg1=new int[100*100];
	private int[] wimg2=new int[100*100];	
	/**
	 * N個�?�基準点から、最もテンプレートに�?致した座標を返却する�?
	 * 検索�?囲は、{@link #setSearchArea}で与えたpx,pyにつ�?て、xn+i_px>=xn>=xn-i_px,yn+i_py>=yn>=yn-i_pyの矩形�?囲�?
	 * i_pointsそれぞれにつ�?て検索する�?
	 * @param i_template
	 * 探索�?囲。単三区店を中�?に�?
	 * @param ry
	 * @param i_points
	 * 検索する座標セ�?ト�??(近い場�?の場合に、同�?条件の探索をキャンセルできる?�?)
	 * @param o_obs_point
	 * 観察座標系での�?致点。return�?0の場合�?�無効�?
	 * @return
	 * �?致�?(値�?囲調査中)
	 * 0の場合�?��?致せず�?
	 * @throws NyARRuntimeException
	 */
	public double ar2GetBestMatching(NyARTemplatePatchImage i_template, NyARIntPoint2d[] i_points,int i_number_of_point,
			NyARDoublePoint2d o_obs_point) throws NyARRuntimeException
	{
		//�?大�?ンプレートサイズの制�?
		assert(i_template.xsize*i_template.ysize<100*100);
		int wval2;
		int i, j, l;
		int ii;
		int ret;

		NyARIntSize s = this._i_ref_raster.getSize();
		int yts = i_template.yts;
		int xts = i_template.xts;

		
		

		//パッチ�?�探索
		ret = 1;
		int sw=this._search_area.x;
		int sh=this._search_area.y;
		//パッチエリアの初期�?
		for (ii = i_number_of_point-1; ii>=0; ii--) {
			if (i_points[ii].y < 0) {
				break;
			}
			// 検索するパッチ中�?を決�?
			int px = (i_points[ii].x / (SKIP_INTERVAL + 1)) * (SKIP_INTERVAL + 1) + (SKIP_INTERVAL + 1) / 2;
			int py = (i_points[ii].y / (SKIP_INTERVAL + 1)) * (SKIP_INTERVAL + 1) + (SKIP_INTERVAL + 1) / 2;
			//検索�?囲を画面�?に制�?
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
			//利用するパッチエリアの初期�?
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
					// 既に検�?�済�?�エリア?�?
					if (this._mbuf[i+j*s.w] != 0) {
						// mfImage[j*xsize+i] ){
						continue;
					}
					this._mbuf[i+j*s.w]=1;//ii番目のパッチで検索済みを�?��?�ク
					int wval = this.ar2GetBestMatchingSubFine(i_template, i, j);
					if (wval <= 0) {
						continue;
					}
					//ログへ追�?
					ml.tryToAdd(i, j,wval);
					ret = 0;
				}
			}
		}

		double ret_sim=0;
		//�?番スコアの良�?パッチを得る
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
					int wval = this.ar2GetBestMatchingSubFine(i_template, i, j);
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
	 * @param 評価点。エラーの場�?0
	 * @return
	 */
	private int ar2GetBestMatchingSubFine(NyARTemplatePatchImage mtemp, int sx, int sy)
	{
		int xsize = this._i_ref_raster.getWidth();
		int[] p11 = mtemp.img;
		int p11_ptr = 0;
		int[] p1 = this.wimg1;
		int p1_ptr = 0;
		int[] p3 = this.wimg2;
		int p3_ptr = 0;
		int ave = 0;
		int k = 0;
		int[] p2 = (int[]) this._i_ref_raster.getBuffer();
		for (int j = -(mtemp.yts); j <= mtemp.yts; j++) {
			int p2_ptr = ((sy + j * NyARTemplatePatchImage.AR2_TEMP_SCALE) * xsize + sx - mtemp.xts * NyARTemplatePatchImage.AR2_TEMP_SCALE);
			for (int i = -(mtemp.xts); i <= mtemp.xts; i++) {
				if (p11[p11_ptr] != NyARTemplatePatchImage.AR2_TEMPLATE_NULL_PIXEL) {
					int w = p2[p2_ptr];// w = *(p2+0);// + *(p2+1) + *(p2+2);
					ave += w;
					p3[p3_ptr] = w;
					p3_ptr++;

					p1[p1_ptr] = p11[p11_ptr];
					p1_ptr++;
					k++;
				}
				p2_ptr += NyARTemplatePatchImage.AR2_TEMP_SCALE;
				p11_ptr++;
			}
		}
		if (k == 0) {
			return 0;
		}
		ave /= k;

//		p1 = this.wimg1;
//		p3 = this.wimg2;
		int wval = 0;
		int vlen = 0;
		p1_ptr = 0;
		p3_ptr = 0;
		for (int i = 0; i < k; i++) {
			int w = p3[p3_ptr] - ave;
			p3_ptr++;
			vlen += w * w;
			wval += p1[p1_ptr] * w;
			p1_ptr++;
		}
		if (vlen == 0) {
			return 0;
		} else {
			return wval * 100 / mtemp.vlen * 100 / (int) Math.sqrt(vlen);
		}
	}
}