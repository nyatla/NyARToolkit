package jp.nyatla.nyartoolkit.core.kpm.sandbox.matrixlab2;

public class MatItem{

	public boolean is_zero;
	public String name;
	final public int r;
	final public int c;
	public MatItem(String i_format,String i_prefix,int ir,int ic,boolean i_is_zero){
		this.c=ic;
		this.r=ir;//i_format="%s%02d%02d"
		this.name=String.format(i_format,i_prefix,ir,ic);
	}
	public void setName(String i_name) {
		this.name=i_name;
	}
}