package jp.nyatla.nyartoolkit.core.kpm;

public class Utils {
	public static float[] arraysubset(float[] a,int s,int l){
		float[] r=new float[l];
		System.arraycopy(a,s,r,0,l);
		return r;
	}
}
