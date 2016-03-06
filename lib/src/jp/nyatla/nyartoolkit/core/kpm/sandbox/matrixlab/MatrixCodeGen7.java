package jp.nyatla.nyartoolkit.core.kpm.sandbox.matrixlab;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatrixCodeGen7 extends MatTable{
	public MatrixCodeGen7(int i_size) {
		super(i_size);
	}
	/**
	 * ベースクラス
	 */
	abstract public class Factor{
		public Factor(){
		}
		abstract public String toString();
		public boolean equals(Object e)
		{
			Factor te=((Factor)e);
			return te.toString().compareTo(this.toString())==0;
		}
		public int hashCode()
		{
			return this.toString().hashCode();
		}		
	}
	public class ValueFactor extends Factor{
		final public String val;
		public ValueFactor(String i_val){
			this.val=i_val;
		}
		@Override
		public String toString()
		{
			return val;
		}
	}	
	/** n項の和*/
	public class SumFactor extends Factor
	{
		class Item{
			final public int flag;
			final public Factor factor;
			Item(int i_flag,Factor i_factor){
				this.flag=i_flag;
				this.factor=i_factor;
			}
		}
		final List<Item> l=new ArrayList<Item>();
		public SumFactor()
		{
		}
		@Override
		public String toString()
		{
			String s="";
			for(Item i:this.l){
				s+=(i.flag>0?"+":"-")+i.factor.toString();
			}
			return "("+s.substring(1)+")";
		}
		public void add(int i_flag,Factor f){
			this.l.add(new Item(i_flag,f));
		}
	}
	/** 2項の積*/
	public class MulSection extends Factor{
		final public Factor first;
		final public Factor second;
		final int first_flag;
		final int second_flag;
		public MulSection(int i_ff,Factor f,int i_fs,Factor s){
			this.first=f;
			this.first_flag=i_ff;
			this.second=s;
			this.second_flag=i_fs;
		}
		@Override
		public String toString()
		{
			String f1=this.first_flag>0?"":"-";
			String f2=this.second_flag>0?"":"-";
			return f1+first.toString()+"*"+f2+second.toString();
		}
	}
	public class Tag{
		public int id;
		public Factor src_section;
		public Factor dst_section;
		public int lv;
		public Tag(int i_value_id,Factor s){
			this.id=i_value_id;
			this.lv=-1;
			this.src_section=s;
			this.dst_section=new ValueFactor("v"+i_value_id);
			return;
		}

	}
	public class ElementMap extends HashMap<Factor,Tag>
	{
		private int serial_number=0;
		private static final long serialVersionUID = -5465103849523547316L;
		public Factor register(Factor i_key)
		{
			Tag t=null;
			if(this.get(i_key)==null){
				t=new Tag(serial_number,i_key);
				this.put(i_key,t);
				serial_number++;
			}else{
				t=this.get(i_key);
			}
			return t.dst_section;
//			return t.src_section;
		}
	
	}
	protected MatrixCodeGen7(int i_size,MatItem[][] t) {
		super(i_size,t);
	}


	/**
	 * 絶対値の文字列を返す。
	 * @return
	 */
	public Factor absStr(ElementMap map){
		if(this._size==2){
			String s11=this._table[0][0].getStr();
			String s12=this._table[0][1].getStr();
			String s21=this._table[1][0].getStr();
			String s22=this._table[1][1].getStr();
			String v3=String.format("(%s*%s-%s*%s)",s11,s22,s12,s21);
			Factor r=new ValueFactor(v3);
			return map.register(r);
		}else{
			SumFactor ss=new SumFactor();
			for(int i=0;i<this._size;i++){
				MatrixCodeGen7 c=new MatrixCodeGen7(this._size-1,this.getCofactor(0,i));
				Factor s=new MulSection(
					1,
					new ValueFactor(this._table[0][i].getStr()),
					1,
					c.absStr(map));
				ss.add(this.getCofactorFlag(0, i),s);
			}
			return map.register(ss);
		}
	}
	public String inversMatrix()
	{
		ElementMap map=new ElementMap();
		String matstr="double det="+this.absStr(map).toString()+";\n";
		for(int r=0;r<this._size;r++){
			for(int c=0;c<this._size;c++){
				int f=this.getCofactorFlag(r, c);
				Factor val=new MatrixCodeGen7(this._size-1,this.getCofactor(r, c)).absStr(map);
				//名前は転値
				matstr+=String.format("double M%d%d=(%s%s)/det;\n",c,r,f>0?"":"-", val);
			}				
		}
		//MAPの結果をListに転送
		List<Map.Entry<Factor, Tag>> l=new ArrayList<Map.Entry<Factor, Tag>>();
		for(Map.Entry<Factor, Tag> e : map.entrySet()){
			l.add(e);
		}
		//順番にソート
		Collections.sort(l, new Comparator<Map.Entry<Factor, Tag>>(){			 
            @Override
            public int compare(
            		Map.Entry<Factor, Tag> entry1, Map.Entry<Factor, Tag> entry2) {
                return entry1.getValue().id<entry2.getValue().id?-1:1;
            }
        });
		String elemstr="";
		for(Map.Entry<Factor, Tag> e : l){
			Tag t=map.get(e.getKey());
			elemstr+=("double "+t.dst_section+"="+t.src_section+";//\n");
		}
		return elemstr+matstr;
	}
	public static void main(String[] args){
		MatrixCodeGen7 m33=new MatrixCodeGen7(8);
		String s=m33.inversMatrix();
		System.out.println(s);
//		System.out.println(m33.inversMatrix());
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
