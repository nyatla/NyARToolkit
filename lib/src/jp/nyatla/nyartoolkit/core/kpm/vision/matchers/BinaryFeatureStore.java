package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;






/**
 * Represents a container for features and point information.
 */
public class BinaryFeatureStore extends FeaturePointStack
{
	public BinaryFeatureStore(int i_size){
		super(i_size);
	}
	public BinaryFeatureStore()
	{
		super(9999);
		System.out.println("force set BinaryFeatureStore size to 9999");
	}
};
