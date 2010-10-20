package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.NyARPointVector2d;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
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
	 * 頂点の速度値
	 */
	public NyARIntPoint2d[] vertex_v=NyARIntPoint2d.createArray(4);

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

	


	private void setDiffParam(NyARRectTargetStatus i_prev_param)
	{
		if(i_prev_param!=null){
			//過去と比較して、周波数ずれを計算
			int d=i_prev_param.square.checkVertexShiftValue(this.square);
			this.square.rotateVertexL(d);
			//差分パラメータをセット
			NyARDoublePoint2d[] vc=this.square.sqvertex;
			NyARDoublePoint2d[] vp=i_prev_param.square.sqvertex;
			//頂点速度の計測
			for(int i=3;i>=0;i--){
				int x=(int)(vc[i].x-vp[i].x);
				int y=(int)(vc[i].y-vp[i].y);
				this.vertex_v[i].setValue(x,y);
			}
		}else{
			//頂点速度のリセット
			for(int i=3;i>=0;i--){
				this.vertex_v[i].x=this.vertex_v[i].y=0;
			}
		}
		return;
	}
	
	/**
	 * 輪郭情報を元に矩形パラメータを推定し、値をセットします。
	 * この関数は、処理の成功失敗に関わらず、内容変更を行います。
	 * @param i_contour_status
	 * 関数を実行すると、内容は破壊されます。
	 * @return
	 * @throws NyARException
	 */
	public boolean setValueWithInitialCheck(NyARContourTargetStatus i_contour_status,NyARIntRect i_sample_area) throws NyARException
	{
		//ベクトルのマージ(マージするときに、3,4象限方向のベクトルは1,2象限のベクトルに変換する。)
		this._ref_my_pool._vecpos_op.margeResembleCoords(i_contour_status.vecpos);
		if(i_contour_status.vecpos.length<4){
			return false;
		}
		//キーベクトルを取得
		i_contour_status.vecpos.getKeyCoord(this._ref_my_pool._indexbuf);
		//点に変換
		NyARSquare sq=this.square;
		if(!this._ref_my_pool._line_detect.line2SquareVertex(this._ref_my_pool._indexbuf,this.square.sqvertex)){
			return false;
		}
		//点から直線を再計算
		for(int i=3;i>=0;i--){
			sq.line[i].calculateLineWithNormalize(sq.sqvertex[i],sq.sqvertex[(i+1)%4]);
		}
		this.setDiffParam(null,false);
		return checkInitialRectCondition(i_sample_area);
	}
	/**
	 * 現在の矩形を元に、線分をトレースして、頂点を取得します。
	 * @param i_reader
	 * @return
	 * @throws NyARException 
	 */
	private boolean updateVertexParamByLineLog(NyARVectorReader_INT1D_GRAY_8 i_reader,NyARSquare i_prev_sq) throws NyARException
	{
		//4本のベクトルを計算
		NyARSquare sq=this.square;
NyARIntRect r=new NyARIntRect();
r.x=r.y=0;
r.h=240;
r.w=320;
		if(!clllip(i_reader,i_prev_sq,r)){
			return false;
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
		if(!updateVertexParamByLineLog(i_raster.getVectorReader(),i_prev_status.square)){
			return false;
		}
		//差分パラメータのセット
		setDiffParam(i_prev_status,false);
		return true;
	}
	
	
	/**
	 * 値をセットします。この関数は、処理の成功失敗に関わらず、内容変更を行います。
	 * @param i_contour_status
	 * @return
	 * @throws NyARException
	 */
	public boolean setValueWithDeilyCheck(LrlsGsRaster i_raster,LowResolutionLabelingSamplerOut.Item i_source,NyARRectTargetStatus i_prev_status) throws NyARException
	{
		VecLinear vecpos=this._ref_my_pool._vecpos;
		//輪郭線を取る
		if(!i_raster.getVectorReader().traceConture((LrlsGsRaster)i_source.ref_raster,i_source.lebeling_th,i_source.entry_pos,vecpos)){
			return false;
		}
		
		//ベクトルのマージ(マージするときに、3,4象限方向のベクトルは1,2象限のベクトルに変換する。)
		this._ref_my_pool._vecpos_op.margeResembleCoords(vecpos);
		if(vecpos.length<4){
			return false;
		}
		//キーベクトルを取得
		vecpos.getKeyCoord(this._ref_my_pool._indexbuf);
		//点に変換
		NyARSquare sq=this.square;
		if(!this._ref_my_pool._line_detect.line2SquareVertex(this._ref_my_pool._indexbuf,this.square.sqvertex)){
			return false;
		}
		//点から直線を再計算
		for(int i=3;i>=0;i--){
			sq.line[i].calculateLineWithNormalize(sq.sqvertex[i],sq.sqvertex[(i+1)%4]);
		}
		//差分パラメータのセット
		setDiffParam(i_prev_status,false);
		return checkDeilyRectCondition(i_prev_status);
	}
	/**
	 * このデータが初期チェック(CoordからRectへの遷移)をパスするかチェックします。
	 * 条件は、
	 *  1.検出四角形の対角点は元の検出矩形内か？
	 *  2.一番長い辺と短い辺の比は、0.1~10の範囲か？
	 *  3.位置倍長い辺、短い辺が短すぎないか？
	 * @param i_sample_area
	 * この矩形を検出するために使った元データの範囲(ラべリング検出領域)
	 */
	private boolean checkInitialRectCondition(NyARIntRect i_sample_area)
	{
		NyARDoublePoint2d[] v=this.square.sqvertex;

		//検出した四角形の対角点が検出エリア内か？
		int cx=(int)(v[0].x+v[1].x+v[2].x+v[3].x)/4;
		int cy=(int)(v[0].y+v[1].y+v[2].y+v[3].y)/4;
		if(!i_sample_area.isInnerPoint(cx,cy)){
			return false;
		}
		//一番長い辺と短い辺の比を確認(10倍の比があったらなんか変)
		int max=Integer.MIN_VALUE;
		int min=Integer.MAX_VALUE;
		for(int i=0;i<4;i++){
			int t=(int)v[i].sqNorm(v[(i+1)%4]);
			if(t>max){max=t;}
			if(t<min){min=t;}
		}
		//比率係数の確認
		if(max<(5*5) ||min<(5*5)){
			return false;
		}
		//10倍スケールの2乗
		if((10*10)*min/max<(5*5)){
			return false;
		}
		return true;
	}
	/**
	 * このデータが初期チェック(CoordからRectへの遷移)をパスするかチェックします。
	 * 条件は、
	 *  1.一番長い辺と短い辺の比は、0.1~10の範囲か？
	 *  2.位置倍長い辺、短い辺が短すぎないか？
	 *  3.移動距離が極端に大きなものは無いか？(他の物の3倍動いてたらおかしい)

	 * @param i_sample_area
	 */
	public boolean checkDeilyRectCondition(NyARRectTargetStatus i_prev_st)
	{
		NyARDoublePoint2d[] v=this.square.sqvertex;

		//一番長い辺と短い辺の比を確認(10倍の比があったらなんか変)
		int max=Integer.MIN_VALUE;
		int min=Integer.MAX_VALUE;
		for(int i=0;i<4;i++){
			int t=(int)v[i].sqNorm(v[(i+1)%4]);
			if(t>max){max=t;}
			if(t<min){min=t;}
		}
		//比率係数の確認
		if(max<(5*5) ||min<(5*5)){
			return false;
		}
		//10倍スケールの2乗
		if((10*10)*min/max<(5*5)){
			return false;
		}
		//4頂点の移動距離を確認
		for(int i=0;i<4;i++){
			i_prev
		}
		return true;
	}	
	private boolean clllip(NyARVectorReader_INT1D_GRAY_8 i_reader,NyARSquare i_prevsq,NyARIntRect i_rect) throws NyARException
	{
		VecLinear vecpos=this._ref_my_pool._vecpos;
		boolean p1,p2;
		//NyARIntRect i_rect
		for(int i=0;i<4;i++)
		{
			//線分が範囲内にあるかを確認
			p1=i_rect.isInnerPoint(i_prevsq.sqvertex[i]);
			p2=i_rect.isInnerPoint(i_prevsq.sqvertex[(i+3)%4]);
			//個数で分岐
			if(p1 && p2){
				//2ならクリッピング必要なし。
if(!i_reader.traceLine(i_prevsq.sqvertex[i],i_prevsq.sqvertex[(i+1)%4],3,vecpos)){
					return false;
				}
			}else if(p1!=p2){
				//1ならクリッピング後に、外に出ていた点に近い輪郭交点を得る。
				if(p1){
					//p2が範囲外
					return false;
				}else{
					//p1が範囲外
					return false;
				}
			}else{
				//0ならクリッピングして得られた２点を使う。
			}
			//クラスタリングして、傾きの近いベクトルを探す。(限界は10度)
			this._ref_my_pool._vecpos_op.margeResembleCoords(vecpos);
			//基本的には1番でかいベクトルだよね。だって、直線状に取るんだもの。
			int vid=vecpos.getMaxCoordIndex();
			if(vecpos.items[vid].getAbsVecCos(-i_prevsq.line[i].b,i_prevsq.line[i].a)>NyARMath.COS_DEG_10){
				this.square.line[i].setVectorWithNormalize(vecpos.items[vid]);
				//同定OK
			}else{
				return false;
				//同定NG
			}
		}
		return true;
	}
	/**
	 * 直線式とRECTから、クリップされた線分を計算します。
	 * @param a
	 * 直線の方程式。
	 * @param i_rect
	 * 制限RECT
	 * @param o_point
	 * 点の座標は、RECT.X<N<RECT.X+RECT.W,RECT.H<M<RECT.Y+RECT.Hに制限されます。
	 * @return
	 * 線分が作れない。
	 */
	public boolean clip(NyARLinear a,NyARIntRect i_rect,NyARIntPoint2d[] o_point)
	{	
		int idx=0;
		NyARIntPoint2d ptr=o_point[0];
		if(a.crossPos(0,-1,i_rect.y,ptr) && ptr.x>=i_rect.x && ptr.x<(i_rect.x+i_rect.w))
		{
			//y=rect.yの線
			idx++;
		}
		ptr=o_point[idx];
		if(a.crossPos(0,-1,i_rect.y+i_rect.h-1,ptr) && ptr.x>=i_rect.x && ptr.x<(i_rect.x+i_rect.w))
		{
			//y=(rect.y+rect.h-1)の線
			idx++;
		}
		if(idx==2){
			return true;
		}
		ptr=o_point[idx];
		if(a.crossPos(-1,0,i_rect.x,ptr) && ptr.y>=i_rect.y && ptr.y<(i_rect.y+i_rect.w))
		{
			//y=rect.yの線
			idx++;
		}
		if(idx==2){
			return true;
		}
		ptr=o_point[idx];
		if(a.crossPos(-1,0,i_rect.x+i_rect.w-1, ptr) && ptr.y>=i_rect.y && ptr.y<(i_rect.y+i_rect.w))
		{
			//y=(rect.y+=rect.h)の線
			idx++;
		}
		if(idx==2){
			return true;
		}
		return false;
	}
}