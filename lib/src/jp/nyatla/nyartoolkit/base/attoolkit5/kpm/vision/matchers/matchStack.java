package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers;

import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class matchStack extends NyARObjectStack<match_t>{

	public matchStack(int i_length) {
		super(i_length, match_t.class);
	}
	@Override
	public match_t createElement(){
		return new match_t();
	}
	
}
