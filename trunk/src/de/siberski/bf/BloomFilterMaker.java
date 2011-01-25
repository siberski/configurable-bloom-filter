/*
 * Copyright (C) 2010 Wolf Siberski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.siberski.bf;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.max;

/**
 * BloomFilterMaker is a helper class for configuration of BloomFilters. Usage:
 * create new BloomFilterMaker, call configuration methods (length(),
 * hashCount(), etc.), and then call makeFilter().
 * 
 * @param <E>
 *            type of objects to be stored in the filter
 */
public class BloomFilterMaker<E> {
	// length of the Bloom filter
	int length = -1;

	// number of hashes for the created filter
	int hashCount = -1;

	// desired false positive probability
	double fpp = -1.0;

	// maximum number of elements for which the false positive probability
	// should hold
	int maxElements = -1;

	// the hasher to be used in the created filter
	Hasher.Maker hasherMaker = null;

	/**
	 * Builds a new filter with the specified characteristics. This method does
	 * not alter the state of this {@code MapMaker} instance, so it can be
	 * invoked again to create multiple independent maps.
	 * 
	 * @return a Bloom filter with the specified characteristics
	 */
	public BloomFilter<E> makeFilter() {
		// characteristics can be either specified
		// explicitly via length and Hasher.Maker.hashCount
		// implicitly fpp and maxElements
		boolean explicitlyDefined = (length > 0 && hashCount > 0);
		boolean implicitlyDefined = (fpp > 0.0 && maxElements > 0);
		assert (explicitlyDefined || implicitlyDefined)
				&& !(explicitlyDefined && implicitlyDefined);

		if (implicitlyDefined) {
			computeLengthAndHashCount();
		}

		if (hasherMaker == null) {
			hasherMaker = new WangJenkinsHasher.Maker();
		}
		hasherMaker.hashCount(this.hashCount);

		return new BloomFilter<E>(hasherMaker.makeHasher(), length);
	}

	/**
	 * specifies the length of the Bloom filter
	 */
	public BloomFilterMaker<E> length(int length) {
		if (length < 1) {
			throw new IllegalArgumentException("length must be larger than 0");
		}
		if (this.length != -1) {
			throw new IllegalStateException("length already set to "
					+ this.length);
		}
		if (this.fpp != -1.0) {
			throw new IllegalStateException(
					"specifying false positive probability and length is not allowed");
		}
		this.length = length;
		return this;
	}

	/**
	 * specifies the number of hashes to be used
	 */
	public BloomFilterMaker<E> hashCount(int hashCount) {
		if (hashCount < 1) {
			throw new IllegalArgumentException(
					"hashCount must be larger than 0");
		}
		if (this.hashCount != -1) {
			throw new IllegalStateException("hashCount already set to "
					+ this.hashCount);
		}
		if (this.fpp != -1.0) {
			throw new IllegalStateException(
					"specifying false positive probability and hashCount is not allowed");
		}
		this.hashCount = hashCount;
		return this;
	}

	/**
	 * Specifies the hashing algorithm for the BloomFilter. Default is the
	 * Wang-Jenkins hashing.
	 */
	public BloomFilterMaker<E> hasher(Hasher.Maker hasherMaker) {
		if (this.hasherMaker != null) {
			throw new IllegalStateException("hasher already set to "
					+ hasherMaker);
		}
		this.hasherMaker = hasherMaker;

		return this;
	}

	/**
	 * sets the optimal length and number of hashes for the filter, given an
	 * upper bound for the false positive probability and an upper bound for the
	 * number of elements to be stored in the filter.
	 * 
	 * @param fpp
	 *            upper bound for the false positive probability
	 * @param maxElements
	 *            the expected maximum number of elements stored in the filter
	 */
	public BloomFilterMaker<E> desiredFalsePositiveProbability(double fpp,
			int maxElements) {
		if (fpp <= 0.0 || fpp >= 1.0) {
			throw new IllegalArgumentException(
					"false positive probability must be between 0.0 and 1.0");
		}
		if (maxElements < 1) {
			throw new IllegalArgumentException(
					"maxElements must be larger than 0");
		}
		if (this.fpp != -1.0) {
			throw new IllegalStateException(
					"false positive probability already set to " + this.fpp);
		}
		if (this.length != -1.0) {
			throw new IllegalStateException(
					"specifying length and false positive probability is not allowed");
		}
		if (this.hashCount != -1.0) {
			throw new IllegalStateException(
					"specifying hashCount and false positive probability is not allowed");
		}

		this.fpp = fpp;
		this.maxElements = maxElements;

		return this;
	}

	private void computeLengthAndHashCount() {
		// determine optimal length
		int optLength = (int) ceil(abs(maxElements * log(fpp) / pow(log(2), 2)));
		int offset = Long.SIZE - (optLength % Long.SIZE);
		optLength += offset; // round to next full word length
		length = optLength;

		// determine optimal number of hash functions
		int optHashCount = max(1, (int) ceil((length / maxElements) * log(2)));
		hashCount = optHashCount;
	}

}
