package jp.nyatla.nyartoolkit.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntCoordinates;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.rpf.utils.VecLinearCoordinates;

/**
 * NyARVectorReaderインタフェイスのうち、バッファフォーマットに依存しない関数を実装するクラス。
 * 派生クラスで画素アクセス関数を実装して、最終的なクラスに仕上げます。
 *
 */
public abstract class NyARVectorReader_Base implements INyARVectorReader
{
	private VecLinearCoordinates.VecLinearCoordinatePoint[] _tmp_coord_pos;
	private int _rob_resolution;
	protected NyARGrayscaleRaster _ref_base_raster;
	private NyARGrayscaleRaster _ref_rob_raster;
	protected NyARCameraDistortionFactor _factor;
	/**
	 * 継承を必須とするため、コンストラクタを隠蔽。
	 */
	protected NyARVectorReader_Base()
	{
		
	}
	
	/**
	 * 継承クラスのコンストラクタから呼び出す。
	 * @param i_ref_raster
	 * 基本画像
	 * @param i_ref_raster_distortion
	 * 歪み解除オブジェクト(nullの場合歪み解除を省略)
	 * @param i_ref_rob_raster
	 * エッジ探索用のROB画像
	 * @param i_contour_pickup
	 * 輪郭線取得クラス
	 * @param 
	 */
	public void initInstance(NyARGrayscaleRaster i_ref_raster,NyARCameraDistortionFactor i_ref_raster_distortion,NyARGrayscaleRaster i_ref_rob_raster,NyARContourPickup i_contour_pickup)
	{
		this._rob_resolution=i_ref_raster.getWidth()/i_ref_rob_raster.getWidth();
		this._ref_rob_raster=i_ref_rob_raster;
		this._ref_base_raster=i_ref_raster;
		this._coord_buf = new NyARIntCoordinates((i_ref_raster.getWidth() + i_ref_raster.getHeight()) * 4);
		this._factor=i_ref_raster_distortion;
		this._tmp_coord_pos=VecLinearCoordinates.VecLinearCoordinatePoint.createArray(this._coord_buf.items.length);
		this._cpickup = i_contour_pickup;
		return;
	}
	/**
	 * ワーク変数
	 */
	protected NyARIntCoordinates _coord_buf;
	private NyARContourPickup _cpickup;
	protected final double _MARGE_ANG_TH = NyARMath.COS_DEG_10;

	public boolean traceConture(int i_th,
			NyARIntPoint2d i_entry, VecLinearCoordinates o_coord)
			throws NyARException
	{
		NyARIntCoordinates coord = this._coord_buf;
		// Robertsラスタから輪郭抽出
		if (!this._cpickup.getContour(this._ref_rob_raster, i_th, i_entry.x, i_entry.y,
				coord)) {
			// 輪郭線MAXならなにもできないね。
			return false;

		}
		// 輪郭線のベクトル化
		return traceConture(coord, this._rob_resolution,
				this._rob_resolution * 2, o_coord);
	}



	/**
	 * 点1と点2の間に線分を定義して、その線分上のベクトルを得ます。点は、画像の内側でなければなりません。 320*240の場合、(x>=0 &&
	 * x<320 x+w>0 && x+w<320),(y>0 && y<240 y+h>=0 && y+h<=319)となります。
	 * 
	 * @param i_pos1
	 *            点1の座標です。
	 * @param i_pos2
	 *            点2の座標です。
	 * @param i_area
	 *            ベクトルを検出するカーネルサイズです。1の場合(n*2-1)^2のカーネルになります。 点2の座標です。
	 * @param o_coord
	 *            結果を受け取るオブジェクトです。
	 * @return
	 * @throws NyARException
	 */
	public boolean traceLine(NyARIntPoint2d i_pos1, NyARIntPoint2d i_pos2,int i_edge, VecLinearCoordinates o_coord)
	{
		NyARIntCoordinates coord = this._coord_buf;
		NyARIntSize base_s=this._ref_base_raster.getSize();
		// (i_area*2)の矩形が範囲内に収まるように線を引く
		// 移動量

		// 点間距離を計算
		int dist = (int) Math.sqrt(i_pos1.sqDist(i_pos2));
		// 最低AREA*2以上の大きさが無いなら、ラインのトレースは不可能。
		if (dist < 4) {
			return false;
		}
		// dist最大数の決定
		if (dist > 12) {
			dist = 12;
		}
		// サンプリングサイズを決定(移動速度とサイズから)
		int s = i_edge * 2 + 1;
		int dx = (i_pos2.x - i_pos1.x);
		int dy = (i_pos2.y - i_pos1.y);
		int r = base_s.w - s;
		int b = base_s.h - s;

		// 最大14点を定義して、そのうち両端を除いた点を使用する。
		for (int i = 1; i < dist - 1; i++) {
			int x = i * dx / dist + i_pos1.x - i_edge;
			int y = i * dy / dist + i_pos1.y - i_edge;
			// limit
			coord.items[i - 1].x = x < 0 ? 0 : (x >= r ? r : x);
			coord.items[i - 1].y = y < 0 ? 0 : (y >= b ? b : y);
		}

		coord.length = dist - 2;
		// 点数は20点程度を得る。
		return traceConture(coord, 1, s, o_coord);
	}

