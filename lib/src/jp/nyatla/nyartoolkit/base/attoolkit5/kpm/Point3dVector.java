package jp.nyatla.nyartoolkit.base.attoolkit5.kpm;

import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class Point3dVector extends NyARObjectStack<Point3d>{

	public Point3dVector(int i_length) {
		super(i_length, Point3d.class);
	}
	protected Point3d createElement()
	{
		return new Point3d();
	}
}
