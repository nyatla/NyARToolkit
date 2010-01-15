/* このソースは実験用のソースです。
 * 動いたり動かなかったりします。
 * 
 */
package jp.nyatla.nyartoolkit.dev;

import javax.media.*;

import javax.media.util.BufferToImage;
import javax.media.format.*;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;

import jp.nyatla.nyartoolkit.core.*;

import java.awt.*;

import jp.nyatla.nyartoolkit.core.labeling.*;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingImage;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingLabel;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingLabelStack;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabeling_ARToolKit;
import jp.nyatla.nyartoolkit.core.match.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.pca2d.INyARPca2d;
import jp.nyatla.nyartoolkit.core.pca2d.NyARPca2d_MatrixPCA_O2;
import jp.nyatla.nyartoolkit.core.pickup.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareStack;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix22;
import jp.nyatla.nyartoolkit.core.types.stack.*;


///**
// * QRコードのシンボルを結びつける偉いクラス
// *
// */
//class NyQrCodeSymbolBinder
//{
//	LabelingBufferdImage bimg;
//	
//	NyARIntPoint[][] _sqare;
//	/**
//	 * 最小の三角形を構成する頂点セットを得る
//	 * @param i_s0
//	 * @param i_s1
//	 * @param i_s2
//	 * @param o_vertex
//	 */
//	public static void getMinimumTriangleVertex(NyARSquare[] i_sqare,int[] o_vertex_id)
//	{
//		//辺の長さが最小になる頂点の組合せを探す
//		int d;
//		int x,y;
//		int dmax=0x7fffffff;
//		final NyARIntPoint[] vertex0=i_sqare[0].imvertex;
//		final NyARIntPoint[] vertex1=i_sqare[1].imvertex;
//		final NyARIntPoint[] vertex2=i_sqare[2].imvertex;
//		for(int i=0;i<4;i++)
//		{
//			for(int i2=0;i2<4;i2++)
//			{
//				for(int i3=0;i3<4;i3++){
//					x=vertex0[i].x-vertex2[i3].x;
//					y=vertex0[i].y-vertex2[i3].y;
//					d=x*x+y*y;
//					x=vertex1[i2].x-vertex2[i3].x;
//					y=vertex1[i2].y-vertex2[i3].y;
//					d+=x*x+y*y;
//					x=vertex1[i2].x-vertex0[i].x;
//					y=vertex1[i2].y-vertex0[i].y;
//					d+=x*x+y*y;
//					if(d<dmax){
//						dmax=d;
//						o_vertex_id[0]=i;					
//						o_vertex_id[1]=i2;
//						o_vertex_id[2]=i3;
//					}
//				}
//			}
//		}
//		return;
//	}
//	/**
//	 * 2矩形の頂点距離が最低の組合せを探す
//	 * @param i_sqare
//	 * @param o_vertex_id
//	 */
//	public static void getMinimumLineVertex(NyARIntPoint[] i_sqare0,NyARIntPoint[] i_sqare1,int[] o_vertex_id)
//	{
//		//辺の長さが最小になる頂点の組合せを探す
//		int d;
//		int x,y;
//		int dmax=0x7fffffff;
//		for(int i=0;i<4;i++)
//		{
//			for(int i2=0;i2<4;i2++)
//			{
//				x=i_sqare1[i2].x-i_sqare0[i].x;
//				y=i_sqare1[i2].y-i_sqare0[i].y;
//				d=x*x+y*y;
//				if(d<dmax){
//					dmax=d;
//					o_vertex_id[0]=i;					
//					o_vertex_id[1]=i2;
//				}
//			}
//		}
//		return;
//	}	
//	
//	/**
//	 * キーシンボルのインデックスを得る
//	 * @param i_sqare
//	 * @param i_vertex_id
//	 * 最小三角形の頂点IDセット
//	 * @return
//	 */
//	public static int getKeySymble(NyARSquare[] i_sqare,int[] i_vertex_id)
//	{
//		//シンボルグループの重心を計算
//		int cx,cy;
//		cx=cy=0;
//		for(int i=0;i<3;i++)
//		{
//			final NyARIntPoint[] sq_ptr=i_sqare[i].imvertex;
//			cx+=sq_ptr[0].x;			
//			cx+=sq_ptr[1].x;			
//			cx+=sq_ptr[2].x;			
//			cx+=sq_ptr[3].x;			
//			cy+=sq_ptr[0].y;			
//			cy+=sq_ptr[1].y;			
//			cy+=sq_ptr[2].y;			
//			cy+=sq_ptr[3].y;			
//		}
//		cx/=12;
//		cy/=12;	
//		//前段で探した頂点候補のうち、最も重心に近いものが中心シンボルの内対角点
//		int key_symble_idx=0;
//		int x=i_sqare[0].imvertex[i_vertex_id[0]].x-cx;
//		int y=i_sqare[0].imvertex[i_vertex_id[0]].y-cy;
//		int dmax=x*x+y*y;
//		for(int i=1;i<3;i++){
//			x=i_sqare[i].imvertex[i_vertex_id[i]].x-cx;
//			y=i_sqare[i].imvertex[i_vertex_id[i]].y-cy;
//			final int d=x*x+y*y;
//			if(d<dmax){
//				dmax=d;
//				key_symble_idx=i;
//			}
//		}
//		return key_symble_idx;
//	}
//	public void bindSquare(NyARSquare i_sq1,int i_lv1,NyARSquare i_sq2,int i_lv2)
//	{
//		NyARSquare new_square=new NyARSquare();
//		//4辺の式を計算
//		new_square.line[0].copyFrom(i_sq1.line[(i_lv1)%4]);
//		new_square.line[1].copyFrom(i_sq1.line[(i_lv1+3)%4]);
//		new_square.line[2].copyFrom(i_sq2.line[(i_lv2)%4]);
//		new_square.line[3].copyFrom(i_sq2.line[(i_lv2+3)%4]);
//		//歪み無しの座標系を計算
//		final NyARDoublePoint2d[] l_sqvertex = new_square.sqvertex;
//		final NyARLinear[] l_line = new_square.line;		
//		for (int i = 0; i < 4; i++) {
//			final NyARLinear l_line_i = l_line[i];
//			final NyARLinear l_line_2 = l_line[(i + 3) % 4];
//			final double w1 = l_line_2.run * l_line_i.rise - l_line_i.run * l_line_2.rise;
//			if (w1 == 0.0) {
//				return;
//			}
//			l_sqvertex[i].x = (l_line_2.rise * l_line_i.intercept - l_line_i.rise * l_line_2.intercept) / w1;
//			l_sqvertex[i].y = (l_line_i.run * l_line_2.intercept - l_line_2.run * l_line_i.intercept) / w1;
////			// 頂点インデクスから頂点座標を得て保存
////			l_imvertex[i].x = i_xcoord[i_mkvertex[i]];
////			l_imvertex[i].y = i_ycoord[i_mkvertex[i]];
//		}
//		Graphics g=this.bimg.getGraphics();
//		g.setColor(Color.red);
//		int[] x=new int[4];
//		int[] y=new int[4];
//		for(int i=0;i<4;i++){
//			x[i]=(int)l_sqvertex[i].x;
//			y[i]=(int)l_sqvertex[i].y;
//		}
//		g.drawPolygon(x,y,4);
//		//基準点はVertexをそのまま採用
//		//２個の想定点は座標を逆変換して設定
//	}
//	/**
//	 *
//	 * @param i_sq
//	 * @param o_sq
//	 * @return
//	 */
//	public boolean margeEdge(NyARSquare[] i_sq,NyARSquare o_sq)
//	{
//		int[] minimum_triangle_vertex=new int[3];
//		int[] minimum_line_vertex=new int[2];
//
//		//辺の長さが最小になる頂点の組合せを探す
//		getMinimumTriangleVertex(i_sq,minimum_triangle_vertex);
//		
//		//キーシンボルのインデクス番号を得る
//		int key_simble_idx=getKeySymble(i_sq,minimum_triangle_vertex);
//		
//		//エッジシンボルのインデックス番号を決める
//		int symbol_e1_idx=(key_simble_idx+1)%3;
//		int symbol_e2_idx=(key_simble_idx+2)%3;
//		
//		//エッジシンボル間で最短距離を取る頂点ペアを取る
//		//(角度を低くするとエラーが出やすい。対角線との類似性を確認する方法のほうがいい。多分)
//		getMinimumLineVertex(i_sq[symbol_e1_idx].imvertex,i_sq[symbol_e2_idx].imvertex,minimum_line_vertex);
//		
//		//内対角を外対角に変換
//		int lv1=(minimum_line_vertex[0]+2)%4;
//		int lv2=(minimum_line_vertex[1]+2)%4;
//		int kv =(minimum_triangle_vertex[key_simble_idx]+2)%4;
//		//矩形のバインド
//		bindSquare(i_sq[symbol_e1_idx],lv1,i_sq[symbol_e2_idx],lv2);
//				
//		
//		Graphics g=this.bimg.getGraphics();
//		//内対角に緑の点を打つ
//		g.setColor(Color.green);
//		g.fillRect(i_sq[symbol_e1_idx].imvertex[lv1].x-2,i_sq[symbol_e1_idx].imvertex[lv1].y-2,4,4);
//		g.fillRect(i_sq[symbol_e2_idx].imvertex[lv2].x-2,i_sq[symbol_e2_idx].imvertex[lv2].y-2,4,4);
////		g.fillRect(i_sq[symbol_e2_idx][minimum_line_vertex[1]].x-2,i_sq[symbol_e2_idx][minimum_line_vertex[1]].y-2,4,4);
//		
//		
//		//中央の中心エッジから最も遠い点が
//		//両端のエッジも探す
//		
//		
//		
//
////		this.bimg.getGraphics().fillRect(i_sq[edge1_id][vid1_id].x,i_sq[edge1_id][vid1_id].y,5,5);
//		
//		for (int i = 0; i <3; i++) {
//			int[] xp=new int[4]; 
//			int[] yp=new int[4]; 
//			for(int i2=0;i2<4;i2++){
//				xp[i2]=i_sq[i].imvertex[i2].x;
//				yp[i2]=i_sq[i].imvertex[i2].y;
//			}
//			this.bimg.getGraphics().setColor(Color.RED);
//			this.bimg.getGraphics().drawPolygon(xp, yp,4);
//		}		
//		
//		
//		return false;
//		
//		
//		
//		
//
//		
//	}	
//	
//	
//	
//	
//}


