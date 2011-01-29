package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObjectPool;
import jp.nyatla.nyartoolkit.rpf.utils.LineBaseVertexDetector;
import jp.nyatla.nyartoolkit.rpf.utils.VecLinearCoordinates;
import jp.nyatla.nyartoolkit.rpf.utils.VecLinearCoordinatesOperator;

/**
 * このクラスは、{@link NyARRectTargetStatus}型のプールクラスです。
 * 通常、ユーザが使うことはありません。
 */
public class NyARRectTargetStatusPool extends NyARManagedObjectPool<NyARRectTargetStatus>
{
	/** 要素間で共有するオブジェクト。この変数は、NyARRectTargetStatus以外から使わないでください。*/
	public VecLinearCoordinates _vecpos=new VecLinearCoordinates(100);
	/** 要素間で共有するオブジェクト。この変数は、NyARRectTargetStatus以外から使わないでください。*/
	public LineBaseVertexDetector _line_detect=new LineBaseVertexDetector();
	/** 要素間で共有するオブジェクト。この変数は、NyARRectTargetStatus以外から使わないでください。*/
	public VecLinearCoordinatesOperator _vecpos_op=new VecLinearCoordinatesOperator(); 
	/** 要素間で共有するオブジェクト。この変数は、NyARRectTargetStatus以外から使わないでください。*/
	public VecLinearCoordinates.VecLinearCoordinatePoint[] _indexbuf=new VecLinearCoordinates.VecLinearCoordinatePoint[4];
	/** 要素間で共有するオブジェクト。この変数は、NyARRectTargetStatus以外から使わないでください。*/
	public NyARLinear[] _line=NyARLinear.createArray(4);
	/**
	 * コンストラクタです。
	 * プールの最大サイズを利用して、インスタンスを生成します。
	 * @param i_size
	 * プールの最大サイズです。
	 * @throws NyARException
	 */
	public NyARRectTargetStatusPool(int i_size) throws NyARException
	{
		super.initInstance(i_size,NyARRectTargetStatus.class);
	}
	/**
	 * この関数は、リスト要素を生成して返します。
	 */	
	protected NyARRectTargetStatus createElement()
	{
		return new NyARRectTargetStatus(this);
	}

	private final int[] __sq_table=new int[4];
	/**
	 * この関数は、頂点同士の移動距離を測定して、極端に大きな差を含む点が無いかをチェックします。
	 * 点セット同士の順番は一致している必要があります。
	 * チェックルールは、頂点セット同士の差のうち一つが、全体の一定割合以上の誤差を持つかです。
	 * この関数は矩形の辺推定の誤認識対策用のコードですが、より良い方法が見つかったら消してもかまいません。
	 * @param i_point1
	 * 点セット１(4要素)
	 * @param i_point2
	 * 点セット２(4要素)
	 * @return
	 * 極端な点が無ければtrue
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