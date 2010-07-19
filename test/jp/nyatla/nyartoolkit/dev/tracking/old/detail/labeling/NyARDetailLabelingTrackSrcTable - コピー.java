package jp.nyatla.nyartoolkit.dev.tracking.detail.labeling;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.NyARLabelOverlapChecker;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.*;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.squaredetect.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.dev.tracking.detail.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.NyARRasterFilter_ARToolkitThreshold;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.*;
import jp.nyatla.nyartoolkit.core.analyzer.raster.NyARRasterAnalyzer_Histogram;


public class NyARDetailLabelingTrackSrcTable extends NyARDetailTrackSrcTable
{	
	
	/**
	 *	領域分割ラべリングをするクラス
	 *
	 */
	public class SquareDetector
	{
		private final int _width;
		private final int _height;

		private final NyARLabelOverlapChecker<NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo> _overlap_checker = new NyARLabelOverlapChecker<NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo>(32,NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo.class);
		private final NyARRleLabelFragmentInfoStack _stack;
		private final NyARCoord2SquareVertexIndexes _coord2vertex=new NyARCoord2SquareVertexIndexes();
		private final NyARContourPickup _cpickup=new NyARContourPickup();
		
		private final int _max_coord;
		private final NyARIntPoint2d[] _coord;
		private NyARRasterFilter_ARToolkitThreshold _rgb2bin;
		private NyARLabeling_Rle _labeling;
		
		
		private NyARRasterAnalyzer_Histogram _histogram_analyzer;
		
		
		//この辺は排他利用してもOK		
		private NyARHistogram _histogram;
		private NyARHistogramAnalyzer_DiscriminantThreshold _threshold_detector;
		private NyARBinRaster _bin;
		private NyARCoord2Linear _coordline;
		
		
		/**
		 * 最大i_squre_max個のマーカーを検出するクラスを作成する。
		 * 
		 * @param i_param
		 */
		public SquareDetector(NyARIntSize i_size,NyARCameraDistortionFactor i_distfactor_ref,int i_raster_type) throws NyARException
		{
			this._width = i_size.w;
			this._height = i_size.h;
			//ラベリングのサイズを指定したいときはsetAreaRangeを使ってね。
			this._stack=new NyARRleLabelFragmentInfoStack(i_size.w*i_size.h*2048/(320*240)+32);//検出可能な最大ラベル数
			this._coordline=new NyARCoord2Linear(i_size,i_distfactor_ref);
			

			// 輪郭の最大長は画面に映りうる最大の長方形サイズ。
			int number_of_coord = (this._width + this._height) * 2;

			// 輪郭バッファ
			this._max_coord = number_of_coord;
			this._coord = NyARIntPoint2d.createArray(number_of_coord);

			this._histogram_analyzer=new NyARRasterAnalyzer_Histogram(i_raster_type,4);
			this._histogram=new NyARHistogram(256);
			this._threshold_detector=new NyARHistogramAnalyzer_DiscriminantThreshold();
			this._bin=new NyARBinRaster(i_size.w,i_size.h);
			return;
		}
		/**
		 * RGBソースを、i_itemを元に領域分割してラべリングする。
		 * @param i_raster
		 * @param i_item
		 * @param i_number
		 */
		public void initLabelingStack(INyARRgbRaster i_raster,NyARDetailLabelingTrackItem[] i_item,int i_number,NyARBinRaster o_bin,NyARRleLabelFragmentInfoStack o_flagment) throws NyARException
		{
			NyARIntRect rect=new NyARIntRect();

			NyARRasterFilter_ARToolkitThreshold ave=this._rgb2bin;
			
			o_flagment.clear();//ラベルスタックのリセット
			for(int i=i_number-1;i>=0;i--){
				rect.wrapVertex(i_item[i].estimate.ideal_vertex,4);
				
				//エリアを1.5倍
				int w=rect.w;
				int h=rect.h;
				rect.x-=w-w/2;
				rect.y-=h-h/2;
				rect.w+=w;
				rect.h+=h;
				//境界値制限
				rect.clip(0,0,319,239);
				//領域指定のヒストグラム抽出
				int skip=rect.w*rect.h/4096;
				this._histogram_analyzer.setVerticalInterval(skip<1?1:skip);
				this._histogram_analyzer.analyzeRaster(i_raster,rect,this._histogram);
				int th=this._threshold_detector.getThreshold(this._histogram);
				//領域指定のBIN化
				ave.setThreshold(th);
				ave.doFilter(i_raster,rect, o_bin);
				//領域指定のラべリングと、スタックへの蓄積
				this._labeling.labeling(o_bin,rect,o_flagment);
			}
			
		}