/**
 * 矩形座標をPCAではなく、頂点座標そのものからSquare位置を計算するクラス
 * 
 */
class NyARQRCodeDetector implements INyARSquareDetector
{
	LabelingBufferdImage bimg;
	private static final double VERTEX_FACTOR = 2.0;// 線検出のファクタ

	private static final int AR_AREA_MAX = 100000;// #define AR_AREA_MAX 100000

	private static final int AR_AREA_MIN = 70;// #define AR_AREA_MIN 70

	private final int _width;

	private final int _height;

	private final NyARLabeling_ARToolKit _labeling;

	private final NyARLabelingImage _limage;

	private final NyARCameraDistortionFactor _dist_factor_ref;

	/**
	 * 最大i_squre_max個のマーカーを検出するクラスを作成する。
	 * 
	 * @param i_param
	 */
	public NyARQRCodeDetector(NyARCameraDistortionFactor i_dist_factor_ref, NyARIntSize i_size) throws NyARException
	{
		this._width = i_size.w;
		this._height = i_size.h;
		this._dist_factor_ref = i_dist_factor_ref;
		this._labeling = new NyARLabeling_ARToolKit();
		this._limage = new NyARLabelingImage(this._width, this._height);
		this._labeling.attachDestination(this._limage);

		// 輪郭の最大長は画面に映りうる最大の長方形サイズ。
		int number_of_coord = (this._width + this._height) * 2;

		// 輪郭バッファは頂点変換をするので、輪郭バッファの２倍取る。
		this._max_coord = number_of_coord;
		this._xcoord = new int[number_of_coord * 2];
		this._ycoord = new int[number_of_coord * 2];
	}

