package jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.utils;

/**
 * 特徴点を範囲分類するためのクラス。
 * {@link DoGScaleInvariantDetector}から使うクラス。
 *
 */
public class AreaBuckit
{
	public class BucketPair {
		public double first;
		public int second;
	}		
	/**
	 * [Y][X]の順で[N]個のbucketを連続配置したもの。
	 */
	final public BucketPair[] _buckit;
	final private int dx;
	final private int dy;
	final private int _y_dim;
	final private int _block_size;
	public AreaBuckit(int i_w,int i_h,int i_y_dim,int i_x_dim,int i_max_f_num)
	{
		int n=i_max_f_num/(i_x_dim*i_y_dim);
		assert(n>=1);
		this._buckit=new BucketPair[i_y_dim*i_x_dim*n];
		for(int i=0;i<this._buckit.length;i++){
			this._buckit[i]=new BucketPair();
		}
		this._y_dim=i_y_dim;
		this.dx = (int) Math.ceil(i_w / i_x_dim);
		this.dy = (int) Math.ceil(i_h / i_y_dim);
		this._block_size=n;
		return;			
	}
	public void clear()
	{
		for(int i=0;i<this._buckit.length;i++){
			this._buckit[i].first=0;
		}
		return;			
		
	}
	/**
	 * bucketにアイテム配置を試行する。
	 * @param i_x
	 * @param i_y
	 * @param idx
	 * @param score
	 * @return
	 * 配置に失敗した場合=false
	 * 成功した場合=true
	 */
	public boolean put(double i_x,double i_y,int idx,double score)
	{
		//ブロックを選択
//		int s=((this._x_dim*((int)(i_y / dy)))+((int)(i_x / dx)))*this._block_size;
		int s=((this._y_dim*((int)(i_x / dx)))+((int)(i_y / dy)))*this._block_size;
		int e=this._block_size+s;
		BucketPair[] p=this._buckit;

		for(int i=s;i<e;i++){
			if(p[i].first<score){
				//最後の項目を退避
				BucketPair l=p[e-1];
				l.first=score;
				l.second=idx;
				//入れ替え
				for(int j=e-1;j>=i+1;j--){
					p[j]=p[j-1];
				}
				p[i]=l;
				return true;
			}
		}
		return false;
	}
}