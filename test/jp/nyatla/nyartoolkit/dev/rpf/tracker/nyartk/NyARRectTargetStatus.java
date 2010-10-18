package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.NyARPointVector2d;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LrlsGsRaster;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.NyARVectorReader_INT1D_GRAY_8;




public class NyARRectTargetStatus extends NyARTargetStatus
{
	private NyARRectTargetStatusPool _ref_my_pool;
	
	
	/**
	 * 現在の矩形情報
	 */
	public NyARSquare square;
	/**
	 * 頂点の加速度情報
	 */
	//
	//制御部
	
	/**
	 * @Override
	 */
	public NyARRectTargetStatus(NyARRectTargetStatusPool i_pool)
	{
		super(i_pool._op_interface);
		this._ref_my_pool=i_pool;
		this.square=new NyARSquare();
	}
	/**
	 * @Override 
	 */
	public int releaseObject()
	{
		int ret=super.releaseObject();
		return ret;
	}

	


	/**
	 * i_vecposをソースにして、メンバ変数を更新します。
	 * @param i_vecpos
	 * ベクトル集合。引数の内容は、破壊されます。
	 * @return
	 * @throws NyARException 
	 */
	private boolean updateVertexParam(VectorCoords i_vecpos) throws NyARException
	{
		//ベクトルのマージ(マージするときに、3,4象限方向のベクトルは1,2象限のベクトルに変換する。)
		this._ref_my_pool._vecpos_op.margeResembleCoords(i_vecpos);
		if(i_vecpos.length<4){
			return false;
		}

		//4線分抽出
		if(i_vecpos.length<4){
			return false;
		}
		i_vecpos.getKeyCoord(this._ref_my_pool._indexbuf);
		//点に変換
		NyARSquare sq=this.square;
		if(!this._ref_my_pool._line_detect.line2SquareVertex(this._ref_my_pool._indexbuf,this.square.sqvertex)){
			return false;
		}
		//点から直線を再計算
		for(int i=3;i>=0;i--){
			sq.line[i].calculateLine(sq.sqvertex[i],sq.sqvertex[(i+1)%4]);
		}
		return true;
	}
	private boolean setDiffParam(NyARRectTargetStatus i_prev_param)
	{
		//過去と比較して、周波数ずれを計算
		//頂点位置を調整
		int d=i_prev_param.square.checkVertexShiftValue(this.square);
		this.square.rotateVertexL(d);
		//差分パラメータをセット
		return true;
	}
	