	public boolean traceLine(NyARDoublePoint2d i_pos1,NyARDoublePoint2d i_pos2, int i_edge, VecLinearCoordinates o_coord)
	{
		NyARIntCoordinates coord = this._coord_buf;
		NyARIntSize base_s=this._ref_base_raster.getSize();
		// (i_area*2)の矩形が範囲内に収まるように線を引く
		// 移動量

		// 点間距離を計算
		int dist = (int) Math.sqrt(i_pos1.sqDist(i_pos2));
		// 最低AREA*2以上の大きさが無いなら、ラインのトレースは不可能。
		if (dist < 4) {
			return false;
		}
		// dist最大数の決定
		if (dist > 12) {
			dist = 12;
		}
		// サンプリングサイズを決定(移動速度とサイズから)
		int s = i_edge * 2 + 1;
		int dx = (int) (i_pos2.x - i_pos1.x);
		int dy = (int) (i_pos2.y - i_pos1.y);
		int r = base_s.w - s;
		int b = base_s.h - s;

		// 最大24点を定義して、そのうち両端の2個を除いた点を使用する。
		for (int i = 1; i < dist - 1; i++) {
			int x = (int) (i * dx / dist + i_pos1.x - i_edge);
			int y = (int) (i * dy / dist + i_pos1.y - i_edge);
			// limit
			coord.items[i - 1].x = x < 0 ? 0 : (x >= r ? r : x);
			coord.items[i - 1].y = y < 0 ? 0 : (y >= b ? b : y);
		}

		coord.length = dist - 2;
		// 点数は10点程度を得る。
		return traceConture(coord, 1, s, o_coord);
	}
	//ベクトルの類似度判定式
	private final static boolean checkVecCos(VecLinearCoordinates.VecLinearCoordinatePoint i_current_vec,VecLinearCoordinates.VecLinearCoordinatePoint i_prev_vec,double i_ave_dx,double i_ave_dy)
	{
		double x1=i_current_vec.dx;
		double y1=i_current_vec.dy;
		double n=(x1*x1+y1*y1);
		//平均ベクトルとこのベクトルがCOS_DEG_20未満であることを確認(pos_ptr.getAbsVecCos(i_ave_dx,i_ave_dy)<NyARMath.COS_DEG_20 と同じ)
		double d;
		d=(x1*i_ave_dx+y1*i_ave_dy)/NyARMath.COS_DEG_20;
		if(d*d<(n*(i_ave_dx*i_ave_dx+i_ave_dy*i_ave_dy))){
			//隣接ベクトルとこのベクトルが5度未満であることを確認(pos_ptr.getAbsVecCos(i_prev_vec)<NyARMath.COS_DEG_5と同じ)
			d=(x1*i_prev_vec.dx+y1*i_prev_vec.dy)/NyARMath.COS_DEG_5;
			if(d*d<n*(i_prev_vec.dx*i_prev_vec.dx+i_prev_vec.dy*i_prev_vec.dy)){
				return true;
			}
		}
		return false;
	}
	/**
	 * 輪郭線を取得します。
	 * 取得アルゴリズムは、以下の通りです。
	 * 1.輪郭座標(n)の画素周辺の画素ベクトルを取得。
	 * 2.輪郭座標(n+1)周辺の画素ベクトルと比較。
	 * 3.差分が一定以下なら、座標と強度を保存
	 * 4.3点以上の集合になったら、最小二乗法で直線を計算。
	 * 5.直線の加重値を個々の画素ベクトルの和として返却。
	 */
	public boolean traceConture(NyARIntCoordinates i_coord, int i_pos_mag,int i_cell_size, VecLinearCoordinates o_coord)
	{
		assert(i_cell_size<=45);//intで二乗差分値を計算するときの限界
		VecLinearCoordinates.VecLinearCoordinatePoint[] pos=this._tmp_coord_pos;
		// ベクトル化
		int MAX_COORD = o_coord.items.length;
		int i_coordlen = i_coord.length;
		NyARIntPoint2d[] coord = i_coord.items;
		VecLinearCoordinates.VecLinearCoordinatePoint pos_ptr;

		//0個目のライン探索
		int number_of_data = 0;
		double sq;
		int sq_sum=0;//3x3の場合、1ピクセルあたりΣ8*(255*255)が最大値なので、45x45程度のセルが最大。
		//0番目のピクセル
		pos[0].scalar=sq=this.getAreaVector33(coord[0].x * i_pos_mag, coord[0].y * i_pos_mag,i_cell_size, i_cell_size,pos[0]);
		sq_sum+=(int)sq;
		//[2]に0を保管

		//1点目だけは前方と後方、両方に探索をかける。
		//前方探索の終点
		int coord_last_edge=i_coordlen;
		//後方探索
		int sum=1;
		double ave_dx=pos[0].dx;
		double ave_dy=pos[0].dy;
		for (int i = i_coordlen-1; i >0; i--)
		{
			// ベクトル取得
			pos_ptr=pos[sum];
			pos_ptr.scalar=sq=this.getAreaVector33(coord[i].x * i_pos_mag,coord[i].y * i_pos_mag, i_cell_size, i_cell_size,pos_ptr);
			sq_sum+=(int)sq;
			// 類似度判定
			if(checkVecCos(pos[sum],pos[sum-1],ave_dx,ave_dy))
			{
				//相関なし->前方探索へ。
				ave_dx=pos_ptr.dx;
				ave_dy=pos_ptr.dy;
				coord_last_edge=i;
				break;
			} else {
				//相関あり- 点の蓄積
				ave_dx+=pos_ptr.dx;
				ave_dy+=pos_ptr.dy;
				sum++;
			}
		}
		//前方探索
		for (int i = 1; i<coord_last_edge; i++)
		{
			// ベクトル取得
			pos_ptr=pos[sum];
			pos_ptr.scalar=sq=this.getAreaVector33(coord[i].x * i_pos_mag,coord[i].y * i_pos_mag, i_cell_size, i_cell_size,pos_ptr);
			sq_sum+=(int)sq;			
			if(sq==0){
				continue;
			}
			//if (pos_ptr.getAbsVecCos(pos[sum-1]) < NyARMath.COS_DEG_5 && pos_ptr.getAbsVecCos(ave_dx,ave_dy)<NyARMath.COS_DEG_20) {
			if (checkVecCos(pos[sum],pos[sum-1],ave_dx,ave_dy)) {
				//相関なし->新しい要素を作る。
				if(this.leastSquaresWithNormalize(pos,sum,o_coord.items[number_of_data],sq_sum/(sum*5))){
					number_of_data++;
				}
				ave_dx=pos_ptr.dx;
				ave_dy=pos_ptr.dy;
				//獲得した値を0へ移動
				pos[0].setValue(pos[sum]);
				sq_sum=0;
				sum=1;
			} else {
				//相関あり- 点の蓄積
				ave_dx+=pos_ptr.dx;
				ave_dy+=pos_ptr.dy;				
				sum++;
			}
			// 輪郭中心を出すための計算
			if (number_of_data == MAX_COORD) {
				// 輪郭ベクトルバッファの最大を超えたら失敗
				return false;
			}
		}
		if(this.leastSquaresWithNormalize(pos,sum,o_coord.items[number_of_data],sq_sum/(sum*5))){
			number_of_data++;
		}
		// ベクトル化2:最後尾と先頭の要素が似ていれば連結する。
		// sq_distの合計を計算
		o_coord.length = number_of_data;

		return true;
	}
	/**
	 * ノイズらしいベクトルを無視しながら最小二乗法でベクトルを統合する関数
	 * @param i_points
	 * @param i_number_of_data
	 * @param o_dest
	 * @param i_scale_th
	 * @return
	 */
	private final boolean leastSquaresWithNormalize(VecLinearCoordinates.VecLinearCoordinatePoint[] i_points,int i_number_of_data,VecLinearCoordinates.VecLinearCoordinatePoint o_dest,double i_scale_th)
	{
		int i;
		int num=0;
		double sum_xy = 0, sum_x = 0, sum_y = 0, sum_x2 = 0;
		for (i=i_number_of_data-1; i>=0; i--){
			VecLinearCoordinates.VecLinearCoordinatePoint ptr=i_points[i];
			//規定より小さいスケールは除外なう
			if(ptr.scalar<i_scale_th)
			{
				continue;
			}
			double xw=ptr.x;
			sum_xy += xw * ptr.y;
			sum_x += xw;
			sum_y += ptr.y;
			sum_x2 += xw*xw;
			num++;
		}
		if(num<3){
			return false;
		}
		double la=-(num * sum_x2 - sum_x*sum_x);
		double lb=-(num * sum_xy - sum_x * sum_y);
		double cc=(sum_x2 * sum_y - sum_xy * sum_x);
		double lc=-(la*sum_x+lb*sum_y)/num;
		//交点を計算
		final double w1 = -lb * lb - la * la;
		if (w1 == 0.0) {
			return false;
		}		
		o_dest.x=((la * lc - lb * cc) / w1);
		o_dest.y= ((la * cc +lb * lc) / w1);
		o_dest.dy=-lb;
		o_dest.dx=-la;
		o_dest.scalar=num;
		return true;
	}	

