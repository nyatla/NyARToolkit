package jp.nyatla.nyartoolkit.core.kpm.sandbox.matrixlab3;



public class MatTable
{
	//row,col
	final protected int _size;
	final protected MatItem[][] _table;
	final public String _fmt;
	
	public MatTable(int i_size)
	{
		this._fmt=i_size>=10?"%s%02d%02d":"%s%d%d";
		this._size=i_size;
		this._table=new MatItem[i_size][i_size];
		for(int r=0;r<i_size;r++){
			for(int c=0;c<i_size;c++){
				this._table[r][c]=new MatItem(this._fmt,"a",r,c,false);
			}
		}
	}
	/**
	 * matの余因子成分を生成する。
	 * @param i_mat
	 * @param i_r
	 * @param i_c
	 */
	protected MatTable(MatTable i_mat,int i_r,int i_c)
	{
		this._size=i_mat._size-1;
		this._table=i_mat.getCofactor(i_r,i_c);
		this._fmt=i_mat._fmt;
	}
	/**
	 * 余因子行列を得る。
	 * @param r
	 * @param c
	 * @return
	 */
	public MatItem[][] getCofactor(int r,int c)
	{
		MatItem[][] mat=new MatItem[this._size-1][this._size-1];
		int rp=0;
		for(int i=0;i<this._size;i++){
			if(i==r){
				continue;
			}
			int cp=0;
			for(int j=0;j<this._size;j++){
				if(j==c){
					continue;
				}
				mat[rp][cp]=this._table[i][j];
				cp++;
			}
			rp++;
		}
		return mat;
	}
	/**
	 * 余因子のフラグを得る。
	 * @param r
	 * @param c
	 * @return
	 */
	public int getCofactorFlag(int r,int c){
		return (r+c)%2==0?1:-1;
	}

	public void setZero(int r, int c) {
		this._table[r][c].is_zero=true;// TODO Auto-generated method stub
		this.setName(r, c,"0");
	}	
	public void setName(int r, int c,String i_name)
	{
		this._table[r][c].setName(i_name);
	}
	public String getIndexHash()
	{
		String rs="";
		String cs="";
		for(int s=0;s<this._size;s++){
			rs+=this._table[s][0].r;
			cs+=this._table[0][s].c;
		}
		return rs+"-"+cs;
	}
}