	private final int _max_coord;

	private final int[] _xcoord;

	private final int[] _ycoord;

	private void normalizeCoord(int[] i_coord_x, int[] i_coord_y, int i_index, int i_coord_num)
	{
		// vertex1を境界にして、後方に配列を連結
		System.arraycopy(i_coord_x, 1, i_coord_x, i_coord_num, i_index);
		System.arraycopy(i_coord_y, 1, i_coord_y, i_coord_num, i_index);
	}

	private final int[] __detectMarker_mkvertex = new int[5];

	/**
	 * ARMarkerInfo2 *arDetectMarker2( ARInt16 *limage, int label_num, int *label_ref,int *warea, double *wpos, int *wclip,int area_max, int area_min, double
	 * factor, int *marker_num ) 関数の代替品 ラベリング情報からマーカー一覧を作成してo_marker_listを更新します。 関数はo_marker_listに重なりを除外したマーカーリストを作成します。
	 * 
	 * @param i_raster
	 * 解析する２値ラスタイメージを指定します。
	 * @param o_square_stack
	 * 抽出した正方形候補を格納するリスト
	 * @throws NyARException
	 */
	public final void detectMarker(NyARBinRaster i_raster, NyARSquareStack o_square_stack) throws NyARException
	{
		final INyARLabeling labeling_proc = this._labeling;
		final NyARLabelingImage limage = this._limage;

		// 初期化

		// マーカーホルダをリセット
		o_square_stack.clear();

		// ラベリング
		labeling_proc.labeling(i_raster);

		// ラベル数が0ならここまで
		final int label_num = limage.getLabelStack().getLength();
		if (label_num < 1) {
			return;
		}

		final NyARLabelingLabelStack stack = limage.getLabelStack();
		// ラベルを大きい順に整列
		stack.sortByArea();
		final NyARLabelingLabel[] labels = stack.getArray();


		// デカいラベルを読み飛ばし
		int i;
		for (i = 0; i < label_num; i++) {
			// 検査対象内のラベルサイズになるまで無視
			if (labels[i].area <= AR_AREA_MAX) {
				break;
			}
		}
		
		final int xsize = this._width;
		final int ysize = this._height;
		final int[] xcoord = this._xcoord;
		final int[] ycoord = this._ycoord;
		final int coord_max = this._max_coord;
		final int[] mkvertex = this.__detectMarker_mkvertex;
		final int[][] buf = (int[][]) limage.getBufferReader().getBuffer();
		final int[] indextable = limage.getIndexArray();
		int coord_num;
		int label_area;
		NyARLabelingLabel label_pt;
		NyARSquareStack wk_stack=new NyARSquareStack(100);

		for (; i < label_num; i++) {
			label_pt = labels[i];
			label_area = label_pt.area;
			// 検査対象サイズよりも小さくなったら終了
			if (label_area < AR_AREA_MIN) {
				break;
			}
			// クリップ領域が画面の枠に接していれば除外
			if (label_pt.clip_l == 1 || label_pt.clip_r == xsize - 2) {// if(wclip[i*4+0] == 1 || wclip[i*4+1] ==xsize-2){
				continue;
			}
			if (label_pt.clip_t == 1 || label_pt.clip_b == ysize - 2) {// if( wclip[i*4+2] == 1 || wclip[i*4+3] ==ysize-2){
				continue;
			}
			// 特徴点候補であるかを確認する。
			if (!hasQrEdgeFeature(buf, indextable, label_pt)) {
				continue;
			}

			// 輪郭を取得
			coord_num = limage.getContour(i, coord_max, xcoord, ycoord);
			if (coord_num == coord_max) {
				// 輪郭が大きすぎる。
				continue;
			}
			// 頂点候補のインデクスを取得
			final int vertex1 = scanVertex(xcoord, ycoord, coord_num);

			// 頂点候補(vertex1)を先頭に並べなおした配列を作成する。
			normalizeCoord(xcoord, ycoord, vertex1, coord_num);

			// 頂点情報を取得
			if (!getSquareVertex(xcoord, ycoord, vertex1, coord_num, label_area, mkvertex)) {
				continue;
			}
			NyARSquare square=(NyARSquare)wk_stack.prePush();
			if(!getSquareLine(mkvertex,xcoord,ycoord,square)){
				wk_stack.pop();
				continue;
			}
		}
		bindQrcodeEdge(wk_stack);
		//エッジ同士の相関関係をしらべる。

		return;
	}
	/**
	 *
	 * @param i_sq
	 * @param o_sq
	 * @return
	 */
	public boolean margeEdge(NyARSquare[] i_sq,NyARSquare o_sq)
	{
		NyQrCodeSymbolBinder binder=new NyQrCodeSymbolBinder();
		binder.bimg=this.bimg;
		binder.(i_sq, o_sq);

		return false;
		
		
		
		
	}
	/**
	 * QRコードのエッジペアを作る
	 * @param i_square_stack
	 */
	public void bindQrcodeEdge(NyARSquareStack i_square_stack)
	{
		
		NyARSquare sq_ptr1,sq_ptr2,sq_ptr3;
		int number_of_edge=i_square_stack.getLength();
		if(number_of_edge<3){
			return;
		}
		NyARSquare[] sa=i_square_stack.getArray();
		for(int i=0;i<number_of_edge;i++)
		{	
			for(int i2=i+1;i2<number_of_edge;i2++)
			{
				sq_ptr2=sa[i2];
				for(int i3=i2+1;i3<number_of_edge;i3++){
					sq_ptr3=sa[i3];
					//3個のエッジの関連性を確認する。
					margeEdge(sa,null);
				}
				//
			}
		}
	}
	/**
	 * ２つの頂点座標を結ぶ直線から、NyARLinearを計算する。
	 * 
	 * @param i_v1
	 * @param i_v2
	 * @param o_line
	 */
	final private void getLine(NyARDoublePoint2d i_v1, NyARDoublePoint2d i_v2, NyARLinear o_line)
	{
		final double x = i_v1.x - i_v2.x;
		final double y = i_v1.y - i_v2.y;
		final double x2 = x * x;
		final double y2 = y * y;
		final double rise_ = Math.sqrt(x2 / (x2 + y2));
		o_line.rise = rise_;
		o_line.run = Math.sqrt(y2 / (x2 + y2));
		if (x < 0) {
			if (y < 0) {
				o_line.rise = -o_line.rise;
			} else {
				o_line.rise = -o_line.rise;
				o_line.run = -o_line.run;
			}
		} else {
			if (y < 0) {
				o_line.rise = -o_line.rise;
				o_line.run = -o_line.run;
			} else {
				o_line.rise = -o_line.rise;
			}
		}
		o_line.intercept = (i_v1.y + (o_line.run / o_line.rise) * (i_v1.x)) * rise_;

	}
	private final INyARPca2d _pca=new NyARPca2d_MatrixPCA_O2(100);
	private final NyARDoubleMatrix22 __getSquareLine_evec=new NyARDoubleMatrix22();
	private final NyARDoublePoint2d __getSquareLine_mean=new NyARDoublePoint2d();
	private final NyARDoublePoint2d __getSquareLine_ev=new NyARDoublePoint2d();
	/**
	 * arGetLine(int x_coord[], int y_coord[], int coord_num,int vertex[], double line[4][3], double v[4][2]) arGetLine2(int x_coord[], int y_coord[], int
	 * coord_num,int vertex[], double line[4][3], double v[4][2], double *dist_factor) の２関数の合成品です。 マーカーのvertex,lineを計算して、結果をo_squareに保管します。
	 * Optimize:STEP[424->391]
	 * 
	 * @param i_cparam
	 * @return
	 * @throws NyARException
	 */
	private boolean getSquareLine(int[] i_mkvertex, int[] i_xcoord, int[] i_ycoord, NyARSquare o_square) throws NyARException
	{
		final NyARLinear[] l_line = o_square.line;
		final NyARCameraDistortionFactor dist_factor=this._dist_factor_ref;  
		final NyARDoubleMatrix22 evec=this.__getSquareLine_evec;
		final NyARDoublePoint2d mean=this.__getSquareLine_mean;
		final NyARDoublePoint2d ev=this.__getSquareLine_ev;
	
		
		for (int i = 0; i < 4; i++) {
			final double w1 = (double) (i_mkvertex[i + 1] - i_mkvertex[i] + 1) * 0.05 + 0.5;
			final int st = (int) (i_mkvertex[i] + w1);
			final int ed = (int) (i_mkvertex[i + 1] - w1);
			final int n = ed - st + 1;
			if (n < 2) {
				// nが2以下でmatrix.PCAを計算することはできないので、エラー
				return false;
			}
			//主成分分析する。
			this._pca.pcaWithDistortionFactor(i_xcoord, i_ycoord, st, n,dist_factor, evec, ev,mean);
			final NyARLinear l_line_i = l_line[i];
			l_line_i.run = evec.m01;// line[i][0] = evec->m[1];
			l_line_i.rise = -evec.m00;// line[i][1] = -evec->m[0];
			l_line_i.intercept = -(l_line_i.run * mean.x + l_line_i.rise * mean.y);// line[i][2] = -(line[i][0]*mean->v[0] + line[i][1]*mean->v[1]);
		}

		final NyARDoublePoint2d[] l_sqvertex = o_square.sqvertex;
		final NyARIntPoint2d[] l_imvertex = o_square.imvertex;
		for (int i = 0; i < 4; i++) {
			final NyARLinear l_line_i = l_line[i];
			final NyARLinear l_line_2 = l_line[(i + 3) % 4];
			final double w1 = l_line_2.run * l_line_i.rise - l_line_i.run * l_line_2.rise;
			if (w1 == 0.0) {
				return false;
			}
			l_sqvertex[i].x = (l_line_2.rise * l_line_i.intercept - l_line_i.rise * l_line_2.intercept) / w1;
			l_sqvertex[i].y = (l_line_i.run * l_line_2.intercept - l_line_2.run * l_line_i.intercept) / w1;
			// 頂点インデクスから頂点座標を得て保存
			l_imvertex[i].x = i_xcoord[i_mkvertex[i]];
			l_imvertex[i].y = i_ycoord[i_mkvertex[i]];
		}
		return true;
	}
	/**
	 * 辺からの対角線が最長になる点を対角線候補として返す。
	 * 
	 * @param i_xcoord
	 * @param i_ycoord
	 * @param i_coord_num
	 * @return
	 */
	private int scanVertex(int[] i_xcoord, int[] i_ycoord, int i_coord_num)
	{
		final int sx = i_xcoord[0];
		final int sy = i_ycoord[0];
		int d = 0;
		int w, x, y;
		int ret = 0;
		for (int i = 1; i < i_coord_num; i++) {
			x = i_xcoord[i] - sx;
			y = i_ycoord[i] - sy;
			w = x * x + y * y;
			if (w > d) {
				d = w;
				ret = i;
			}
			// ここでうまく終了条件入れられないかな。
		}
		return ret;
	}

