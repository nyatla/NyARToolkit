package jp.nyatla.util;

public class IntPointer {
	private int[] array_ref;		//配列
	private int array_offset;		//配列に対する基準値
	private int position;			//array_offsetに対する現在位置
	public static IntPointer wrap(int[] i_array_ref,int i_offset)
	{
		return new IntPointer(i_array_ref,i_offset);
	}
	public void set(int i_value)
	{
		array_ref[array_offset+position]=i_value;		
	}
	public void set(int i_rel_positon,int i_value)
	{
		array_ref[array_offset+position+i_rel_positon]=i_value;
	}
	/**
	 * カレント位置の値を取得する
	 * @return
	 */
	public int get()
	{
		return array_ref[array_offset+position];
	}
	/**
	 * カレント位置から+i_slideの位置にある値を取得する。
	 * @param i_step
	 * @return
	 */
	public int get(int i_slide)
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
	public void addValue(int i_val)
	{
		array_ref[array_offset+position]+=i_val;
	}
	public void addValue(int i_step,int i_val)
	{
		array_ref[array_offset+position+i_step]+=i_val;
	}
	private IntPointer(int[] i_array_ref,int i_base_point)
	{
		array_offset	=i_base_point;
		array_ref		=i_array_ref;
		position		=0;
	}
	public IntPointer()
	{
		array_offset	=0;
		array_ref		=new int[1];
		position		=0;
	}
}
