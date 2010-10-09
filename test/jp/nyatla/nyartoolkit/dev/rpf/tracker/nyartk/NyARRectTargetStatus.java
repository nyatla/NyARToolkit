package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
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
	/**
	 * 輪郭ターゲット
	 */
	public NyARContourTargetStatus _contoure;
	
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
	public NyARRectTargetStatus(INyARManagedObjectPoolOperater i_ref_pool_operator)
	{
		super(i_ref_pool_operator);
		this.square=new NyARSquare();
		this._contoure=null;
	}
	public int releaseObject()
	{
		int ret=super.releaseObject();
		if(ret==0 && this._contoure!=null)
		{
			this._contoure.releaseObject();
			this._contoure=null;
		}
		return ret;
	}
	private int[] _wk_indexbuf=new int[4];
	/**
	 * i_contour_statusをソースにして、メンバ変数を更新します。
	 * @param i_contour_status
	 * @return
	 */
	private boolean updateVertexParam(NyARContourTargetStatus i_contour_status)
	{
		int[] indexbuf=this._wk_indexbuf;
		//4線分抽出
		if(i_contour_status.vecpos.length<4){
			return false;
		}
		i_contour_status.vecpos.getKeyCoordIndexes(indexbuf);
		//[省略]正当性チェック？(もしやるなら輪郭抽出系にも手を加えないと。)
		NyARSquare sq=this.square;
		//4頂点を計算する。(本当はベクトルの方向を調整してから計算するべき)
		for(int i=3;i>=0;i--){
			NyARContourTargetStatus.CoordData cv=i_contour_status.vecpos.item[indexbuf[i]];
			sq.line[i].setVector(cv.dx,cv.dy,cv.x,cv.y);
		}
		//4点抽出
		for(int i=3;i>=0;i--){
			if(!NyARLinear.crossPos(sq.line[i],sq.line[(i + 3) % 4],sq.sqvertex[i])){
				//四角が作れない。
				return false;
			}
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
		updateVertexParam(i_contour_status);
		//参照する値の差し替え
		if(this._contoure!=null){
			this._contoure.releaseObject();
		}
		this._contoure=(NyARContourTargetStatus)i_contour_status.refObject();
		return true;
	}
	NyARContourTargetStatus.VectorCoords _vc=new NyARContourTargetStatus.VectorCoords(10);
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
			NyARContourTargetStatus.CoordData ptr=this._vc.item[this._vc.getMaxCoordIndex()];
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
	 * 値をセットします。この関数は、処理の成功失敗に関わらず、内容変更を行います。
	 * @param i_contour_status
	 * @return
	 * @throws NyARException
	 */
	public boolean setValue(NyARContourTargetStatusPool i_pool, LrlsGsRaster i_raster,LowResolutionLabelingSamplerOut.Item i_source,NyARRectTargetStatus i_prev_status) throws NyARException
	{
		NyARContourTargetStatus s=i_pool.newObject();
		if(s==null){
			return false;
		}

			//輪郭ステータスを経由した抽出
			if(!s.setValue(i_source)){
				s.releaseObject();
				return false;
			}
			//頂点パラメータのセット
			if(!updateVertexParam(s)){
				s.releaseObject();
				return false;
			}

		//差分パラメータのセット
		setDiffParam(i_prev_status);
		
		if(this._contoure!=null){
			this._contoure.releaseObject();
		}
		this._contoure=s;
		return true;
	}

	
}