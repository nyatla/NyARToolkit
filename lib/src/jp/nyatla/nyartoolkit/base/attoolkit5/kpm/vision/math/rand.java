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
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math;

public class rand {
	static final int FAST_RAND_MAX=32767;
    private int _seed;
    public rand(int i_seed)
    {
    	this._seed=i_seed;
    	
    }
    /**
     * Implements a fast random number generator. 
     *
     * http://software.intel.com/en-us/articles/fast-random-number-generator-on-the-intel-pentiumr-4-processor/
     */
    public int fastrandom()
    {
        this._seed = (214013*this._seed+2531011);
        return (this._seed>>16)&0x7FFF;
    }
    
    
    /**
     * Shuffle the elements of an array.
     *
     * @param[in/out] v Array of elements
     * @param[in] pop_size Population size, or size of the array v
     * @param[in] sample_size The first SAMPLE_SIZE samples of v will be shuffled
     * @param[in] seed Seed for random number generator
     */
    public void ArrayShuffle(int[] v,int idx,int pop_size, int sample_size) {
        for(int i = 0; i < sample_size; i++) {
            int k = fastrandom()%pop_size;
            int t=v[idx+i];
//            std::swap(v[i], v[k]);
            v[idx+i]=v[idx+k];
            v[idx+k]=t;
        }
    }
}
