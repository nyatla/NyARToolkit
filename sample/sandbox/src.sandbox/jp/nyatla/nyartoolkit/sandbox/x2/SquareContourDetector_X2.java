package jp.nyatla.nyartoolkit.sandbox.x2;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARI64Linear;
import jp.nyatla.nyartoolkit.core.types.NyARI64Point2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARI64Matrix22;
import jp.nyatla.nyartoolkit.core.squaredetect.*;

public class SquareContourDetector_X2
{
	private final int PCA_LENGTH=20;
	private final int[] _xpos=new int[PCA_LENGTH];
	private final int[] _ypos=new int[PCA_LENGTH];
	private final int[] __detectMarker_mkvertex = new int[5];
	private final NyARFixedFloatVertexCounter __getSquareVertex_wv1 = new NyARFixedFloatVertexCounter();
	private final NyARFixedFloatVertexCounter __getSquareVertex_wv2 = new NyARFixedFloatVertexCounter();
	private final NyARFixedFloatPca2d _pca;
	private final NyARI64Matrix22 __getSquareLine_evec=new NyARI64Matrix22();
	private final NyARI64Point2d __getSquareLine_mean=new NyARI64Point2d();
	private final NyARI64Point2d __getSquareLine_ev=new NyARI64Point2d();
	private final NyARI64Linear[] __getSquareLine_i64liner=NyARI64Linear.createArray(4);
	private final NyARFixedFloatObserv2IdealMap _dist_factor;
	public SquareContourDetector_X2(NyARIntSize i_size,NyARCameraDistortionFactor i_distfactor_ref)
	{
		//歪み計算テーブルを作ると、8*width/height*2の領域を消費します。
		//領域を取りたくない場合は、i_dist_factor_refの値をそのまま使ってください。
		this._dist_factor = new NyARFixedFloatObserv2IdealMap(i_distfactor_ref,i_size);


		// 輪郭バッファは頂点変換をするので、輪郭バッファの２倍取る。
		this._pca=new NyARFixedFloatPca2d();
		return;
	}

	public boolean coordToSquare(int[] i_xcoord,int[] i_ycoord,int i_st_index,int i_coord_num,int i_label_area,NyARSquare o_square) throws NyARException
	{

		final int[] mkvertex = this.__detectMarker_mkvertex;

		// 頂点情報を取得
		if (!getSquareVertex(i_xcoord, i_ycoord, i_st_index, i_coord_num, i_label_area, mkvertex)) {
			// 頂点の取得が出来なかったので破棄
			return false;
		}
		// マーカーを検出
		if (!getSquareLine(mkvertex, i_xcoord, i_ycoord, o_square)){
			// 矩形が成立しなかった。
			return false;
		}
		return true;
	}
	
