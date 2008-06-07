package jp.nyatla.util;
/**
 * double型ポインタのエミュレートクラス
 * 対象のdouble配列を基点を基準に参照する。
 * @author atla
 *
 */
public class DoublePointer
{
	private double[] array_ref;	//配列
	private int array_offset;		//配列に対する基準値
	private int position;			//array_offsetに対する現在位置
	public static DoublePointer wrap(double[] i_array_ref,int i_offset)
	{
		return new DoublePointer(i_array_ref,i_offset);
	}
	public static DoublePointer wrap(DoublePointer i_inst)
	{
		return new DoublePointer(i_inst.array_ref,i_inst.getPtrArrayOffset());
	}
	//現在位置からのサブシーケンスを返す。
	public DoublePointer slice(int i_offset)
	{
		return DoublePointer.wrap(array_ref,array_offset+position+i_offset);
	}
	public void set(double i_value)
	{
		array_ref[array_offset+position]=i_value;		
	}
	public void set(int i_rel_positon,double i_value)
	{
		array_ref[array_offset+position+i_rel_positon]=i_value;
	}
	/**
	 * カレント位置の値を取得する
	 * @return
	 */
	public double get()
	{
		return array_ref[array_offset+position];
	}
	/**
	 * カレント位置から+i_slideの位置にある値を取得する。
	 * @param i_step
	 * @return
	 */
	public double get(int i_slide)
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
	public double[] array()
	{
		return array_ref;
	}
	public void subValue(double i_val)
	{
		array_ref[array_offset+position]-=i_val;
	}
	public void subValue(int i_step,double i_val)
	{
		array_ref[array_offset+position+i_step]-=i_val;
	}
	public void addValue(double i_val)
	{
		array_ref[array_offset+position]+=i_val;
	}
	public void addValue(int i_step,double i_val)
	{
		array_ref[array_offset+position+i_step]+=i_val;
	}

	/**
	 * 現在位置のオフセット位置を返す。
	 * @return
	 */
	public int getPtrArrayOffset()
	{
		return array_offset+position;
	}
	private DoublePointer(double[] i_array_ref,int i_base_point)
	{
		array_offset	=i_base_point;
		array_ref		=i_array_ref;
		position		=0;
	}
	public DoublePointer(int i_length)
	{
		array_offset	=0;
		array_ref		=new double[i_length];
		position		=0;		
	}
	public DoublePointer()
	{
		array_offset	=0;
		array_ref		=new double[1];
		position		=0;		
	}
}