	private NyARIntPoint2d[] __pt = NyARIntPoint2d.createArray(2);
	private NyARLinear __temp_l = new NyARLinear();

	/**
	 * クリッピング付きのライントレーサです。
	 * 
	 * @param i_pos1
	 * @param i_pos2
	 * @param i_edge
	 * @param o_coord
	 * @return
	 * @throws NyARException
	 */
	public boolean traceLineWithClip(NyARDoublePoint2d i_pos1,
		NyARDoublePoint2d i_pos2, int i_edge, VecLinearCoordinates o_coord)
		throws NyARException
	{
		NyARIntSize s=this._ref_base_raster.getSize();
		boolean is_p1_inside_area, is_p2_inside_area;

		NyARIntPoint2d[] pt = this.__pt;
		// 線分が範囲内にあるかを確認
		is_p1_inside_area = s.isInnerPoint(i_pos1);
		is_p2_inside_area = s.isInnerPoint(i_pos2);
		// 個数で分岐
		if (is_p1_inside_area && is_p2_inside_area) {
			// 2ならクリッピング必要なし。
			if (!this.traceLine(i_pos1, i_pos2, i_edge, o_coord)) {
				return false;
			}
			return true;

		}
		// 1,0個の場合は、線分を再定義
		if (!this.__temp_l.makeLinearWithNormalize(i_pos1, i_pos2)) {
			return false;
		}
		if (!this.__temp_l.makeSegmentLine(s.w,s.h,pt)) {
			return false;
		}
		if (is_p1_inside_area != is_p2_inside_area) {
			// 1ならクリッピング後に、外に出ていた点に近い輪郭交点を得る。

			if (is_p1_inside_area) {
				// p2が範囲外
				pt[(i_pos2.sqDist(pt[0]) < i_pos2.sqDist(pt[1])) ? 1 : 0].setValue(i_pos1);
			} else {
				// p1が範囲外
				pt[(i_pos1.sqDist(pt[0]) < i_pos2.sqDist(pt[1])) ? 1 : 0].setValue(i_pos2);
			}
		} else {
			// 0ならクリッピングして得られた２点を使う。
			if (!this.__temp_l.makeLinearWithNormalize(i_pos1, i_pos2)) {
				return false;
			}
			if (!this.__temp_l.makeSegmentLine(s.w,s.h, pt)) {
				return false;
			}
		}
		if (!this.traceLine(pt[0], pt[1], i_edge, o_coord)) {
			return false;
		}

		return true;
	}	
}
