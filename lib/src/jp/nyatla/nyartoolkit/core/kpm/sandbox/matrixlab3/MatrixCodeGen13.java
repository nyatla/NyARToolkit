package jp.nyatla.nyartoolkit.core.kpm.sandbox.matrixlab3;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


public class MatrixCodeGen13{
	static public class Data{
		final String func;
		final String vname;
		final int number_of_mul;
		final int number_of_sum;
		public Data(String i_func,String i_vname,int i_number_of_mul,int i_number_of_sum){
			this.func=i_func;
			this.vname=i_vname;
			this.number_of_mul=i_number_of_mul;
			this.number_of_sum=i_number_of_sum;
		}
	}
	static public class MatrixTree{
		public class ElementMap extends LinkedHashMap<String,Data>{};
		/** 下位のdet*/
		final private MatrixTree.ElementMap _map=new MatrixTree.ElementMap();
		final private MatrixTree _child;
		final private int _depth;
		public MatrixTree(int i_depth)
		{
			if(i_depth>2){
				this._child=new MatrixTree(i_depth-1);
			}else{
				this._child=null;
			}
			this._depth=i_depth;
		}
		private static int vid=0;
		public static String variableId()
		{
			return "v"+(vid++);
		}
		private static String detMat(MatrixTree i_tree,MatTable i_mat)
		{
			if(i_tree==null){
				return null;
			}
			//detハッシュが登録済であれば何もしない。
			String hash=i_mat.getIndexHash();
			Data t=i_tree._map.get(hash);
			if(t!=null){
				return t.vname;
			}
			if(i_mat._size==2){
				String v3=String.format("(%s*%s-%s*%s)",i_mat._table[0][0].name,i_mat._table[1][1].name,i_mat._table[0][1].name,i_mat._table[1][0].name);
				String v=variableId();
				i_tree._map.put(hash,new Data(v3,v,2,1));
				return v;
			}else{
				String f="";
				int s=i_mat._size;
				for(int i=0;i<s;i++){
					MatItem a=i_mat._table[0][i];
					MatTable mt=new MatTable(i_mat,0,i);
					String d=detMat(i_tree._child,mt);
					f+=String.format("%s%s*(%s)",i%2==0?"+":"-",a.name,d);
				}
				String v=variableId();
				i_tree._map.put(hash,new Data(f.substring(1),v,s,s-1));

				return v;
			}
		}
		public void inversMat(MatTable i_mat,OutputStream i_stream) throws IOException
		{
			PrintWriter pr=new PrintWriter(i_stream);

			//行列ツリーの生成
			String[][] matname=new String[i_mat._size][i_mat._size];
			String det;
			vid=0;
			det=detMat(this,i_mat);
			for(int r=0;r<i_mat._size;r++){
				for(int c=0;c<i_mat._size;c++){
					matname[r][c]=detMat(this._child,new MatTable(i_mat,r, c));
				}
			}
			
			pr.write(String.format("class Mat%dx%d{\n",i_mat._size,i_mat._size));
			//メンバ変数リストの生成
			for(int r=0;r<i_mat._size;r++){
				pr.write(String.format("\tpublic double "+i_mat._fmt,"m",r,0));
				for(int c=1;c<i_mat._size;c++){
					pr.write(String.format(","+i_mat._fmt,"m",r,c));
				}
				pr.write(";\n");
			}
			pr.write("\tpublic double invers(){\n");
			
			//ローカル変数リストの生成
			for(int r=0;r<i_mat._size;r++){
				pr.write(String.format("\t\tdouble %s="+i_mat._fmt,i_mat._table[r][0].name,"m",r,0));
				for(int c=1;c<i_mat._size;c++){
					pr.write(String.format(",%s="+i_mat._fmt,i_mat._table[r][c].name,"m",r,c));
				}
				pr.write(";\n");
			}
			
			//変数ツリーのダンプ
			int[] tmp=new int[3];
			this.dumpTree(pr,tmp);

			//出力変数の生成
			pr.println(String.format("\t\tdouble det=1/%s;",det));
			tmp[1]++;
			tmp[2]++;
			for(int r=0;r<i_mat._size;r++){
				for(int c=0;c<i_mat._size;c++){
					boolean f=(r+c)%2==0;
					pr.write(String.format("\t\t"+i_mat._fmt+"=%s%s*det;\n","m",r,c,f?"":"-",matname[c][r]));
					tmp[1]++;
					tmp[2]++;
				}
			}
			String ts=String.format("\t\t//total sum=%d,mul=%d,copy=%d\n",tmp[0],tmp[1],tmp[2]);
			pr.format(ts);
			System.out.println(ts);
			pr.write("\t\treturn det;\n\t}\n}\n");
			pr.flush();
			
		}
		private void dumpTree(PrintWriter i_out,int[] tmp) throws IOException
		{
			//System.out.println(this._depth);
			if(this._child!=null){
				this._child.dumpTree(i_out,tmp);
			}
			int s=0;
			int m=0;
			int c=0;
			for(Map.Entry<String,Data> i:this._map.entrySet()){
				Data d=i.getValue();
				i_out.write("\t\tdouble "+d.vname+"="+d.func+";//"+i.getKey()+"\n");
				m+=d.number_of_mul;
				s+=d.number_of_sum;
				c++;
			}
			tmp[1]+=m;
			tmp[0]+=s;
			tmp[2]+=c;
			
			i_out.write(
				String.format("\t\t//lv%d=%d;mul=%d,sum=%d,copy=%s\n",
				this._depth,this._map.size(),m,s,c)
				);
			i_out.flush();
		}
	}
	public static void putResult(int d,OutputStream o){
		MatrixTree m33=new MatrixTree(d);
		System.out.println("//D"+d+" start="+new Date().toString());
		long s=System.currentTimeMillis();
		try {
			m33.inversMat(new MatTable(d),o);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("//result="+(System.currentTimeMillis()-s)+"ms");
	}

	public static void main(String[] args)
	{
		try {
			for(int i=19;;i++){
				File file = new File(String.format("f:\\Mat%dx%d.java",i,i));
				FileOutputStream fso=new FileOutputStream(file);
				putResult(i,fso);
				fso.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
}
