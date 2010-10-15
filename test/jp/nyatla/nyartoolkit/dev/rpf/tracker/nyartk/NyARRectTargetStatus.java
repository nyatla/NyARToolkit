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
//現在のベクトルと比較？
			sq.line[i].setVector(ptr.dx,ptr.dy,ptr.x,ptr.y);
		}
//[省略]正当性チェック？(もしやるなら輪郭抽出系にも手を加えないと。)
		//4点抽出
		for(int i=3;i>=0;i--){
			if(!NyARLinear.crossPos(sq.line[i],sq.line[(i + 3) % 4],sq.sqvertex[i])){
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

	
}