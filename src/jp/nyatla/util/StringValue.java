package jp.nyatla.util;

public class StringValue {
	private String v;
	public StringValue()
	{
		v="";
	}
	public StringValue(String i_v)
	{
		v=i_v;
	}
	public void set(String i_v)
	{
		v=i_v;
	}
	public String get()
	{
		return v;
	}
	public String toString()
	{
		return v;
	}
}
