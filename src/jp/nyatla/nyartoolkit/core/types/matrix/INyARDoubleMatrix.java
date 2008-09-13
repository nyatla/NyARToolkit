package jp.nyatla.nyartoolkit.core.types.matrix;

public interface INyARDoubleMatrix
{
	/**
	 * 配列の内容を行列に設定する。
	 * 遅いので余り使わないでね。
	 * @param o_value
	 */
	public void setValue(double[] i_value);
	/**
	 * 行列の内容を配列に返す。
	 * 遅いので余り使わないでね。
	 * @param o_value
	 */
	public void getValue(double[] o_value);

}
