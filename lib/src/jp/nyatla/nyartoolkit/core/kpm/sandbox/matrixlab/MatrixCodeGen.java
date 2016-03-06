package jp.nyatla.nyartoolkit.core.kpm.sandbox.matrixlab;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MatrixCodeGen extends MatTable{
	public MatrixCodeGen(int i_size) {
		super(i_size);
	}
	protected MatrixCodeGen(int i_size,MatItem[][] t) {
		super(i_size,t);
	}
	/**
	 * 絶対値の文字列を返す。
	 * @return
	 */
	public String absStr(int flag){
		if(this._size==2){
			String s11=this._table[0][0].getStr();
			String s12=this._table[0][1].getStr();
			String s21=this._table[1][0].getStr();
			String s22=this._table[1][1].getStr();
			if(flag>0){
				return String.format("%s*%s-%s*%s",s11,s22,s12,s21);
			}else{
				return String.format("%s*%s-%s*%s",s12,s21,s11,s22);
			}
		}else{
			String s="";
			for(int i=0;i<this._size;i++){
				s+=String.format("%s*(%s)",
					this._table[0][i].getStr(),
					new MatrixCodeGen(this._size-1,this.getCofactor(0,i)).absStr(flag)
				);
				if(i!=this._size-1){
					s+=this.getCofactorFlag(0,i+1)>0?"+":"-";
				}
			}
			return s;
		}
	}
	public String inversMatrix()
	{
		String s="det="+this.absStr(1)+"\n";
		for(int r=0;r<this._size;r++){
			for(int c=0;c<this._size;c++){
				String val=new MatrixCodeGen(this._size-1,this.getCofactor(r, c)).absStr(this.getCofactorFlag(r, c));
				//名前は転値
				s+=String.format("M%d%d=(%s)/det;\n",c,r,val);
			}				
		}
		return s;
	}
	public String valueDef()
	{
		String s="";
		for(int r=0;r<this._size;r++){
			s+="double ";
			for(int c=0;c<this._size;c++){
				s+=String.format("m%d%d=0,\t",r,c);
			}
			s+="\n";
		}
		return s;
	}
	public static void main(String[] args){
		MatrixCodeGen m33=new MatrixCodeGen(8);
		String s=m33.inversMatrix();
//		System.out.println(m33.inversMatrix());
		System.out.println(m33.valueDef());
		try {
			File file = new File("d:\\mat.txt");
			FileWriter filewriter = new FileWriter(file);
			filewriter.write(s);
			filewriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
