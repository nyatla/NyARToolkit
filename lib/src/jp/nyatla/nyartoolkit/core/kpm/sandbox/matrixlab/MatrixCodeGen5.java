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

public class MatrixCodeGen5 extends MatTable{
	public MatrixCodeGen5(int i_size) {
		super(i_size);
	}
	abstract public class Section{
		public int flag;
		public Section(int i_flag){
			this.flag=i_flag;
		}
		abstract public String toString();
	}
	public class ValueSection extends Section{
		final public String val;
		public ValueSection(String i_val,int i_flag){
			super(i_flag);
			this.val=i_val;
		}
		@Override
		public String toString()
		{
			return "("+val+")";
		}
	}	
	/** n項の和*/
	public class SumSection extends Section{
		final List<Section> l=new ArrayList<Section>();
		public SumSection(int i_flags)
		{
			super(i_flags);
		}
		@Override
		public String toString(){
			String s="";
			for(Section i:this.l){
				s+=(i.flag>0?"+":"-")+i.toString();
			}
			return "("+s.substring(1)+")";
		}
		public void add(Section s){
			this.l.add(s);
		}
	}
	/** 2項の積*/
	public class MulSection extends Section{
		final public Section first;
		final public Section second;
		public MulSection(Section f,Section s,int i_flags){
			super(i_flags);
			this.first=f;
			this.second=s;
		}
		@Override
		public String toString()
		{
			String f1=first.flag>0?"":"-";
			String f2=second.flag>0?"":"-";
			return "("+f1+first.toString()+")*("+f2+second.toString()+")";
		}
	}
	public class Tag{
		public int ref_count;
		public int value_id;
		public int lv;
		public Section section;
		public String getValueName(){
			return "v"+value_id;
		}
		public Tag(int i_ref_count,int i_value_id,Section s){
			this.ref_count=i_ref_count;
			this.value_id=i_value_id;
			this.lv=-1;
			return;
		}
	}
	public class ElementMap extends HashMap<String,Tag>
	{
		private int serial_number=0;
		private static final long serialVersionUID = -5465103849523547316L;
		public Tag register(Section i_key)
		{
			String k=i_key.toString();
			Tag t=null;
			if(this.get(k)==null){
				this.put(k,new Tag(1,serial_number,i_key));
				serial_number++;
			}else{
				t=this.get(k);
				t.ref_count++;
			}
			return t;
		}
	}
	protected MatrixCodeGen5(int i_size,MatItem[][] t) {
		super(i_size,t);
	}


	/**
	 * 絶対値の文字列を返す。
	 * @return
	 */
	public Section absStr(ElementMap map,int flag){
		if(this._size==2){
			String s11=this._table[0][0].getStr();
			String s12=this._table[0][1].getStr();
			String s21=this._table[1][0].getStr();
			String s22=this._table[1][1].getStr();
			String v3=String.format("(%s*%s)-(%s*%s)",s11,s22,s12,s21);
			return new ValueSection(v3,flag);		
		}else{
			SumSection ss=new SumSection(flag);
			for(int i=0;i<this._size;i++){
				MatrixCodeGen5 c=new MatrixCodeGen5(this._size-1,this.getCofactor(0,i));
				Section s=new MulSection(
					new ValueSection(this._table[0][i].getStr(),1),
					c.absStr(map,this.getCofactorFlag(0, i)),1);
				ss.add(s);
			}
			return ss;
		}
	}
	public String inversMatrix()
	{
		ElementMap map=new ElementMap();
		String matstr="double det="+this.absStr(map,1).toString()+";\n";
		for(int r=0;r<this._size;r++){
			for(int c=0;c<this._size;c++){
				int f=this.getCofactorFlag(r, c);
				Section val=new MatrixCodeGen5(this._size-1,this.getCofactor(r, c)).absStr(map,f);
				//名前は転値
				matstr+=String.format("double M%d%d=(%s%s)/det;\n",c,r,val.flag>0?"":"-", val);
			}				
		}
		//MAPの結果をListに転送
		List<Map.Entry<String, Tag>> l=new ArrayList<Map.Entry<String, Tag>>();
		for(Map.Entry<String, Tag> e : map.entrySet()){
			l.add(e);
		}
		//順番にソート
		Collections.sort(l, new Comparator<Map.Entry<String, Tag>>(){			 
            @Override
            public int compare(
            		Map.Entry<String, Tag> entry1, Map.Entry<String, Tag> entry2) {
                return entry1.getValue().value_id<entry2.getValue().value_id?-1:1;
            }
        });
		String elemstr="";
		for(Map.Entry<String, Tag> e : l){
			String sk=e.getKey();
			elemstr+=("double "+map.get(sk).getValueName()+"="+sk+";//"+e.getValue().ref_count+"\n");
		}
		return elemstr+matstr;
	}
	public static void main(String[] args){
		MatrixCodeGen5 m33=new MatrixCodeGen5(4);
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
