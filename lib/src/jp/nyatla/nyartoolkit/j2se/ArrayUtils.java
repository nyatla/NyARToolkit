package jp.nyatla.nyartoolkit.j2se;

import java.util.List;

public class ArrayUtils {
	public static <T> int[] toIntArray_impl(List<Integer> i_list,int i_offset,int i_size,int[] i_dest){
		for(int i=0;i<i_size;i++){
			i_dest[i]=i_list.get(i_offset+i);
		}
		return i_dest;
	}
	public static int[] toIntArray_impl(List<Integer> i_list,int i_offset,int i_size){
		return toIntArray_impl(i_list,i_offset,i_size,new int[i_size]);
	}
	
}
