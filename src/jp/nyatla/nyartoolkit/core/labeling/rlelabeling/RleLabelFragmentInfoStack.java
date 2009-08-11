package jp.nyatla.nyartoolkit.core.labeling.rlelabeling;


import jp.nyatla.nyartoolkit.core.labeling.NyARLabelInfo;
import jp.nyatla.nyartoolkit.core.labeling.NyARLabelInfoStack;


public class RleLabelFragmentInfoStack  extends NyARLabelInfoStack<RleLabelFragmentInfoStack.RleLabelFragmentInfo>
{
	public class RleLabelFragmentInfo extends NyARLabelInfo
	{
		//継承メンバ
		//int area; // フラグメントラベルの領域数
		public short id; // フラグメントラベルのインデクス
		public int entry_x; // フラグメントラベルの位置
		public int clip_t;
		public int clip_l;
		public int clip_r;
		public int clip_b;
		public double pos_x;
		public double pos_y;
	}	
	public RleLabelFragmentInfoStack(int i_length)
	{
		super(i_length, RleLabelFragmentInfoStack.RleLabelFragmentInfo.class);
		return;
	}

	protected RleLabelFragmentInfoStack.RleLabelFragmentInfo createElement()
	{
		return new RleLabelFragmentInfoStack.RleLabelFragmentInfo();
	}
}