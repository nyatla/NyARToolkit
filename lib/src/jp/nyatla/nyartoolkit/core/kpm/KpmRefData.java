package jp.nyatla.nyartoolkit.core.kpm;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;

public class KpmRefData {
    final public NyARDoublePoint2d        coord2D=new NyARDoublePoint2d();
    final public NyARDoublePoint2d        coord3D=new NyARDoublePoint2d();      // millimetres.
    final public FreakFeature      featureVec=new FreakFeature();
    public int               pageNo;
    public int               refImageNo;
}
