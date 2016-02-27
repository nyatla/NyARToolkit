package jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid;

import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class DogFeaturePointStack extends NyARObjectStack<DogFeaturePoint> implements Cloneable {
	public DogFeaturePointStack(int i_length) {
		super(i_length, DogFeaturePoint.class);
	}

	@Override
	final protected DogFeaturePoint createElement() {
		return new DogFeaturePoint();
	}
}