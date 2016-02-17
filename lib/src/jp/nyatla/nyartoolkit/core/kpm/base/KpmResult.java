package jp.nyatla.nyartoolkit.core.kpm.base;

import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

public class KpmResult {
	final public NyARDoubleMatrix44                 camPose=new NyARDoubleMatrix44();
	final public NyARTransMatResultParam			resultparams=new NyARTransMatResultParam();
    public int                       pageNo;
    public double                     error;
    public int                       inlierNum;
    public int                       camPoseF;
    public boolean                   skipF;
}
