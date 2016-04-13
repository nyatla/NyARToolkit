/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.core.kpm.utils;

public class Hamming {
	/**
	 * Hamming distance for 32 bits.
	 */
	public static int HammingDistance32(int a, int b) {
		final int m1 = 0x55555555; // 0101...
		final int m2 = 0x33333333; // 00110011..
		final int m4 = 0x0f0f0f0f; // 4 zeros, 4 ones
		final int h01 = 0x01010101; // the sum of 256 to the power of 0,1,2,...

		int x;

		x = a ^ b;
		x -= (x >> 1) & m1; // put count of each 2 bits into those 2 bits
		x = (x & m2) + ((x >> 2) & m2); // put count of each 4 bits into those 4 bits
		x = (x + (x >> 4)) & m4; // put count of each 8 bits into those 8 bits

		return (x * h01) >> 24; // returns left 8 bits of x + (x<<8) + (x<<16) + (x<<24) + ...
	}

	public static int HammingDistance64(long a, long b) {
		long bits = a ^ b;
		bits = (bits & 0x5555555555555555L) + (bits >> 1 & 0x5555555555555555L);
		bits = (bits & 0x3333333333333333L) + (bits >> 2 & 0x3333333333333333L);
		bits = (bits & 0x0f0f0f0f0f0f0f0fL) + (bits >> 4 & 0x0f0f0f0f0f0f0f0fL);
		bits = (bits & 0x00ff00ff00ff00ffL) + (bits >> 8 & 0x00ff00ff00ff00ffL);
		bits = (bits & 0x0000ffff0000ffffL) + (bits >> 16 & 0x0000ffff0000ffffL);
		bits = (bits & 0x00000000ffffffffL) + (bits >> 32 & 0x00000000ffffffffL);
		return (int) bits;
	}

	public static int HammingDistance32(byte[] a, int a_idx, byte[] b, int b_idx) {
		int ai = ((0xff & a[a_idx + 0]) << 24) | ((0xff & a[a_idx + 1]) << 16) | ((0xff & a[a_idx + 2]) << 8)
				| ((0xff & a[a_idx + 3]));
		int bi = ((0xff & b[b_idx + 0]) << 24) | ((0xff & b[b_idx + 1]) << 16) | ((0xff & b[b_idx + 2]) << 8)
				| ((0xff & b[b_idx + 3]));
		return HammingDistance32(ai, bi);
	}

	public static int HammingDistance64(byte[] a, int a_idx, byte[] b, int b_idx) {
		long ai = ((0xffL & a[a_idx + 0]) << 56) | ((0xffL & a[a_idx + 1]) << 48) | ((0xffL & a[a_idx + 2]) << 40)
				| ((0xffL & a[a_idx + 3]) << 32) | ((0xffL & a[a_idx + 4]) << 24) | ((0xffL & a[a_idx + 5]) << 16)
				| ((0xffL & a[a_idx + 6]) << 8) | ((0xffL & a[a_idx + 7]));
		long bi = ((0xffL & b[b_idx + 0]) << 56) | ((0xffL & b[b_idx + 1]) << 48) | ((0xffL & b[b_idx + 2]) << 40)
				| ((0xffL & b[b_idx + 3]) << 32) | ((0xffL & b[b_idx + 4]) << 24) | ((0xffL & b[b_idx + 5]) << 16)
				| ((0xffL & b[b_idx + 6]) << 8) | ((0xffL & b[b_idx + 7]));
		return HammingDistance64(ai, bi);
	}

}