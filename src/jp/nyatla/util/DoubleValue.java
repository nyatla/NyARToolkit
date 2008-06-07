package jp.nyatla.util;

public class DoubleValue {
	private double v;
	public void set(double i_v){
		v=i_v;
	}
	public double get(){
		return v;
	}
	public void add(double i_v){
		v+=i_v;
	}
}