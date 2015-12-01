package jp.nyatla.nyartoolkit.core.kpm;

import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

public class KpmResult {
	final public NyARDoubleMatrix44                 camPose=new NyARDoubleMatrix44();
	final public NyARTransMatResultParam			resultparams=new NyARTransMatResultParam();
    public int                       pageNo;
    public float                     error;
    public int                       inlierNum;
    public int                       camPoseF;
    public boolean                   skipF;
}