	private final NyARVertexCounter __getSquareVertex_wv1 = new NyARVertexCounter();

	private final NyARVertexCounter __getSquareVertex_wv2 = new NyARVertexCounter();

	/**
	 * static int arDetectMarker2_check_square( int area, ARMarkerInfo2 *marker_info2, double factor ) 関数の代替関数 OPTIMIZED STEP [450->415] o_squareに頂点情報をセットします。
	 * 
	 * @param i_x_coord
	 * @param i_y_coord
	 * @param i_vertex1_index
	 * @param i_coord_num
	 * @param i_area
	 * @param o_vertex
	 * 要素数はint[4]である事
	 * @return
	 */
	private boolean getSquareVertex(int[] i_x_coord, int[] i_y_coord, int i_vertex1_index, int i_coord_num, int i_area, int[] o_vertex)
	{
		final NyARVertexCounter wv1 = this.__getSquareVertex_wv1;
		final NyARVertexCounter wv2 = this.__getSquareVertex_wv2;
		final int end_of_coord = i_vertex1_index + i_coord_num - 1;
		final int sx = i_x_coord[i_vertex1_index];// sx = marker_info2->x_coord[0];
		final int sy = i_y_coord[i_vertex1_index];// sy = marker_info2->y_coord[0];
		int dmax = 0;
		int v1 = i_vertex1_index;
		for (int i = 1 + i_vertex1_index; i < end_of_coord; i++) {// for(i=1;i<marker_info2->coord_num-1;i++)
			// {
			final int d = (i_x_coord[i] - sx) * (i_x_coord[i] - sx) + (i_y_coord[i] - sy) * (i_y_coord[i] - sy);
			if (d > dmax) {
				dmax = d;
				v1 = i;
			}
		}
		final double thresh = (i_area / 0.75) * 0.01 * VERTEX_FACTOR;

		o_vertex[0] = i_vertex1_index;

		if (!wv1.getVertex(i_x_coord, i_y_coord, i_vertex1_index, v1, thresh)) { // if(get_vertex(marker_info2->x_coord,marker_info2->y_coord,0,v1,thresh,wv1,&wvnum1)<
			// 0 ) {
			return false;
		}
		if (!wv2.getVertex(i_x_coord, i_y_coord, v1, end_of_coord, thresh)) {// if(get_vertex(marker_info2->x_coord,marker_info2->y_coord,v1,marker_info2->coord_num-1,thresh,wv2,&wvnum2)
			// < 0) {
			return false;
		}

		int v2;
		if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {// if(wvnum1 == 1 && wvnum2== 1) {
			o_vertex[1] = wv1.vertex[0];
			o_vertex[2] = v1;
			o_vertex[3] = wv2.vertex[0];
		} else if (wv1.number_of_vertex > 1 && wv2.number_of_vertex == 0) {// }else if( wvnum1 > 1 && wvnum2== 0) {
			// 頂点位置を、起点から対角点の間の1/2にあると予想して、検索する。
			v2 = (v1 - i_vertex1_index) / 2 + i_vertex1_index;
			if (!wv1.getVertex(i_x_coord, i_y_coord, i_vertex1_index, v2, thresh)) {
				return false;
			}
			if (!wv2.getVertex(i_x_coord, i_y_coord, v2, v1, thresh)) {
				return false;
			}
			if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {
				o_vertex[1] = wv1.vertex[0];
				o_vertex[2] = wv2.vertex[0];
				o_vertex[3] = v1;
			} else {
				return false;
			}
		} else if (wv1.number_of_vertex == 0 && wv2.number_of_vertex > 1) {
			// v2 = (v1-i_vertex1_index+ end_of_coord-i_vertex1_index) / 2+i_vertex1_index;
			v2 = (v1 + end_of_coord) / 2;

			if (!wv1.getVertex(i_x_coord, i_y_coord, v1, v2, thresh)) {
				return false;
			}
			if (!wv2.getVertex(i_x_coord, i_y_coord, v2, end_of_coord, thresh)) {
				return false;
			}
			if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {
				o_vertex[1] = v1;
				o_vertex[2] = wv1.vertex[0];
				o_vertex[3] = wv2.vertex[0];
			} else {
				return false;
			}
		} else {
			return false;
		}
		o_vertex[4] = end_of_coord;
		return true;
	}
	/**
	 * QRコードのエッジ特徴を持つラベルであるかを調べる
	 * @param buf
	 * @param index_table
	 * @param i_label
	 * @return
	 */
	private boolean hasQrEdgeFeature(int buf[][], int[] index_table, NyARLabelingLabel i_label)
	{
		int tx, bx;
		int w;
		int i_label_id = i_label.id;
		int[] limage_j;
		final int clip_l = i_label.clip_l;
		final int clip_b = i_label.clip_b;
		final int clip_r = i_label.clip_r;
		final int clip_t = i_label.clip_t;

		tx = bx = 0;
		// 上接点(→)
		limage_j = buf[clip_t];
		for (int i = clip_l; i <= clip_r; i++) {// for( i = clip[0]; i <=clip[1]; i++, p1++ ) {
			w = limage_j[i];
			if (w > 0 && index_table[w - 1] == i_label_id) {
				tx = i;
				break;
			}
		}
		// 下接点(←)
		limage_j = buf[clip_b];
		for (int i = clip_r; i >= clip_l; i--) {// for( i = clip[0]; i <=clip[1]; i++, p1++ ) {
			w = limage_j[i];
			if (w > 0 && index_table[w - 1] == i_label_id) {
				bx = i;
				break;
			}
		}
		final int cx = (clip_l + clip_r) / 2;
		final int cy = (clip_t + clip_b) / 2;
		// 横断チェック(中心から線を引いて、101になるかしらべる)
		if (!checkDiagonalLine(buf, cx, cy, bx, clip_b)) {
			return false;
		}
		if (!checkDiagonalLine(buf, tx, clip_t, cx, cy)) {
			return false;
		}
		return true;
	}

