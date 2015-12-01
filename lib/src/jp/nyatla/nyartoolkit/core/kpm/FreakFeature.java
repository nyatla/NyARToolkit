package jp.nyatla.nyartoolkit.core.kpm;

public class FreakFeature {
	final public static int FREAK_SUB_DIMENSION=96;
    final public int[]    v=new int[FREAK_SUB_DIMENSION];
    public float              angle;
    public float              scale;
    public int               maxima;
    public static FreakFeature[] createArray(int i_len){
    	FreakFeature[] a= new FreakFeature[i_len];
    	for(int i=0;i<a.length;i++){
    		a[i]=new FreakFeature();
    	}
    	return a;
    }
}
