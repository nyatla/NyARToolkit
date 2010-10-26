package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status;

import java.math.MathContext;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.NyARVecLinear2d;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LrlsGsRaster;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.dev.rpf.utils.VecLinearCoordinates;




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
	 * 予想した頂点速度の二乗値の合計
	 */
	public int estimate_sum_sq_vertex_velocity_ave;
	/**
	 * 予想頂点位置
	 */
	/**
	 * 予想頂点範囲
	 */
	public NyARIntRect estimate_rect=new NyARIntRect();
	public NyARDoublePoint2d[] estimate_vertex=NyARDoublePoint2d.createArray(4);

	/**
	 * 最後に使われた検出タイプの値です。
	 */
	public int detect_type;
	/**
	 * 初期矩形検出で検出を実行した。
	 */
	public static final int DT_SQINIT=0;
	/**
	 * 定常矩形検出で検出を実行した。
	 */
	public static final int DT_SQDAILY=1;
	/**
	 * 定常直線検出で検出を実行した。
	 */
	public static final int DT_LIDAILY=2;
	/**
	 * みつからなかったよ。
	 */
	public static final int DT_FAILED=-1;
	
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
		this.detect_type=DT_SQINIT;
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
		int sum_of_vertex_sq_dist=0;
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
				sum_of_vertex_sq_dist+=x*x+y*y;
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
		this.estimate_sum_sq_vertex_velocity_ave=sum_of_vertex_sq_dist/4;
		this.estimate_rect.setAreaRect(ve_ptr,4);