	/**
	 * 対角線のパターンを調べる。
	 * 
	 * @param buf
	 * @param i_px1
	 * @param i_py1
	 * @param i_px2
	 * @param i_py2
	 * @return
	 */
	private boolean checkDiagonalLine(int[][] buf, int i_px1, int i_py1, int i_px2, int i_py2)
	{
		int sub_y = i_py2 - i_py1;
		int sub_x = i_px2 - i_px1;
		// 黒
		int i = 0;
		for (; i < sub_y; i++) {
			int yp = i_py1 + i;
			int xp = i_px1 + i * sub_x / sub_y;
			if (buf[yp][xp] == 0 && buf[yp][xp-1] == 0 && buf[yp][xp+1] == 0) {
				break;
			}

		}
		if (i == sub_y) {
			return false;
		}
		// 白
		for (; i < sub_y; i++) {
			int yp = i_py1 + i;
			int xp = i_px1 + i * sub_x / sub_y;
			if (buf[yp][xp] != 0 && buf[yp][xp-1] != 0 && buf[yp][xp+1] != 0) {
				break;
			}

		}
		if (i == sub_y) {
			return false;
		}
		// 黒
		for (; i < sub_y; i++) {
			int yp = i_py1 + i;
			int xp = i_px1 + i * sub_x / sub_y;
			if (buf[yp][xp] == 0 && buf[yp][xp-1] == 0 && buf[yp][xp+1] == 0) {
				break;
			}

		}
		if (i != sub_y) {
			return false;
		}
		// 端まで到達したらOK
		return true;
	}

}


