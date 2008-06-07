package jp.nyatla.util;

public class StringPointer {
	private String[] array_ref;//配列
	private int array_offset;           //配列に対する基準値
	private int position;          //array_offsetに対する現在位置
	public void set(String i_value)
	{
		array_ref[array_offset+position]=i_value;
	}
	public String get()
	{
		return array_ref[array_offset+position];
	}
	public String toString()
	{
		return get();
	}
	public StringPointer()
	{
		array_ref=new String[1];
		array_offset=0;
		position=0;
	}
}