		private final int[] __detectMarker_mkvertex = new int[4];
		private NyARIntPoint2d[] __ref_vertex=new NyARIntPoint2d[4];
		public void detectMarker(INyARRgbRaster i_raster,NyARDetailLabelingTrackItem[] i_item,int i_number,NyARDetailLabelingTrackSrcTable o_table) throws NyARException
		{
			NyARBinRaster bin=this._bin;
			final NyARRleLabelFragmentInfoStack flagment=this._stack;
			final NyARLabelOverlapChecker<NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo> overlap = this._overlap_checker;

			//labelの準備
			initLabelingStack(i_raster,i_item,i_number,bin,flagment);
			int label_num=flagment.getLength();
			// ラベル数が0ならここまで
			if (label_num < 1) {
				return;
			}
			//ラベルをソートしておく
			flagment.sortByArea();

			//取得準備
			final int xsize = this._width;
			final int ysize = this._height;
			NyARIntPoint2d[] coord = this._coord;
			final int coord_max = this._max_coord;
			final int[] mkvertex =this.__detectMarker_mkvertex;

			//重なりチェッカの最大数を設定
			overlap.setMaxLabels(label_num);

			for (int i=0; i < label_num; i++) {
				final NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo label_pt=flagment.getItem(i);
				int label_area = label_pt.area;
			
				// クリップ領域が画面の枠に接していれば除外
				if (label_pt.clip_l == 0 || label_pt.clip_r == xsize-1){
					continue;
				}
				if (label_pt.clip_t == 0 || label_pt.clip_b == ysize-1){
					continue;
				}
				// 既に検出された矩形との重なりを確認
				if (!overlap.check(label_pt)) {
					// 重なっているようだ。
					continue;
				}
				//輪郭を取得
				int coord_num = _cpickup.getContour(this._bin,i_area,label_pt.entry_x,label_pt.clip_t, coord);
				if (coord_num == coord_max) {
					// 輪郭が大きすぎる。
					continue;
				}
				//輪郭線をチェックして、矩形かどうかを判定。矩形ならばmkvertexに取得
				if (!this._coord2vertex.getVertexIndexes(coord,coord_num,label_area, mkvertex)) {
					// 頂点の取得が出来なかった
					continue;
				}
				//矩形の発見を通知
				this.onSquareDetect(coord,coord_num,mkvertex,o_table);

				// 検出済の矩形の属したラベルを重なりチェックに追加する。
				overlap.push(label_pt);
			
			}
			return;
		}
		public void onSquareDetect(NyARIntPoint2d[] i_coord,int i_coorx_num,int[] i_vertex_index,NyARDetailLabelingTrackSrcTable o_table) throws NyARException
		{
			NyARIntPoint2d[] vertex=this.__ref_vertex;
			
			
			
			NyARDetailLabelingTrackSrcTable.Item item=o_table.prePush();
			if(item==null){
				System.out.println("Drop stack full");
				o_table.pop();
				return;
			}
			
			//線分を配列に配置
			vertex[0]=i_coord[i_vertex_index[0]];
			vertex[1]=i_coord[i_vertex_index[1]];
			vertex[2]=i_coord[i_vertex_index[2]];
			vertex[3]=i_coord[i_vertex_index[3]];

			for(int i=0;i<4;i++){
				this._coordline.coord2Line(i_vertex_index[i],i_vertex_index[(i+1)%4],i_coord,i_coorx_num,item.ideal_line[i]);
			}
			double cx,cy;
			cx=cy=0;
			for (int i = 0; i < 4; i++) {
				//直線同士の交点計算
				NyARDoublePoint2d v_ptr=item.ideal_vertex[i];
				if(!NyARLinear.crossPos(item.ideal_line[i],item.ideal_line[(i + 3) % 4],v_ptr)){
					throw new NyARException();//ここのエラー復帰するならダブルバッファにすればOK
				}
				cx+=v_ptr.x;
				cy+=v_ptr.y;
			}
			//中心位置計算
			item.ideal_center.x=(int)(cx/4);
			item.ideal_center.y=(int)(cy/4);
			return;
		}		
		
		
		/**
		 * デバック用API
		 * @return
		 */
		public Object[] _probe()
		{
			Object[] ret=new Object[10];
			ret[0]=this._stack;
			return ret;
		}

	}	
	
	
	
	
	