//		this.estimate_rect.clip(i_left, i_top, i_right, i_bottom);
		return;
	}
	
	/**
	 * 輪郭情報を元に矩形パラメータを推定し、値をセットします。
	 * この関数は、処理の成功失敗に関わらず、内容変更を行います。
	 * @param i_contour_status
	 * 関数を実行すると、このオブジェクトの内容は破壊されます。
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
		NyARSquare this_sq=this.square;
		if(!this._ref_my_pool._line_detect.line2SquareVertex(this._ref_my_pool._indexbuf,this_sq.sqvertex)){
			return false;
		}
		//点から直線を再計算
		for(int i=3;i>=0;i--){
			this_sq.line[i].makeLinearWithNormalize(this_sq.sqvertex[i],this_sq.sqvertex[(i+1)%4]);
		}
		this.setEstimateParam(null);
		if(!checkInitialRectCondition(i_sample_area))
		{
			return false;
		}
		this.detect_type=DT_SQINIT;
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
		NyARSquare this_sq=this.square;
		if(!this._ref_my_pool._line_detect.line2SquareVertex(this._ref_my_pool._indexbuf,this_sq.sqvertex)){
			return false;
		}
		//点から直線を再計算
		for(int i=3;i>=0;i--){
			this_sq.line[i].makeLinearWithNormalize(this_sq.sqvertex[i],this_sq.sqvertex[(i+1)%4]);
		}
		//頂点並び順の調整
		this_sq.rotateVertexL(i_prev_status.square.checkVertexShiftValue(this_sq));		

		//パラメタチェック
		if(!checkDeilyRectCondition(i_prev_status)){
			return false;
		}
		//次回の予測
		setEstimateParam(i_prev_status);
		return true;
	}

	/**
	 * 輪郭からの単独検出
	 * @param i_raster
	 * @param i_prev_status
	 * @return
	 * @throws NyARException
	 */
	public boolean setValueByLineLog(LrlsGsRaster i_raster,NyARRectTargetStatus i_prev_status) throws NyARException
	{
		//検出範囲からカーネルサイズの2乗値を計算。検出領域の二乗距離の1/(40*40) (元距離の1/40)
		int d=((int)i_prev_status.estimate_rect.getDiagonalSqDist()/(NyARMath.SQ_40));
		//二乗移動速度からカーネルサイズを計算。
		int v_ave_limit=i_prev_status.estimate_sum_sq_vertex_velocity_ave;
		//
		if(v_ave_limit>d){
			//移動カーネルサイズより、検出範囲カーネルのほうが大きかったらエラー(動きすぎ)
			return false;
		}
		d=(int)Math.sqrt(d);
		//最低でも2だよね。
		if(d<2){
			d=2;
		}
		//最大カーネルサイズ(5)を超える場合は5にする。
		if(d>5){
			d=5;
		}
		
		//ライントレースの試行
		NyARSquare sq=this.square;
		if(!traceSquare(i_raster.getVectorReader(),d,i_prev_status)){
			//System.out.println(">>>>>>>>>>>>>"+v_ave_limit+","+d);
			return false;
		}else{
		}

		//4点抽出
		for(int i=3;i>=0;i--){
			if(!sq.line[i].crossPos(sq.line[(i + 3) % 4],sq.sqvertex[i])){
				//四角が作れない。
				return false;
			}
		}		

		//頂点並び順の調整
		this.square.rotateVertexL(i_prev_status.square.checkVertexShiftValue(this.square));		
		//差分パラメータのセット
		setEstimateParam(i_prev_status);
		return true;
	}
	/**
	 * 状況に応じて矩形選択手法を切り替えます。
	 * @param i_raster
	 * @param i_source
	 * @param i_prev_status
	 * @return
	 * @throws NyARException
	 */
	public boolean setValueByAutoSelect(LrlsGsRaster i_raster,LowResolutionLabelingSamplerOut.Item i_source,NyARRectTargetStatus i_prev_status) throws NyARException
	{
		int current_detect_type=DT_SQDAILY;
		//移動速度による手段の切り替え
		int sq_v_ave_limit=i_prev_status.estimate_sum_sq_vertex_velocity_ave/4;
		//速度が小さい時か、前回LineLogが成功したときはDT_LIDAILY
		if(((sq_v_ave_limit<3) && (i_prev_status.detect_type==DT_SQDAILY)) || (i_prev_status.detect_type==DT_LIDAILY)){
			current_detect_type=DT_LIDAILY;
		}
		
		//前回の動作ログによる手段の切り替え
		switch(current_detect_type)
		{
		case DT_LIDAILY:
			//LineLog->
			if(setValueByLineLog(i_raster,i_prev_status))
			{
				//うまくいった。
				this.detect_type=DT_LIDAILY;
				return true;
			}
			if(i_source!=null){
				if(setValueWithDeilyCheck(i_raster,i_source,i_prev_status))
				{
					//うまくいった
					this.detect_type=DT_SQDAILY;
					return true;
				}
			}
			break;
		case DT_SQDAILY:
			if(i_source!=null){
				if(setValueWithDeilyCheck(i_raster,i_source,i_prev_status))
				{
					this.detect_type=DT_SQDAILY;
					return true;
				}
			}
			break;
		default:
			break;
		}
		//前回の動作ログを書き換え
		i_prev_status.detect_type=DT_FAILED;
		return false;
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
		return this._ref_my_pool.checkLargeDiff(this.square.sqvertex,i_prev_st.square.sqvertex);
	}

	/**
	 * 予想位置を基準に四角形をトレースして、一定の基準をクリアするかを評価します。
	 * @param i_reader
	 * @param i_edge_size
	 * @param i_prevsq
	 * @return
	 * @throws NyARException
	 */
	private boolean traceSquare(NyARVectorReader_INT1D_GRAY_8 i_reader,int i_edge_size,NyARRectTargetStatus i_prevsq) throws NyARException
	{
		NyARDoublePoint2d p1,p2;
		VecLinearCoordinates vecpos=this._ref_my_pool._vecpos;
		//NyARIntRect i_rect
		p1=i_prevsq.estimate_vertex[0];
		int dist_limit=i_edge_size*i_edge_size*2;
		//強度敷居値(セルサイズ-1)
//		int min_th=i_edge_size*2+1;
//		min_th=(min_th*min_th);
		for(int i=0;i<4;i++)
		{
			p2=i_prevsq.estimate_vertex[(i+1)%4];
			
			//クリップ付きで予想位置周辺の直線のトレース
			i_reader.traceLineWithClip(p1,p2,i_edge_size,vecpos);

			//クラスタリングして、傾きの近いベクトルを探す。(限界は10度)
			this._ref_my_pool._vecpos_op.margeResembleCoords(vecpos);
			//基本的には1番でかいベクトルだよね。だって、直線状に取るんだもの。

			int vid=vecpos.getMaxCoordIndex();
			//データ品質規制(強度が多少強くないと。)
//			if(vecpos.items[vid].sq_dist<(min_th)){
//				return false;
//			}
//@todo:パラメタ調整
			//角度規制(元の線分との角度を確認)
			if(vecpos.items[vid].getAbsVecCos(-i_prevsq.square.line[i].b,i_prevsq.square.line[i].a)<NyARMath.COS_DEG_8){
				//System.out.println("CODE1");
				return false;
			}
//@todo:パラメタ調整
			//予想点からさほど外れていない点であるか。(検出点の移動距離を計算する。)
			double dist;
			dist=vecpos.items[vid].sqDistBySegmentLineEdge(i_prevsq.square.sqvertex[i],i_prevsq.square.sqvertex[i%4]);
			if(dist<dist_limit){
				this.square.line[i].setVectorWithNormalize(vecpos.items[vid]);
			}else{
				//System.out.println("CODE2:"+dist+","+dist_limit);
				return false;
			}
			//頂点ポインタの移動
			p1=p2;
		}
		return true;
	}
}