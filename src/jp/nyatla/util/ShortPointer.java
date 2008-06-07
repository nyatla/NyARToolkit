package jp.nyatla.util;

public class ShortPointer {
	private short[] array_ref;		//配列
	private int array_offset;		//配列に対する基準値
	private int position;			//array_offsetに対する現在位置
	public static ShortPointer wrap(short[] i_array_ref,int i_offset)
	{
		return new ShortPointer(i_array_ref,i_offset);
	}
	public static ShortPointer wrap(ShortPointer i_inst,int i_offset)
	{
		return new ShortPointer(i_inst.array_ref,i_inst.array_offset+i_inst.position+i_offset);
	}
	public void set(short i_value)
	{
		array_ref[array_offset+position]=i_value;		
	}
	public void set(int i_rel_positon,short i_value)
	{
		array_ref[array_offset+position+i_rel_positon]=i_value;
	}
	/**
	 * カレント位置の値を取得する
	 * @return
	 */
	public short get()
	{
		return array_ref[array_offset+position];
	}
	/**
	 * カレント位置から+i_slideの位置にある値を取得する。
	 * @param i_step
	 * @return
	 */
	public short get(int i_slide)
	{
		return array_ref[array_offset+position+i_slide];
	}
	public void incPtr()
	{
		position++;
	}
	public void addPtr(int v)
	{
		position+=v;
	}
	private ShortPointer(short[] i_array_ref,int i_base_point)
	{
		array_offset	=i_base_point;
		array_ref		=i_array_ref;
		position		=0;
	}
//	public BytePointer()
//	{
//		array_offset	=0;
//		array_ref		=new int[1];
//		position		=0;
//	}
}
