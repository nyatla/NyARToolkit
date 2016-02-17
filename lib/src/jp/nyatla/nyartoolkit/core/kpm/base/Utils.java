package jp.nyatla.nyartoolkit.core.kpm.base;

public class Utils {
	public static double[] arraysubset(double[] a,int s,int l){
		double[] r=new double[l];
		System.arraycopy(a,s,r,0,l);
		return r;
	}
}
