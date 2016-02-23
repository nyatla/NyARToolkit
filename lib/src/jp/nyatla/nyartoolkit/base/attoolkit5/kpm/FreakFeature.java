package jp.nyatla.nyartoolkit.base.attoolkit5.kpm;

public class FreakFeature {
	final public static int FREAK_SUB_DIMENSION = 96;
	final public byte[] v = new byte[FREAK_SUB_DIMENSION];
	public double angle;
	public double scale;
	public int maxima;

	protected FreakFeature() {
	}

	public FreakFeature(byte[] i_bytes) {
		System.arraycopy(i_bytes, 0, this.v, 0, i_bytes.length);
	}

	public static FreakFeature[] createArray(int i_len) {
		FreakFeature[] a = new FreakFeature[i_len];
		for (int i = 0; i < a.length; i++) {
			a[i] = new FreakFeature();
		}
		return a;
	}

	public void setFeaturevec(byte[] bytes) {
		System.arraycopy(bytes, 0, this.v, 0, bytes.length);
	}
}
