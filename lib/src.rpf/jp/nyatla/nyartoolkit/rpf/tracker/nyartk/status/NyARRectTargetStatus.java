package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.INyARVectorReader;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.rpf.utils.VecLinearCoordinates;




public class NyARRectTargetStatus extends NyARTargetStatus
{
	private NyARRectTargetStatusPool _ref_my_pool;
	
	
	/**
	 * 現在の矩形情報
	 */
	public NyARDoublePoint2d[] vertex=NyARDoublePoint2d.createArray(4);

	/**
	 * 予想した頂点速度の二乗値の合計
	 */
	public int estimate_sum_sq_vertex_velocity_ave;

	/**
	 * 予想頂点範囲
	 */
	public NyARIntRect estimate_rect=new NyARIntRect();
	/**
	 * 予想頂点位置
	 */
	public NyARDoublePoint2d[] estimate_vertex=NyARDoublePoint2d.createArray(4);

	/**
	 * 最後に使われた検出タイプの値です。DT_xxxの値をとります。
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
		this.detect_type=DT_SQINIT;
	}

	/**
	 * 前回のステータスと予想パラメータを計算してセットします。
	 * @param i_prev_param
	 */
	private final void setEstimateParam(NyARRectTargetStatus i_prev_param)
	{
		NyARDoublePoint2d[] vc_ptr=this.vertex;
		NyARDoublePoint2d[] ve_ptr=this.estimate_vertex;
		int sum_of_vertex_sq_dist=0;
		if(i_prev_param!=null){
			//差分パラメータをセット
			NyARDoublePoint2d[] vp=i_prev_param.vertex;
			//頂点速度の計測
			for(int i=3;i>=0;i--){
				int x=(int)((vc_ptr[i].x-vp[i].x));
				int y=(int)((vc_ptr[i].y-vp[i].y));
				//予想位置
				ve_ptr[i].x=(int)vc_ptr[i].x+x;
				ve_ptr[i].y=(int)vc_ptr[i].y+y;
				sum_of_vertex_sq_dist+=x*x+y*y;
			}
		}else{
			//頂点速度のリセット
			for(int i=3;i>=0;i--){
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
		NyARDoublePoint2d[] this_vx=this.vertex;
		if(!this._ref_my_pool._line_detect.line2SquareVertex(this._ref_my_pool._indexbuf,this_vx)){
			return false;
		}
		
//		//点から直線を再計算
//		for(int i=3;i>=0;i--){
//			this_sq.line[i].makeLinearWithNormalize(this_sq.sqvertex[i],this_sq.sqvertex[(i+1)%4]);
//		}
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
	 * @param i_sampler_in
	 * @param i_source
	 * @param i_prev_status
	 * @return
	 * @throws NyARException
	 */
	public boolean setValueWithDeilyCheck(INyARVectorReader i_vec_reader,LowResolutionLabelingSamplerOut.Item i_source,NyARRectTargetStatus i_prev_status) throws NyARException
	{
		VecLinearCoordinates vecpos=this._ref_my_pool._vecpos;
		//輪郭線を取る
		if(!i_vec_reader.traceConture(i_source.lebeling_th,i_source.entry_pos,vecpos)){
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
		NyARDoublePoint2d[] this_vx=this.vertex;
		if(!this._ref_my_pool._line_detect.line2SquareVertex(this._ref_my_pool._indexbuf,this_vx)){
			return false;
		}
		//頂点並び順の調整
		rotateVertexL(this.vertex,checkVertexShiftValue(i_prev_status.vertex,this.vertex));	

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
	public boolean setValueByLineLog(INyARVectorReader i_vec_reader,NyARRectTargetStatus i_prev_status) throws NyARException
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

		NyARLinear[] sh_l=this._ref_my_pool._line;
		if(!traceSquareLine(i_vec_reader,d,i_prev_status,sh_l)){
			return false;
		}else{
		}
		//4点抽出
		for(int i=3;i>=0;i--){
			if(!sh_l[i].crossPos(sh_l[(i + 3) % 4],this.vertex[i])){
				//四角が作れない。
				return false;
			}
		}		

		//頂点並び順の調整
		rotateVertexL(this.vertex,checkVertexShiftValue(i_prev_status.vertex,this.vertex));	
		//差分パラメータのセット
		setEstimateParam(i_prev_status);
		return true;
	}
	/**
	 * 状況に応じて矩形選択手法を切り替えます。
	 * @param i_vec_reader
	 * サンプリングデータの基本画像にリンクしたVectorReader
	 * @param i_source
	 * サンプリングデータ
	 * @param i_prev_status
	 * 前回の状態を格納したオブジェクト
	 * @return
	 * @throws NyARException
	 */
	public boolean setValueByAutoSelect(INyARVectorReader i_vec_reader,LowResolutionLabelingSamplerOut.Item i_source,NyARRectTargetStatus i_prev_status) throws NyARException
	{
		int current_detect_type=DT_SQDAILY;
		//移動速度による手段の切り替え
		int sq_v_ave_limit=i_prev_status.estimate_sum_sq_vertex_velocity_ave/4;
		//速度が小さい時か、前回LineLogが成功したときはDT_LIDAILY
		if(((sq_v_ave_limit<10) && (i_prev_status.detect_type==DT_SQDAILY)) || (i_prev_status.detect_type==DT_LIDAILY)){
			current_detect_type=DT_LIDAILY;
		}
		
		//前回の動作ログによる手段の切り替え
		switch(current_detect_type)
		{
		case DT_LIDAILY:
			//LineLog->
			if(setValueByLineLog(i_vec_reader,i_prev_status))
			{
				//うまくいった。
				this.detect_type=DT_LIDAILY;
				return true;
			}
			if(i_source!=null){
				if(setValueWithDeilyCheck(i_vec_reader,i_source,i_prev_status))
				{
					//うまくいった
					this.detect_type=DT_SQDAILY;
					return true;
				}
			}
			break;
		case DT_SQDAILY:
			if(i_source!=null){
				if(setValueWithDeilyCheck(i_vec_reader,i_source,i_prev_status))
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
		NyARDoublePoint2d[] this_vx=this.vertex;

		//検出した四角形の対角点が検出エリア内か？
		int cx=(int)(this_vx[0].x+this_vx[1].x+this_vx[2].x+this_vx[3].x)/4;
		int cy=(int)(this_vx[0].y+this_vx[1].y+this_vx[2].y+this_vx[3].y)/4;
		if(!i_sample_area.isInnerPoint(cx,cy)){
			return false;
		}

		
		//一番長い辺と短い辺の比を確認(10倍の比があったらなんか変)
		int max=Integer.MIN_VALUE;
		int min=Integer.MAX_VALUE;
		for(int i=0;i<4;i++){
			int t=(int)this_vx[i].sqDist(this_vx[(i+1)%4]);
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
		NyARDoublePoint2d[] this_vx=this.vertex;

		//一番長い辺と短い辺の比を確認(10倍の比があったらなんか変)
		int max=Integer.MIN_VALUE;
		int min=Integer.MAX_VALUE;
		for(int i=0;i<4;i++){
			int t=(int)this_vx[i].sqDist(this_vx[(i+1)%4]);
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
		return this._ref_my_pool.checkLargeDiff(this_vx,i_prev_st.vertex);
	}

	/**
	 * 予想位置を基準に四角形をトレースして、一定の基準をクリアするかを評価します。
	 * @param i_reader
	 * @param i_edge_size
	 * @param i_prevsq
	 * @return
	 * @throws NyARException
	 */
	private boolean traceSquareLine(INyARVectorReader i_reader,int i_edge_size,NyARRectTargetStatus i_prevsq,NyARLinear[] o_line) throws NyARException
	{
		NyARDoublePoint2d p1,p2;
		VecLinearCoordinates vecpos=this._ref_my_pool._vecpos;
		//NyARIntRect i_rect
		p1=i_prevsq.estimate_vertex[0];
		int dist_limit=i_edge_size*i_edge_size;
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
			if(vecpos.items[vid].getAbsVecCos(i_prevsq.vertex[i],i_prevsq.vertex[(i+1)%4])<NyARMath.COS_DEG_5){
				//System.out.println("CODE1");
				return false;
			}
//@todo:パラメタ調整
			//予想点からさほど外れていない点であるか。(検出点の移動距離を計算する。)
			double dist;
			dist=vecpos.items[vid].sqDistBySegmentLineEdge(i_prevsq.vertex[i],i_prevsq.vertex[i]);
			if(dist<dist_limit){
				o_line[i].setVectorWithNormalize(vecpos.items[vid]);
			}else{
				//System.out.println("CODE2:"+dist+","+dist_limit);
				return false;
			}
			//頂点ポインタの移動
			p1=p2;
		}
		return true;
	}
    /**
     * 頂点同士の距離から、頂点のシフト量を返します。この関数は、よく似た２つの矩形の頂点同士の対応を取るために使用します。
     * @param i_square
     * 比較対象の矩形
     * @return
     * シフト量を数値で返します。
     * シフト量はthis-i_squareです。1の場合、this.sqvertex[0]とi_square.sqvertex[1]が対応点になる(shift量1)であることを示します。
     */
    private final static int checkVertexShiftValue(NyARDoublePoint2d[] i_vertex1,NyARDoublePoint2d[] i_vertex2)
    {
    	assert(i_vertex1.length==4 && i_vertex2.length==4);
    	//3-0番目
    	int min_dist=Integer.MAX_VALUE;
    	int min_index=0;
    	int xd,yd;
    	for(int i=3;i>=0;i--){
    		int d=0;
    		for(int i2=3;i2>=0;i2--){
    			xd= (int)(i_vertex1[i2].x-i_vertex2[(i2+i)%4].x);
    			yd= (int)(i_vertex1[i2].y-i_vertex2[(i2+i)%4].y);
    			d+=xd*xd+yd*yd;
    		}
    		if(min_dist>d){
    			min_dist=d;
    			min_index=i;
    		}
    	}
    	return min_index;
    }
    /**
     * 4とnの最大公約数テーブル
     */
    private final static int[] _gcd_table4={-1,1,2,1};
    /**
     * 頂点を左回転して、矩形を回転させます。
     * @param i_shift
     */
    private final static void rotateVertexL(NyARDoublePoint2d[] i_vertex,int i_shift)
    {
    	assert(i_shift<4);
    	NyARDoublePoint2d vertext;
    	if(i_shift==0){
    		return;
    	}
    	int t1,t2;
    	int d, i, j, mk;
	    int ll=4-i_shift;
	    d = _gcd_table4[ll];//NyMath.gcn(4,ll);
	    mk = (4-ll) % 4;
	    for (i = 0; i < d; i++) {
	    	vertext=i_vertex[i];
	        for (j = 1; j < 4/d; j++) {
	            t1=(i + (j-1)*mk) % 4;
	            t2=(i + j*mk) % 4;
	            i_vertex[t1]=i_vertex[t2];
	        }
	        t1=(i + ll) % 4;
	        i_vertex[t1]=vertext;
	    }
    }
    /**
     * ARToolKitのdirectionモデルに従って、頂点をシフトします。
     * @param i_dir
     */
    public void shiftByArtkDirection(int i_dir)
    {
    	rotateVertexL(this.estimate_vertex,i_dir);
    	rotateVertexL(this.vertex,i_dir);
    }
}