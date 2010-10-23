package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.NyARManagedObjectPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.NyARManagedObject.INyARManagedObjectPoolOperater;

public class NyARRectTargetStatusPool extends NyARManagedObjectPool<NyARRectTargetStatus>
{
	/**
	 * 要素間で共有するオブジェクト。この変数は、NyARRectTargetStatus以外から使わないでください。
	 */
	public VecLinearCoordinates _vecpos=new VecLinearCoordinates(100);
	public LineBaseVertexDetector _line_detect=new LineBaseVertexDetector();
	public VecLinearCoordinatesOperator _vecpos_op=new VecLinearCoordinatesOperator(); 
	public VecLinearCoordinates.CoordData[] _indexbuf=new VecLinearCoordinates.CoordData[4];
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

}