public class LabelingCamera extends Frame implements JmfCaptureListener
{
	private final String camera_file = "../Data/camera_para.dat";

	private JmfNyARRaster_RGB _raster;

	private JmfCameraCapture capture;
	private NyARParam ap;
	public LabelingCamera() throws NyARException, NyARException
	{
		setBounds(0, 0, 640 + 64, 720 + 64);
		// キャプチャの準備
		capture = new JmfCameraCapture(320, 240, 30f, JmfCameraCapture.PIXEL_FORMAT_RGB);
		capture.setCaptureListener(this);

		// キャプチャイメージ用のラスタを準備
		this._raster = new JmfNyARRaster_RGB(320, 240);
		
		// AR用カメラパラメタファイルをロード
		ap = new NyARParam();
		ap.loadARParamFromFile(camera_file);
		ap.changeScreenSize(320, 240);		
		
		
	}

	// そのラベルが特徴点候補か返す。

	private NyARBinRaster _binraster1 = new NyARBinRaster(320, 240);

	private NyARGrayscaleRaster _gsraster1 = new NyARGrayscaleRaster(320, 240);

	private NyARLabelingImage _limage = new NyARLabelingImage(320, 240);

	private LabelingBufferdImage _bimg = new LabelingBufferdImage(320, 240);