	/**
	 * 値をセットします。この関数は、処理の成功失敗に関わらず、内容変更を行います。
	 * 成功した場合、オブジェクトはi_contour_statusの所有権を取得します。
	 * @param i_contour_status
	 * @return
	 * @throws NyARException
	 */
	public boolean setValue(NyARContourTargetStatus i_contour_status) throws NyARException
	{
		//頂点パラメータのセット
		updateVertexParam(i_contour_status.vecpos);
		return true;
	}
	VectorCoords _vc=new VectorCoords(10);
	/**
	 * 現在の矩形を元に、線分をトレースして、頂点を取得します。
	 * @param i_reader
	 * @return
	 */
	private boolean updateVertexParamByLineLog(NyARVectorReader_INT1D_GRAY_8 i_reader,NyARDoublePoint2d[] i_prev_vertex)
	{
		//4本のベクトルを計算
		NyARSquare sq=this.square;
		for(int i=0;i<4;i++){
			//ベクトル取得
			i_reader.traceLineVector(i_prev_vertex[i],i_prev_vertex[(i+3)%4],4,this._vc,10);
			//一番強いベクトルを取る
			VectorCoords.CoordData ptr=this._vc.item[this._vc.getMaxCoordIndex()];
			sq.line[i].setVector(ptr.dx,ptr.dy,ptr.x,ptr.y);
		}

		//4点抽出
		for(int i=3;i>=0;i--){
			if(!sq.line[i].crossPos(sq.line[(i + 3) % 4],sq.sqvertex[i])){
				//四角が作れない。
				return false;
			}
		}
		return true;
	}
	/**
	 * 輪郭からの単独検出
	 * @param i_raster
	 * @param i_prev_status
	 * @return
	 * @throws NyARException
	 */
	public boolean setValue(LrlsGsRaster i_raster,NyARRectTargetStatus i_prev_status) throws NyARException
	{
		if(!updateVertexParamByLineLog(i_raster.getVectorReader(),i_prev_status.square.sqvertex)){
			return false;
		}
		//差分パラメータのセット
		setDiffParam(i_prev_status);
		return true;
	}
	
	
	/**
	 * 距離探索の敷居値
	 * 対角距離の1/n %をの変動を、許容距離とする場合、
	 * (n*n)を指定する。
	 */
	private final static int _DIST_TH=(2*2);
	/**
	 * 値をセットします。この関数は、処理の成功失敗に関わらず、内容変更を行います。
	 * @param i_contour_status
	 * @return
	 * @throws NyARException
	 */
	public boolean setValue(NyARContourTargetStatusPool i_pool, LrlsGsRaster i_raster,LowResolutionLabelingSamplerOut.Item i_source,NyARRectTargetStatus i_prev_status) throws NyARException
	{
		VectorCoords vecpos=this._ref_my_pool._vecpos;
		LrlsGsRaster r=(LrlsGsRaster)i_source.ref_raster;
		//輪郭線を取る
		if(!r.baseraster.getVectorReader().traceConture(r, i_source.lebeling_th, i_source.entry_pos, vecpos)){
			return false;
		}
		//頂点パラメータのセット
		if(!updateVertexParam(vecpos)){
			return false;
		}
		//検出矩形と一致してるかな？
		NyARIntRect s=new NyARIntRect();
		s.setAreaRect(this.square.sqvertex,4);
		//検出エリア同士の対角点移動量を計算
		int d=NyARIntRect.getSqDiagonalPointDiff(s,i_source.base_area);
		if(2*_DIST_TH*d>i_source.base_area_sq_diagonal){
			return false;
		}

		//差分パラメータのセット
		setDiffParam(i_prev_status);
		return true;
	}
	/**
	 * このデータが初期チェック(CoordからRectへの遷移)をパスするかチェックします。
	 * 条件は、
	 *  1.対角線距離比が0.2～5.0,
	 *  
	 *  1.頂点範囲が全て検出エリアの1.5倍サイズ以内,1/10以上の大きさ
	 *  2.頂点同士が極端に接近していないこと(全周距離の)
	 * @todo この関数はリファクタリング対象。
	 * 
	 * @param sampleArea
	 */
	public boolean checkInitialRectCondition(NyARIntRect i_sample_area)
	{
		NyARDoublePoint2d[] v=this.square.sqvertex;


		//対角線比
/*		double sq_dist_rate=NyARDoublePoint2d.sqNorm(v0,v2)/NyARDoublePoint2d.sqNorm(v1,v3);
		if((0.2*0.2)>sq_dist_rate || sq_dist_rate>(5*5)){
			System.out.println("DROP!!!!!!!!!!!!!!!!!!!![0]");
			return false;
		}*/
		//検出した四角形の対角点が検出エリア内か？
		int cx=(int)(v[0].x+v[1].x+v[2].x+v[3].x)/4;
		int cy=(int)(v[0].y+v[1].y+v[2].y+v[3].y)/4;
		if(!i_sample_area.isInnerPoint(cx,cy)){
			System.out.println("DROP!!!!!!!!!!!!!!!!!!!![2]");
			return false;
		}
		//一番長い辺と短い辺の比を確認(10倍の比があったらなんか変)
		int max=Integer.MIN_VALUE;
		int min=Integer.MAX_VALUE;
		for(int i=0;i<4;i++){
			int t=(int)NyARDoublePoint2d.sqNorm(v[i],v[(i+1)%4]);
			if(t>max){max=t;}
			if(t<min){min=t;}
		}
		//比率係数の確認
		if(max<(5*5) ||min<(5*5)){
			System.out.println("DROP!!!!!!!!!!!!!!!!!!!![4]");
			return false;
		}
		//10倍スケールの2乗
		if((10*10)*min/max<(5*5)){
			System.out.println("DROP!!!!!!!!!!!!!!!!!!!![3]");
			return false;
		}
		return true;
	}
	/**
	 * 直線式とRECTから、線分を計算します。
	 * @param a
	 * @param o_point1
	 * @param o_point2
	 * @return
	 * 線分が作れない。
	 */
	public boolean clip(NyARLinear a,NyARIntRect i_rect,NyARDoublePoint2d o_point1,NyARDoublePoint2d o_point2)
	{
		//
/*		a.crossPos(0,-1,i_rect.y,   o_point);     //y=rect.yの線
		a.crossPos(0,-1,i_rect.y+i_rect.h, o_point);//y=(rect.y+=rect.h)の線
		a.crossPos(-1,0,i_rect.x,   o_point);     //y=rect.yの線
		a.crossPos(-1,0,i_rect.x+i_rect.w, o_point);//y=(rect.y+=rect.h)の線
*/	}
}