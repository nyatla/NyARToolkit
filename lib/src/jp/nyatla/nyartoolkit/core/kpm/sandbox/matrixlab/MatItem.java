package jp.nyatla.nyartoolkit.core.kpm.sandbox.matrixlab;

public class MatItem{

	public boolean is_zero;
	public String name;
	public MatItem(int ix,int iy,boolean i_is_zero){
		this.name="a"+ix+"_"+iy;
	}
	public String getStr(){
		return this.name;
	}
	public void setName(String i_name) {
		this.name=i_name;
	}
}