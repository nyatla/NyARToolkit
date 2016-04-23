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
package jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering.selector;


import java.util.PriorityQueue;

import jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering.BinaryHierarchicalNode;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.utils.LongDescripter768;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;


/**
 * 最適化した{@link BinaryHierarchicalSelector}
 * 最適化内容は、実行時newの削除
 */
public class BinaryHierarchicalSelector_O1
{
	public class Queue extends PriorityQueue<NodeStack.Item> {
		private static final long serialVersionUID = 6120329703806461621L;

	}
	final public FreakFeaturePoint[] _result;
	private int _num_of_result;
	public BinaryHierarchicalSelector_O1(int i_MaxNodesToPop,int i_max_result)
	{
		this.mMaxNodesToPop = i_MaxNodesToPop;
		this._result=new FreakFeaturePoint[i_max_result];
		return;
	}
	/**
	 * Query the tree for a reverse index.
	 */
	public int query(BinaryHierarchicalNode i_node,LongDescripter768 feature)
	{
		this._num_of_result=0;
		this.mNumNodesPopped = 0;
		this.mQueue.clear();
		NodeStack nodes = this._node_stack;
		nodes.clear();
		if(i_node.is_leaf){
			this.append(i_node);
		}else{
			this.query(this.mQueue,nodes, i_node, feature);
		}
		return (int) this._num_of_result;
	}

	private class NodeStack extends NyARObjectStack<NodeStack.Item>
	{
		public class Item implements Comparable<Object>{
			public int distance;
			public BinaryHierarchicalNode node;
			@Override
			public int compareTo(Object o) {
				NodeStack.Item p=(NodeStack.Item)o;
				if(this.distance>p.distance){
					return 1;
				}else if(this.distance<p.distance){
					return -1;
				}
				return 0;
			}			
		}
		public NodeStack(int i_length) {
			super(i_length,Item.class);
		}
		protected Item createElement()
		{
			return new Item();
		}
		
	}


	// Node queue
	final private Queue mQueue = new Queue();
	//
	// Number of nodes popped off the priority queue
	private int mNumNodesPopped;

	// Maximum nodes to pop off the priority queue
	final private int mMaxNodesToPop;



	private void append(BinaryHierarchicalNode i_node)
	{
		//末端なら結果配列へ値を追加
		int p=this._num_of_result;
		int l=i_node.reserv_index.length;
		//
		if(l+p>this._result.length){
			l=this._result.length-this._num_of_result;
		}
		for(int i=0;i<l;i++){
			this._result[p+i]=i_node.reserv_index[i];
		}
		this._num_of_result+=l;
		return;
	}
	/**
	 * Recursive function query function.
	 */
	private void query(Queue queue,NodeStack i_nodes,BinaryHierarchicalNode node, LongDescripter768 feature)
	{
		//近傍ノードをスタックに追加
		int sp=i_nodes.getLength();
		int num_of_min=nearest(node,i_nodes, queue, feature);
		
		//先頭からnum_of_min個の最小ノードについて再帰探索
		for (int i = 0; i < num_of_min; i++) {
			BinaryHierarchicalNode n=i_nodes.getItem(sp+i).node;
			if(n.is_leaf){
				this.append(n);
			}else{
				this.query(queue,i_nodes,n,feature);
			}
		}
		//好成績な近傍ノード1つを取り出して探索する。
		if (this.mNumNodesPopped < this.mMaxNodesToPop && !queue.isEmpty()) {
			BinaryHierarchicalNode n = queue.poll().node;// pop();
			this.mNumNodesPopped++;
			if(n.is_leaf){
				this.append(n);
			}else{
				this.query(queue,i_nodes,n,feature);
			}
		}
		return;
	}
	//特徴量の一時バッファ
	final private NodeStack _node_stack=new NodeStack(1000);

    /**
     * Get a queue of all the children nodes sorted by distance from node center.
     */
    private static int nearest(BinaryHierarchicalNode i_node,NodeStack nodes,BinaryHierarchicalSelector_O1.Queue queue,LongDescripter768 feature)
    {
        int mind = Integer.MAX_VALUE;

        int sp=nodes.getLength();
        BinaryHierarchicalNode[] children=i_node.children;
        int num_of_children=children.length;
        
        //最小値の探索
        for(int i = 0; i < num_of_children; i++) {      	
        	NodeStack.Item item=nodes.prePush();
        	if(item==null){
        		//ワークエリアを使い切った。
        		return 0;
        	}
            int d = children[i].center.descripter.hammingDistance(feature);
            item.node=children[i];
            item.distance=d;
            if(d < mind) {
                mind = d;
            }
        }
        int num_of_min=0;
        //最小値以外をキューに追記
        for(int i=0;i<num_of_children;i++){
        	NodeStack.Item item=nodes.getItem(sp+i);
        	if(item.distance==mind){
        		//最小値は先頭に移動
        		nodes.swap(sp+num_of_min,sp+i);
        		num_of_min++;
        	}else{
        		//最小値以外はキューに追加
        		queue.add(item);
        		
        	}
        }        
        //最小値の数は(min_add_idx-s_idx)
        return num_of_min;
    }
	


}
