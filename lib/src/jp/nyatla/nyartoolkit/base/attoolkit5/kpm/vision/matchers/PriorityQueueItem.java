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
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers;

/**
 * The nodes in the tree are sorted as they are visited when a QUERY is done.
 * This class represents an entry in a priority queue to revisit certains nodes
 * in a back-trace.
 */
public class PriorityQueueItem implements Comparable {

	public PriorityQueueItem() {
		this.mNode = null;
		this.mDistance = 0;
	}

	public PriorityQueueItem(Node i_node, int i_dist) {
		this.mNode = i_node;
		this.mDistance = i_dist;
	}

	/**
	 * Get pointer to node.
	 */
	public Node node() {
		return this.mNode;
	}

	/**
	 * Distance to cluster center.
	 */
	public int dist() {
		return this.mDistance;
	}

	// /**
	// * Operator for sorting the queue. Smallest item is always the first.
	// */
	// boolean operator<(const PriorityQueueItem& item) const {
	// return mDistance > item.mDistance;
	// }

	// Pointer to the node
	private Node mNode;
	// Distance from cluster center
	private int mDistance;
	@Override
	public int compareTo(Object o) {
		PriorityQueueItem p=(PriorityQueueItem)o;
		if(this.mDistance>p.mDistance){
			return 1;
		}else if(this.mDistance<p.mDistance){
			return -1;
		}
		return 0;
	}

}