	private NyARRasterFilter_ARToolkitThreshold filter_gs2bin;

	public void onUpdateBuffer(Buffer i_buffer)
	{
		NyARRasterFilter_AreaAverage gs2bin=new NyARRasterFilter_AreaAverage();

		try {
			// キャプチャしたバッファをラスタにセット
			_raster.setBuffer(i_buffer);

			Graphics g = getGraphics();
			// キャプチャ画像
			BufferToImage b2i = new BufferToImage((VideoFormat) i_buffer.getFormat());
			Image img = b2i.createImage(i_buffer);
			this.getGraphics().drawImage(img, 32, 32, this);

			// 画像1
			INyARRasterFilter_RgbToGs filter_rgb2gs = new NyARRasterFilter_RgbAve();
			filter_rgb2gs.doFilter(_raster, _gsraster1);
			this._bimg.drawImage(this._gsraster1);
			this.getGraphics().drawImage(this._bimg, 32 + 320, 32, 320 + 320 + 32, 240 + 32, 0, 240, 320, 0, this);
			

			// 画像2
			gs2bin.doFilter(_gsraster1, _binraster1);
			this._bimg.drawImage(_binraster1);
			this.getGraphics().drawImage(this._bimg, 32, 32 + 240, 320 + 32, 240 + 32 + 240, 0, 240, 320, 0, this);

			// 画像3
			NyARLabelingImage limage = new NyARLabelingImage(320, 240);
			NyARLabeling_ARToolKit labeling = new NyARLabeling_ARToolKit();
			labeling.attachDestination(limage);
			labeling.labeling(_binraster1);
			this._bimg.drawImage(this._gsraster1);
			NyARLabelingLabel[] labels =  limage.getLabelStack().getArray();

			NyARSquareStack stack = new NyARSquareStack(100);
			NyARQRCodeDetector detect = new NyARQRCodeDetector(ap.getDistortionFactor(), new NyARIntSize(320,240));
			detect.bimg=this._bimg;

			detect.detectMarker(_binraster1, stack);
			for (int i = 0; i < stack.getLength(); i++) {
				NyARSquare[] square_ptr = (NyARSquare[]) stack.getArray();
				int[] xp=new int[4]; 
				int[] yp=new int[4]; 
				for(int i2=0;i2<4;i2++){
					xp[i2]=square_ptr[i].imvertex[i2].x;
					yp[i2]=square_ptr[i].imvertex[i2].y;
				}
				this._bimg.getGraphics().setColor(Color.RED);
				this._bimg.getGraphics().drawPolygon(xp, yp,2);
			}
			this.getGraphics().drawImage(this._bimg, 32 + 320, 32 + 240, 320 + 32 + 320, 240 + 32 + 240, 0, 240, 320, 0, this);

			// 画像3
			// threshold.debugDrawHistogramMap(_workraster, _workraster2);
			// this._bimg2.setImage(this._workraster2);
			// this.getGraphics().drawImage(this._bimg2, 32+320, 32+240,320+32+320,240+32+240,0,240,320,0, this);

			// 画像4
			// NyARRasterThresholdAnalyzer_SlidePTile threshold=new NyARRasterThresholdAnalyzer_SlidePTile(15);
			// threshold.analyzeRaster(_gsraster1);
			// filter_gs2bin=new NyARRasterFilter_AreaAverage();
			// filter_gs2bin.doFilter(_gsraster1, _binraster1);
			// this._bimg.drawImage(_binraster1);

			// NyARRasterDetector_QrCodeEdge detector=new NyARRasterDetector_QrCodeEdge(10000);
			// detector.analyzeRaster(_binraster1);

			// this._bimg.overlayData(detector.geResult());

			// this.getGraphics().drawImage(this._bimg, 32, 32+480,320+32,480+32+240,0,240,320,0, this);
			// 画像5

			/*
			 * threshold2.debugDrawHistogramMap(_workraster, _workraster2); this._bimg2.drawImage(this._workraster2); this.getGraphics().drawImage(this._bimg2,
			 * 32+320, 32+480,320+32+320,480+32+240,0,240,320,0, this);
			 */

			// this.getGraphics().drawImage(this._bimg, 32, 32, this);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private INyARLabeling labelingFactory(int i_idx)
	{
		// switch(i_idx){
		// case 0:{NyARLabeling_ARToolKit l=new NyARLabeling_ARToolKit();l.setThresh(4);return l;}
		// case 1:{return new NyLineLabeling();}
		// }
		return null;

	}

	private void startCapture()
	{
		try {
			capture.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		try {
			LabelingCamera mainwin = new LabelingCamera();
			mainwin.setVisible(true);
			mainwin.startCapture();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
