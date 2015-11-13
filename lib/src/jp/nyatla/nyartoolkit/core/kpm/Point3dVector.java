package jp.nyatla.nyartoolkit.core.kpm;

import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class Point3dVector extends NyARObjectStack<Point3d>{

	protected Point3dVector(int i_length) {
		super(i_length, Point3d.class);
	}
	protected Point3d createElement()
	{
		return new Point3d();
	}
}
