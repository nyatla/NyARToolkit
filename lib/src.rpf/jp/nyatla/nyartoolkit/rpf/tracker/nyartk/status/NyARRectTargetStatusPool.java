package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObjectPool;
import jp.nyatla.nyartoolkit.rpf.utils.LineBaseVertexDetector;
import jp.nyatla.nyartoolkit.rpf.utils.VecLinearCoordinates;
import jp.nyatla.nyartoolkit.rpf.utils.VecLinearCoordinatesOperator;


public class NyARRectTargetStatusPool extends NyARManagedObjectPool<NyARRectTargetStatus>
{
	/**
	 * 要素間で共有するオブジェクト。この変数は、NyARRectTargetStatus以外から使わないでください。
	 */
	public VecLinearCoordinates _vecpos=new VecLinearCoordinates(100);
	public LineBaseVertexDetector _line_detect=new LineBaseVertexDetector();
	public VecLinearCoordinatesOperator _vecpos_op=new VecLinearCoordinatesOperator(); 
	public VecLinearCoordinates.VecLinearCoordinatePoint[] _indexbuf=new VecLinearCoordinates.VecLinearCoordinatePoint[4];
	public NyARLinear[] _line=NyARLinear.createArray(4);
	/**
	 * @param i_size
	 * スタックの最大サイズ
	 * @param i_cood_max
	 * 輪郭ベクトルの最大数
	 * @throws NyARException
	 */
	public NyARRectTargetStatusPool(int i_size) throws NyARException
	{
		super.initInstance(i_size,NyARRectTargetStatus.class);
	}
	protected NyARRectTargetStatus createElement()
	{
		return new NyARRectTargetStatus(this);
	}

	private final int[] __sq_table=new int[4];
	/**
	 * 頂点セット同士の差分を計算して、極端に大きな誤差を持つ点が無いかを返します。
	 * チェックルールは、頂点セット同士の差のうち一つが、全体の一定割合以上の誤差を持つかです。
	 * @param i_point1
	 * @param i_point2
	 * @return
	 * @todo 展開して最適化
	 */
	public final boolean checkLargeDiff(NyARDoublePoint2d[] i_point1,NyARDoublePoint2d[] i_point2)
	{
		assert(i_point1.length==i_point2.length);
		int[] sq_tbl=this.__sq_table;
		int all=0;
		for(int i=3;i>=0;i--){
			sq_tbl[i]=(int)i_point1[i].sqDist(i_point2[i]);
			all+=sq_tbl[i];
		}
		//移動距離の2乗の平均値
		if(all<4){
			return true;
		}
		for(int i=3;i>=0;i--){
			//1個が全体の75%以上を持っていくのはおかしい。
			if(sq_tbl[i]*100/all>70){
				return false;
			}
		}
		return true;
	}	


}