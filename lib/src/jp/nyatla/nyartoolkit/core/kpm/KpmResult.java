package jp.nyatla.nyartoolkit.core.kpm;

public class KpmResult {
    public float[][]                 camPose=new float[3][4];
    public int                       pageNo;
    public float                     error;
    public int                       inlierNum;
    public int                       camPoseF;
    public int                       skipF;
}