	private SquareDetector _sqdetect;
	private NyARRasterAnalyzer_Histogram _histogram_analyzer;
	private NyARHistogram _histogram;
	private NyARHistogramAnalyzer_DiscriminantThreshold _threshold_detector;
	public NyARDetailLabelingTrackSrcTable(int i_length,NyARIntSize i_screen_size,NyARCameraDistortionFactor i_distfactor_ref,int i_raster_type) throws NyARException
	{
		super(i_length,i_screen_size,i_distfactor_ref);

		this._sqdetect=new SquareDetector(i_screen_size,i_distfactor_ref,i_raster_type);
		//上位と排他的にシェアしてね。
//		this._rgb2gs=new NyARRasterFilter_Rgb2Gs_RgbAve(i_raster_type);
//		this._gs=new NyARGrayscaleRaster(i_screen_size.w,i_screen_size.h);
//		this._histogram_analyzer=new NyARRasterAnalyzer_Histogram(this._gs.getBufferType(),4);
		this._histogram=new NyARHistogram(256);
		this._threshold_detector=new NyARHistogramAnalyzer_DiscriminantThreshold();
	}
	private NyARRleLabelFragmentInfoStack _label_stack;
	public void initDetectionBatch()
	{
		this._label_stack.clear();
//		this._labeling.labeling(i_gs_raster, i_area, i_th, o_stack);
	}
	public boolean update(NyARDetailLabelingTrackItem[] i_item,int i_number,INyARRgbRaster i_src) throws NyARException
	{
		this._sqdetect.detectMarker(i_src, i_item, i_number, i_area, o_table)
		
//		NyARGrayscaleRaster gs=this._gs;
		NyARIntRect rect=new NyARIntRect();
//		NyARRasterFilter_Rgb2Gs_RgbAve ave=this._rgb2gs;

		//頂点集合を包むRECTを計算
		for(int i=i_number-1;i>=0;i--){
			rect.wrapVertex(i_item[i].estimate.ideal_vertex,4);
			
			//エリアを1.5倍
			int w=rect.w;
			int h=rect.h;
			rect.x-=w-w/2;
			rect.y-=h-h/2;
			rect.w+=w;
			rect.h+=h;
			//境界値制限
			rect.clip(0,0,319,239);
			//領域指定のGS化
			ave.doFilter(i_src, gs,rect.x,rect.y,rect.w,rect.h);
			//領域指定のヒストグラム抽出
			int skip=rect.w*rect.h/4096;
			this._histogram_analyzer.setVerticalInterval(skip<1?1:skip);
			this._histogram_analyzer.analyzeRaster(gs,rect,this._histogram);
			int th=this._threshold_detector.getThreshold(this._histogram);
			//領域指定のラべリングと、スタックへの蓄積
		}

//		this._sqdetect.detectMarker(gs,rect,th,i_item);
//		System.out.println(this._histogram.total_of_data+"]");
		
		
		return true;
	}
	public Object[] _probe()
	{
		Object[] ret=new Object[10];
		ret[0]=this._gs;
		return ret;
	}
}