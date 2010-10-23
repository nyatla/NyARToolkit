package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.NyARVecLinear2d;
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
	/**
	 * 予想頂点位置
	 */
	/**
	 * 予想頂点範囲
	 */
	public NyARIntRect estimate_rect=new NyARIntRect();
	public NyARDoublePoint2d[] estimate_vertex=NyARDoublePoint2d.createArray(4);

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

	private void setEstimateParam(NyARRectTargetStatus i_prev_param)
	{
		NyARDoublePoint2d[] vc_ptr=this.square.sqvertex;
		NyARDoublePoint2d[] ve_ptr=this.estimate_vertex;
		if(i_prev_param!=null){
			//差分パラメータをセット
			NyARDoublePoint2d[] vp=i_prev_param.square.sqvertex;
			//頂点速度の計測
			for(int i=3;i>=0;i--){
				int x=(int)((vc_ptr[i].x-vp[i].x));
				int y=(int)((vc_ptr[i].y-vp[i].y));
				this.vertex_v[i].x=x;//+this.vertex_v[i].x)/2;
				this.vertex_v[i].y=y;//+this.vertex_v[i].y)/2;
				//予想位置
				ve_ptr[i].x=(int)vc_ptr[i].x+x;
				ve_ptr[i].y=(int)vc_ptr[i].y+y;
			}
		}else{
			//頂点速度のリセット
			for(int i=3;i>=0;i--){
				this.vertex_v[i].x=this.vertex_v[i].y=0;
				ve_ptr[i].x=(int)vc_ptr[i].x;
				ve_ptr[i].y=(int)vc_ptr[i].y;
			}
		}
		//頂点予測と範囲予測
		this.estimate_rect.setAreaRect(ve_ptr,4);
//		this.estimate_rect.clip(i_left, i_top, i_right, i_bottom);
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
		i_contour_status.vecpos.limitQuadrantTo12();
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
			sq.line[i].makeLinearWithNormalize(sq.sqvertex[i],sq.sqvertex[(i+1)%4]);
		}
		this.setEstimateParam(null);
		return checkInitialRectCondition(i_sample_area);
	}
	/**
	 * 値をセットします。この関数は、処理の成功失敗に関わらず、内容変更を行います。
	 * @param i_contour_status
	 * @return
	 * @throws NyARException
	 */
	public boolean setValueWithDeilyCheck(LrlsGsRaster i_raster,LowResolutionLabelingSamplerOut.Item i_source,NyARRectTargetStatus i_prev_status) throws NyARException
	{
		VecLinearCoordinates vecpos=this._ref_my_pool._vecpos;
		//輪郭線を取る
		if(!i_raster.getVectorReader().traceConture((LrlsGsRaster)i_source.ref_raster,i_source.lebeling_th,i_source.entry_pos,vecpos)){
			return false;
		}
		//3,4象限方向のベクトルは1,2象限のベクトルに変換する。
		vecpos.limitQuadrantTo12();
		//ベクトルのマージ
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
			sq.line[i].makeLinearWithNormalize(sq.sqvertex[i],sq.sqvertex[(i+1)%4]);
		}
		//頂点並び順の調整
		this.square.rotateVertexL(i_prev_status.square.checkVertexShiftValue(this.square));		

		//パラメタチェック
		if(!checkDeilyRectCondition(i_prev_status)){
			return false;
		}
		//次回の予測
		setEstimateParam(i_prev_status);

		return true;
	}
	/**
	 * 現在の矩形を元に、線分をトレースして、頂点を取得します。
	 * @param i_reader
	 * @return
	 * @throws NyARException 
	 */
	private boolean updateVertexParamByLineLog(NyARVectorReader_INT1D_GRAY_8 i_reader,NyARRectTargetStatus i_prev_sq) throws NyARException
	{
		//4本のベクトルを計算
		NyARSquare sq=this.square;
//現在の速度と認識対象の大きさから、カーネルサイズを決定。
		if(!clllip(i_reader,i_prev_sq)){
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
		if(!updateVertexParamByLineLog(i_raster.getVectorReader(),i_prev_status)){
			return false;
		}
		//頂点並び順の調整
		this.square.rotateVertexL(i_prev_status.square.checkVertexShiftValue(this.square));		
		//差分パラメータのセット
		setEstimateParam(i_prev_status);
		return true;
	}
	
	

	/**
	 * このデータが初期チェック(CoordからRectへの遷移)をパスするかチェックします。
	 * 条件は、
	 *  1.検出四角形の対角点は元の検出矩形内か？
	 *  2.一番長い辺と短い辺の比は、0.1~10の範囲か？
	 *  3.位置倍長い辺、短い辺が短すぎないか？
	 * @param i_sample_area
	 * この矩形を検出するために使った元データの範囲(ターゲット検出範囲)
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
		if((10*10)*min/max<(3*3)){
			return false;
		}
		return true;
	}
	/**
	 * 2回目以降の履歴を使ったデータチェック。
	 * 条件は、
	 *  1.一番長い辺と短い辺の比は、0.1~10の範囲か？
	 *  2.位置倍長い辺、短い辺が短すぎないか？
	 *  3.移動距離が極端に大きなものは無いか？(他の物の3倍動いてたらおかしい)

	 * @param i_sample_area
	 */
	private boolean checkDeilyRectCondition(NyARRectTargetStatus i_prev_st)
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
		if((10*10)*min/max<(3*3)){
			return false;
		}
		//移動距離平均より大きく剥離した点が無いか確認
		return check1PointMove(this.square.sqvertex,i_prev_st.square.sqvertex);
	}
	/**
	 * 頂点の移動距離の平均から、認識ミスを推測(あってｍのなくてもかわんなくね？)
	 * @param i_point1
	 * @param i_point2
	 * @return
	 */
	private boolean check1PointMove(NyARDoublePoint2d[] i_point1,NyARDoublePoint2d[] i_point2)
	{
int[] sq_tbl=new int[4];
		int ave=0;
		for(int i=3;i>=0;i--){
			sq_tbl[i]=(int)i_point1[i].sqNorm(i_point2[i]);
			ave+=sq_tbl[i];
		}
		ave/=4;
		if(ave==0){
			return true;
		}
		for(int i=0;i<4;i++){
			//平均から2倍離れてるのはおかしい。
			if(sq_tbl[i]>ave*(2)){
				return false;
			}
		}
		return true;
	}
	/**/
	private boolean clllip(NyARVectorReader_INT1D_GRAY_8 i_reader,NyARRectTargetStatus i_prevsq) throws NyARException
	{
		NyARDoublePoint2d p1,p2;
		VecLinearCoordinates vecpos=this._ref_my_pool._vecpos;
		//NyARIntRect i_rect
		p1=i_prevsq.estimate_vertex[0];
		for(int i=0;i<4;i++)
		{
			p2=i_prevsq.estimate_vertex[(i+1)%4];
			
			//クリップ付きで予想位置周辺の直線のトレース
			i_reader.traceLineWithClip(p1,p2,5,vecpos);

			//クラスタリングして、傾きの近いベクトルを探す。(限界は10度)
			this._ref_my_pool._vecpos_op.margeResembleCoords(vecpos);
			//基本的には1番でかいベクトルだよね。だって、直線状に取るんだもの。

			int vid=vecpos.getMaxCoordIndex();
			//データ品質規制(強度が多少強くないと。)
			if(vecpos.items[vid].sq_dist<10000){
				return false;
			}

			
			//角度規制
			if(vecpos.items[vid].getAbsVecCos(-i_prevsq.square.line[i].b,i_prevsq.square.line[i].a)<NyARMath.COS_DEG_10){
				return false;
			}
			//予想点からさほど外れていない点であるか。(検出点の移動距離を計算する。)
			double dist;
			dist=vecpos.items[vid].sqDistBySegmentLineEdge(i_prevsq.square.sqvertex[i],i_prevsq.square.sqvertex[i%4]);
			if(dist<2*3*3){
				this.square.line[i].setVectorWithNormalize(vecpos.items[vid]);
			}else{
				return false;
			}
			//頂点のスライド
			p1=p2;
		}
		return true;
	}
}