	private boolean getSquareLine(int[] i_mkvertex, int[] i_xcoord, int[] i_ycoord, NyARSquare o_square) throws NyARException
	{
		final NyARLinear[] l_line = o_square.line;
		final NyARI64Matrix22 evec=this.__getSquareLine_evec;
		final NyARI64Point2d mean=this.__getSquareLine_mean;
		final NyARI64Point2d ev=this.__getSquareLine_ev;
		final NyARI64Linear[] i64liner=this.__getSquareLine_i64liner;
	
		for (int i = 0; i < 4; i++) {
//			final double w1 = (double) (i_mkvertex[i + 1] - i_mkvertex[i] + 1) * 0.05 + 0.5;
			final int w1 = ((((i_mkvertex[i + 1] - i_mkvertex[i] + 1)<<8)*13)>>8) + (1<<7);
			final int st = i_mkvertex[i] + (w1>>8);
			final int ed = i_mkvertex[i + 1] - (w1>>8);
			int n = ed - st + 1;
			if (n < 2) {
				// nが2以下でmatrix.PCAを計算することはできないので、エラー
				return false;
			}
			//配列作成
			n=this._dist_factor.observ2IdealSampling(i_xcoord, i_ycoord, st, n,this._xpos,this._ypos,PCA_LENGTH);
			//主成分分析する。
			this._pca.pcaF16(this._xpos,this._ypos, n,evec, ev,mean);
			final NyARI64Linear l_line_i = i64liner[i];
			l_line_i.run = evec.m01;// line[i][0] = evec->m[1];
			l_line_i.rise = -evec.m00;// line[i][1] = -evec->m[0];
			l_line_i.intercept = -((l_line_i.run * mean.x + l_line_i.rise * mean.y)>>16);// line[i][2] = -(line[i][0]*mean->v[0] + line[i][1]*mean->v[1]);
		}

		final NyARDoublePoint2d[] l_sqvertex = o_square.sqvertex;
		final NyARIntPoint2d[] l_imvertex = o_square.imvertex;
		for (int i = 0; i < 4; i++) {
			final NyARI64Linear l_line_i = i64liner[i];
			final NyARI64Linear l_line_2 = i64liner[(i + 3) % 4];
			final long w1 =(l_line_2.run * l_line_i.rise - l_line_i.run * l_line_2.rise)>>16;
			if (w1 == 0) {
				return false;
			}
			l_sqvertex[i].x = (double)((l_line_2.rise * l_line_i.intercept - l_line_i.rise * l_line_2.intercept) / w1)/65536.0;
			l_sqvertex[i].y = (double)((l_line_i.run * l_line_2.intercept - l_line_2.run * l_line_i.intercept) / w1)/65536.0;
			// 頂点インデクスから頂点座標を得て保存
			l_imvertex[i].x = i_xcoord[i_mkvertex[i]];
			l_imvertex[i].y = i_ycoord[i_mkvertex[i]];
			l_line[i].dy=(double)l_line_i.run/65536.0;
			l_line[i].dx=(double)l_line_i.rise/65536.0;
			l_line[i].c=(double)l_line_i.intercept/65536.0;
		}
		return true;
	}	
	private boolean getSquareVertex(int[] i_x_coord, int[] i_y_coord, int i_vertex1_index, int i_coord_num, int i_area, int[] o_vertex)
	{
		final NyARFixedFloatVertexCounter wv1 = this.__getSquareVertex_wv1;
		final NyARFixedFloatVertexCounter wv2 = this.__getSquareVertex_wv2;
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
		//final double thresh = (i_area / 0.75) * 0.01;
		final long thresh_f16 =(i_area<<16)/75;

		o_vertex[0] = i_vertex1_index;

		if (!wv1.getVertex(i_x_coord, i_y_coord, i_vertex1_index, v1, thresh_f16)) { // if(get_vertex(marker_info2->x_coord,marker_info2->y_coord,0,v1,thresh,wv1,&wvnum1)<
																					// 0 ) {
			return false;
		}
		if (!wv2.getVertex(i_x_coord, i_y_coord, v1, end_of_coord, thresh_f16)) {// if(get_vertex(marker_info2->x_coord,marker_info2->y_coord,v1,marker_info2->coord_num-1,thresh,wv2,&wvnum2)
			// < 0) {
			return false;
		}

		int v2;
		if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {// if(wvnum1 == 1 && wvnum2== 1) {
			o_vertex[1] = wv1.vertex[0];
			o_vertex[2] = v1;
			o_vertex[3] = wv2.vertex[0];
		} else if (wv1.number_of_vertex > 1 && wv2.number_of_vertex == 0) {// }else if( wvnum1 > 1 && wvnum2== 0) {
			//頂点位置を、起点から対角点の間の1/2にあると予想して、検索する。
			v2 = (v1-i_vertex1_index)/2+i_vertex1_index;
			if (!wv1.getVertex(i_x_coord, i_y_coord, i_vertex1_index, v2, thresh_f16)) {
				return false;
			}
			if (!wv2.getVertex(i_x_coord, i_y_coord, v2, v1, thresh_f16)) {
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
			//v2 = (v1-i_vertex1_index+ end_of_coord-i_vertex1_index) / 2+i_vertex1_index;
			v2 = (v1+ end_of_coord)/2;

			if (!wv1.getVertex(i_x_coord, i_y_coord, v1, v2, thresh_f16)) {
				return false;
			}
			if (!wv2.getVertex(i_x_coord, i_y_coord, v2, end_of_coord, thresh_f16)) {
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
	 * 輪郭線の矩形検出開始ポイントを特定して、座標を並べ替えます。
	 * 輪郭線の先頭から、対角線が最長になる点を１点検索し、それより前の区間をバッファの後方に接続します。
	 * 戻り値は対角線が最長になった点です。関数終了後、返却値+i_coord_numの要素が有効になります。
	 * @param i_xcoord
	 * @param i_ycoord
	 * @param i_coord_num
	 * @return
	 */
	public static int normalizeCoord(int[] i_coord_x, int[] i_coord_y,int i_coord_num)
	{
		//
		final int sx = i_coord_x[0];
		final int sy = i_coord_y[0];
		int d = 0;
		int w, x, y;
		int ret = 0;
		for (int i = 1; i < i_coord_num; i++) {
			x = i_coord_x[i] - sx;
			y = i_coord_y[i] - sy;
			w = x * x + y * y;
			if (w > d) {
				d = w;
				ret = i;
			}
			// ここでうまく終了条件入れられないかな。
		}		
		// vertex1を境界にして、後方に配列を連結
		System.arraycopy(i_coord_x, 1, i_coord_x, i_coord_num, ret);
		System.arraycopy(i_coord_y, 1, i_coord_y, i_coord_num, ret);
		return ret;
	}	
	
}