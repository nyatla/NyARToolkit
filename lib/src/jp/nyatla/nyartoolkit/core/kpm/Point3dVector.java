package jp.nyatla.nyartoolkit.core.kpm;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class Point3dVector extends NyARObjectStack<NyARDoublePoint3d> {

	public Point3dVector(int i_length) {
		super(i_length, NyARDoublePoint3d.class);
	}

	protected NyARDoublePoint3d createElement() {
		return new NyARDoublePoint3d();
	}
}
