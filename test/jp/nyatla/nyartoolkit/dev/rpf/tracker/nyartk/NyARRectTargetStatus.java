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

/**
 * タグ付きのNyARDoublePoint2d
 */
class MyNyARDoublePoint2d extends NyARDoublePoint2d
{
	public int tag;
	public static MyNyARDoublePoint2d[] createArray(int i_number)
	{
		MyNyARDoublePoint2d[] ret=new MyNyARDoublePoint2d[i_number];
		for(int i=0;i<i_number;i++)
		{
			ret[i]=new MyNyARDoublePoint2d();
		}
		return ret;
	}		
}

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
	 * X軸上で頂点集合をソートします。
	 * バブルソートだから、沢山の時は書き換えて。
	 * @param i_array
	 */
	public void sortArrayByX(NyARDoublePoint2d[] i_array)
	{
		NyARDoublePoint2d tmp;
		for(int i=0;i<i_array.length-1;i++){
			if(i_array[i].x<i_array[i+1].x){
				tmp=i_array[i];
				i_array[i]=i_array[i+1];
				i_array[i+1]=tmp;
				i=0;
			}
		}
	}
	/**
	 * Y軸上で頂点集合をソートします。
	 * バブルソートだから、沢山の時は書き換えて。
	 * @param i_array
	 */
	public void sortArrayByY(NyARDoublePoint2d[] i_array)
	{
		NyARDoublePoint2d tmp;
		for(int i=0;i<i_array.length-1;i++){
			if(i_array[i].y<i_array[i+1].y){
				tmp=i_array[i];
				i_array[i]=i_array[i+1];
				i_array[i+1]=tmp;
				i=0;
			}
		}
	}

	private int[] _wk_indexbuf=new int[4];
	/**
	 * i_vecposをソースにして、メンバ変数を更新します。
	 * @param i_vecpos
	 * @return
	 */
	private boolean updateVertexParam(VectorCoords i_vecpos)
	{
		int[] indexbuf=this._wk_indexbuf;
		//ベクトルのマージ(マージするときに、3,4象限方向のベクトルは1,2象限のベクトルに変換する。)
		this._ref_my_pool._vecpos_op.margeResembleCoords(i_vecpos);
		if(i_vecpos.length<4){
			return false;
		}
		//4線分の抽出
		i_vecpos.getKeyCoordIndexes(indexbuf);
//極めて似ている線分があったら、他の使ってみる。
/*
		
//線分同士の関係から、四角形を構成するように並び替え
		//[0]に対しての交点を計算
		NyARLinear[] t=NyARLinear.createArray(4);
//		int order[]=new int[3];
		for(int i=0;i<4;i++){
			t[i].setVector(i_vecpos.item[indexbuf[i]]);
		}
		int cross_count=0;
		int no_cross=-1;
		MyNyARDoublePoint2d[] p=MyNyARDoublePoint2d.createArray(3);
		for(int i=0;i<3;i++){
			p[i].tag=i;
			if(NyARLinear.crossPos(t[0],t[i+1],p[i])){
				cross_count++;
			}else{
				no_cross=i;
			}
		}
		if(cross_count==2){
			//2点の場合→交点の無いものが３番目になるように調整。
			if(no_cross!=2){
				int tmp=
			}
		}else if(cross_count==3){
			//3点の場合→交点の位置関係を調べて、真ん中で交わるものを2番目に。
			if(t[0].a==0){
				this.sortArrayByX(p);
			}else{
				this.sortArrayByY(p);
			}
			//2番目確定
			int no1=p[1].tag;
			//2番目と真ん中で交わるものを3番目に
			
		}
		//3点の場合→真ん中→両側の順で。
		
		int getCrossPos()
*/		
		VectorCoords.CoordData[] vpitem=i_vecpos.item;

		//2,3番目の入れ替え判定(0->1)
		if(NyARPointVector2d.getVecCos(vpitem[indexbuf[1]],vpitem[indexbuf[2]])>NyARPointVector2d.getVecCos(vpitem[indexbuf[1]],vpitem[indexbuf[3]]))
		{
			int t=indexbuf[2];
			indexbuf[2]=indexbuf[3];
			indexbuf[3]=t;
		}
		
		
		//[省略]正当性チェック？(もしやるなら輪郭抽出系にも手を加えないと。)
		NyARSquare sq=this.square;
		//線分を計算
		for(int i=3;i>=0;i--){
			VectorCoords.CoordData cv=vpitem[indexbuf[i]];
			sq.line[i].setVector(cv.dx,cv.dy,cv.x,cv.y);
		}
		//4点を計算
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