/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2013-2015 Daqri, LLC.
 *  Author(s): Chris Broaddus
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 *  Copyright (C)2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering.selector.utils;




/**
 * priority queueの代替クラス。
 * distanceが最小のN個のアイテムを保持するクラス。
 * {@link NodeStack.Item}のポインタを保持する。
 *
 */
public class LimitedPriorityNodeList
{

	private NodeStack.Item[] _list;
	private int _len;
	private int _remining_len;
	public LimitedPriorityNodeList(int i_size)
	{
		this._list=new NodeStack.Item[i_size];
		for(int i=0;i<i_size;i++){
			this._list[i]=new NodeStack.Item();
		}
	}
	/**
	 * 状態をリセットする。
	 */
	public void reset()
	{
		this._len=0;
		this._remining_len=this._list.length;
	}

	public boolean push(NodeStack.Item i_node)
	{
		int node_distance=i_node.distance;
		NodeStack.Item[] list=this._list;
		int len=this._len;
		if(len<this._remining_len){
			//空き容量がある場合
			int i;
			for(i=0;i<len;i++){
				if(list[i].distance<node_distance){
					//後方シフトして挿入
					for(int j=len-1;j>=i;j--){
						list[j+1]=list[j];
					}
					list[i]=i_node;
					this._len++;
					return true;
				}
			}
			//終端に追記
			list[len]=i_node;
			this._len++;
			return true;
			
		}else{
			if(list[0].distance<node_distance){
				//最大値よりスコアが大きい場合
				return false;
			}
			//空き容量がない場合
			for(int i=len-1;i>=0;i--){
				if(list[i].distance>node_distance){
					//前方シフトして挿入
					for(int j=0;j<i;j++){
						list[j]=list[j+1];
					}
					list[i]=i_node;
					return true;
				}
			}
			return false;
		}
	}
	/**
	 * 一番distanceの小さいアイテムを返す。
	 */
	public NodeStack.Item popSmallest()
	{
		if(this._len==0){
			return null;
		}
		this._len--;
		this._remining_len--;
		return this._list[this._len];
	}
	/**
	 * Test code
	 * @param args
	 */
	public static void main(String[] args)
	{
////		int[] b=new int[]{0,2,4,6,8,10,9,7,5,3,1};
//		int[] b=new int[100];
//		for(int i=0;i<b.length;i++){
//			b[i]=i;
//		}
//		CandidateNodeList ca=new CandidateNodeList(30);
//		for(int l=0;l<10;l++){
//			ca.reset();
//			for(int j=0;j<10;j++){
//				int idx=(int)(Math.random()*b.length);
//				int idx2=(int)(Math.random()*b.length);
//				int s=b[idx];
//				b[idx]=b[idx2];
//				b[idx2]=s;
//			}
//			for(int i=0;i<b.length;i++){
//				ca.push(null,b[i]);
//				//check
//				for(int p=0;p<ca._len;p++){
//					System.out.print(ca._list[p].distance+" ");
//				}
//				System.out.println();
//				if(i%3==0){
//					NodeStack.Item p=ca.popSmallest();
//					if(p!=null){
//						System.out.println("POP="+p.distance);
//					}
//				}
//			}
//		}
//		return;
	}
}
