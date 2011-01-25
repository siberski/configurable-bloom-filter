/*
 * Copyright 2010 Tom Gibara
 * adapted by Wolf Siberski
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package de.siberski.bf;

import de.siberski.bf.BloomFilter;
import de.siberski.bf.BloomFilterMaker;
import de.siberski.bf.Hasher;
import de.siberski.bf.JavaRandomHasher;

public class BloomFilterAnalyzer {

	public static void main(String[] args) {

		final int count = 5000; // number of elements to test over
		final int max = 100000; // max number of elements to insert
		final int step = 1000; // step size from zero to max

		// final int size = 3000; // size of bloom filter
		// final int k = 3; // number of hashes to use

		final double desiredFpp = 0.5; // the false positive probability upper
										// bound

		testFilter(count, max, step, desiredFpp, new JavaRandomHasher.Maker(),
				"Java Random");
		testFilter(count, max, step, desiredFpp, new WangJenkinsHasher.Maker(),
				"Wang/Jenkins");
		testFilter(count, max, step, desiredFpp, new MersenneHasher.Maker(),
				"Mersenne Twister");
	}

	private static void testFilter(final int count, final int max,
			final int step, double desiredFpp, final Hasher.Maker hasherMaker,
			final String hashAlgoName) {
		BloomFilterMaker<Integer> maker = new BloomFilterMaker<Integer>()
				.desiredFalsePositiveProbability(desiredFpp, max).hasher(
						hasherMaker);
		final BloomFilter<Integer> bf = maker.makeFilter();

		System.out.println("Starting tests for " + hashAlgoName + " hasher");
		final long startTime = System.currentTimeMillis();
		for (int i = 0; i < max; i += step) {
			// clear ready for this run
			bf.clear();
			// add i elements
			for (int j = 0; j < i; j++) {
				int e = invertBits(j);
				bf.add(e);
			}
			// count false positives
			int fpCount = 0;
			for (int j = i; j < count; j++) {
				if (bf.mightContain(j)) {
					fpCount++;
				}
			}

			int expected = expectedTrueBits(bf, i);
			int observed = bf.getBits().cardinality();
			int length = bf.getLength();
			double deviation;
			if (observed == 0 && expected == 0) {
				deviation = 0.0;
			} else {
				deviation = (double) (observed - expected) / expected * 100;
			}

			System.out.println("Length: " + length + ", expected: " + expected
					+ " , observed: " + observed + ", deviation: " + deviation
					+ "%");
		}
		final long finishTime = System.currentTimeMillis();
		System.out.println("(Time taken " + (finishTime - startTime) + "ms)\n");
	}

	private static int invertBits(int j) {
		int result = 0;
		for (int i = 0; i < 32; i++) {
			int mask = (1 << i);
			boolean set = (j & mask) != 0;
			if (set) {
				int setMask = 1 << (31 - i);
				result |= setMask;
			}
		}
		return result;
	}

	public static int expectedTrueBits(BloomFilter<?> bf, int elemCount) {
		double m = bf.getLength();
		double k = bf.getHashCount();
		double n = elemCount;
		double expected = m * (1 - (Math.pow((1 - 1 / m), k * n)));
		return (int) Math.round(expected);
	}
}
