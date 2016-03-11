package jp.nyatla.nyartoolkit.core.kpm.sandbox.matrixlab2;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;


public class MatrixCodeGen12{
	static public class Data{
		final String func;
		final String vname;
		public Data(String i_func,String i_vname){
			this.func=i_func;
			this.vname=i_vname;
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
				i_tree._map.put(hash,new Data(v3,v));
				return v;
			}else{
				String f="";
				for(int i=0;i<i_mat._size;i++){
					MatItem a=i_mat._table[0][i];
					MatTable mt=new MatTable(i_mat._size-1,i_mat.getCofactor(0,i));
					String d=detMat(i_tree._child,mt);
					f+=String.format("%s%s*(%s)",i%2==0?"+":"-",a.name,d);
				}
				String v=variableId();
				i_tree._map.put(hash,new Data(f.substring(1),v));

				return v;
			}
		}
		public void inversMat(MatTable i_mat,OutputStream i_stream) throws IOException
		{
			PrintWriter pr=new PrintWriter(i_stream);

			String[][] matname=new String[i_mat._size][i_mat._size];
			String det;
			vid=0;
			det=detMat(this,i_mat);
			for(int r=0;r<i_mat._size;r++){
				for(int c=0;c<i_mat._size;c++){
					matname[r][c]=detMat(this._child,new MatTable(i_mat._size-1,i_mat.getCofactor(r, c)));
				}
			}
			
			
			for(int r=0;r<i_mat._size;r++){
				System.out.print(String.format("double %s",i_mat._table[r][0].name));
				for(int c=1;c<i_mat._size;c++){
					System.out.print(String.format(",%s",i_mat._table[r][c].name));
				}
				System.out.println(";");
			}
			
			
			//変数ツリーのダンプ
			this.dumpTree(pr);
			//出力の
			pr.println(String.format("double det=%s;",det));
			for(int r=0;r<i_mat._size;r++){
				for(int c=0;c<i_mat._size;c++){
					pr.write(String.format("%s=%s/det;\n",i_mat._table[r][c].name,matname[r][c]));
				}
			}		
			pr.flush();
		}
		private void dumpTree(PrintWriter i_out) throws IOException{
			//System.out.println(this._depth);
			if(this._child!=null){
				this._child.dumpTree(i_out);
			}
			i_out.write(String.format("//field%d=%d\n",this._depth,this._map.size()));
			for(Map.Entry<String,Data> i:this._map.entrySet()){
				Data d=i.getValue();
				i_out.write("double "+d.vname+"="+d.func+";//"+i.getKey()+"\n");
			}
		
		}
		
	}
	public static void main(String[] args){
		int D=8;
		MatrixTree m33=new MatrixTree(D);
		long s=System.currentTimeMillis();
		try {
			m33.inversMat(new MatTable(D),System.out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("//"+(System.currentTimeMillis()-s)+"ms");
//		}

			
		return;
//		m33.setZero(0,0);m33.setZero(0,1);m33.setZero(0,2);
//		m33.setZero(1,3);m33.setZero(1,4);m33.setZero(1,5);
//		m33.setZero(2,0);m33.setZero(2,1);m33.setZero(2,2);
//		m33.setZero(3,3);m33.setZero(3,4);m33.setZero(3,5);
//		m33.setZero(4,0);m33.setZero(4,1);m33.setZero(4,2);
//		m33.setZero(5,3);m33.setZero(5,4);m33.setZero(5,5);
//		
//		m33.setZero(6,0);m33.setZero(6,1);m33.setZero(6,2);
//		m33.setZero(7,3);m33.setZero(7,4);m33.setZero(7,5);
//
//		m33.setName(1, 0,"a03");m33.setName(1, 1,"a04");m33.setName(1, 2,"a05");
//		m33.setName(3, 0,"a23");m33.setName(3, 1,"a24");m33.setName(3, 2,"a25");
//		m33.setName(5, 0,"a43");m33.setName(5, 1,"a44");m33.setName(5, 2,"a45");
//		m33.setName(7, 0,"a63");m33.setName(7, 1,"a64");m33.setName(7, 2,"a65");
//		String s=m33.inversMatrix();
//		System.out.println(s);
//
//		try {
//			File file = new File("d:\\mat.txt");
//			FileWriter filewriter = new FileWriter(file);
//			filewriter.write(s);
//			filewriter.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
	}

}
