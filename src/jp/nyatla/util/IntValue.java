package jp.nyatla.util;

public class IntValue {
	private int v;
	public void set(int i_v){
		v=i_v;
	}
	public int get(){
		return v;
	}
	public void inc(){
		v++;
	}
	public void add(int i_v){
		v+=i_v;
	}
}
