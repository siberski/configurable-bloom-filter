/*
 * Copyright (C) 2010 Wolf Siberski
 * 
 * API and Javadoc partly copied from the proposal by Kevin Bourrillion at
 * http://code.google.com/p/guava-libraries/issues/detail?id=12  
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

import java.io.Serializable;
import java.util.BitSet;

/**
 * A probabilistic "shadow" of a set of elements, useful when the set itself
 * would be too expensive to maintain in memory and query directly. A Bloom
 * filter can give false positives, but never false negatives. That is, adding
 * an element to the filter guarantees that {@link #mightContain} will return
 * {@code true}, but {@link #mightContain} returning {@code true} does not
 * guarantee that this element was ever actually added to the filter.
 */
public class BloomFilter<E> implements Serializable {
	// Serializable version number
	private static final long serialVersionUID = 1L;

	// Hashing algorithm of this filter
	protected Hasher hasher;

	// Current number of true bits
	protected int trueBitCount;

	// Bit array representing the added elements
	protected BitSet bits;

	/**
	 * Creates a new BloomFilter
	 * 
	 * @param hasher
	 *            the hashing algorithm
	 * @param length
	 *            the length of this filter's bit array
	 */
	protected BloomFilter(Hasher hasher, int length) {
		this.hasher = hasher;
		this.bits = new BitSet(length);
		trueBitCount = 0;
	}

	/**
	 * Returns {@code true} if it is <i>possible</i> (probability nonzero) that
	 * {@code element} is contained in the set represented by this Bloom filter.
	 * If this method returns {@code false}, this element is <i>definitely</i>
	 * not present. If it {@code true}, the probability that this element has
	 * <i>not</i> actually been added is given by
	 * {@link #getFalsePositiveProbability()}.
	 */
	public boolean mightContain(E element) {
		int key = 0;
		if (element != null) {
			key = element.hashCode();
		}

		return hasher.testKey(key, bits);
	}

	/**
	 * Adds {@code newElement} to this Bloom filter, so that
	 * {@code mightContain(newElement)} is now guaranteed to return {@code true}
	 * .
	 * 
	 * @return true if the Bloom filter changed as a result of this call
	 */
	public boolean add(E newElement) {
		// compute object key
		int key = 0;
		if (newElement != null) {
			key = newElement.hashCode();
		}

		// set bits according to hashes
		int additionalTrueBits = hasher.addKey(key, bits);

		trueBitCount += additionalTrueBits;
		return additionalTrueBits > 0;
	}

	/**
	 * Adds all elements to this Bloom filter.
	 * 
	 * @param elements
	 *            the elements to be added
	 * @return true if the Bloom filter changed as a result of this call
	 */
	public boolean addAll(Iterable<? extends E> elements) {
		boolean bfChanged = false;
		for (E element : elements) {
			bfChanged = add(element) || bfChanged;
		}
		return bfChanged;
	}

	/**
	 * Adds all elements of another Bloom filter to this Bloom filter. This is
	 * the union of the sets represented by the filters, plus possibly
	 * additional false positives. Only filters with identical length and
	 * hashCount can be combined.
	 * 
	 * @param filter
	 *            Bloom filter of the elements to be added.
	 */
	public void addAll(BloomFilter<E> filter) {
		assert this.getLength() == filter.getLength()
				&& this.hasher.equals(filter.hasher);
		bits.or(filter.getBits());
	}

	/**
	 * Retains only the elements in this set that are contained in the specified
	 * collection. This is the intersection of the sets represented by the
	 * filters, plus possibly false positives. Only filters with identical
	 * length and hashCount can be combined.
	 * 
	 * @param filter
	 *            Bloom filter of the elements to be retained
	 */
	public void retainAll(BloomFilter<E> filter) {
		assert this.getLength() == filter.getLength()
				&& this.hasher.equals(filter.hasher);
		bits.and(filter.getBits());
	}

	/**
	 * Returns the number of hashes used in this Bloom filter
	 * 
	 * @return number of hashes
	 */
	public int getHashCount() {
		return hasher.getHashCount();
	}

	/**
	 * returns the length of the Bloom filter, i.e., the number of bits used to
	 * represent the set
	 * 
	 * @return length in bits
	 */
	public int getLength() {
		return bits.size();
	}

	/**
	 * returns the false positive probability for this Bloom filter, given the
	 * number of elements already added to the filter.
	 * 
	 * @param numberOfElements
	 *            the number of elements already added to the filter.
	 */
	private double getFalsePositiveProbability(int numberOfElements) {
		return Math.pow(
				1d - Math.pow(1 - 1d / getLength(), getHashCount()
						* numberOfElements), getHashCount());
	}

	/**
	 * Returns the probability that {@link #mightContain} will return
	 * {@code true} for an element not actually contained in this set. Uses the
	 * estimated number of distinct elements added to compute the false positive
	 * probability.
	 */
	public double getFalsePositiveProbability() {
		return getFalsePositiveProbability(estimatedSize());
	}

	/**
	 * Returns an estimation on the number of elements.
	 */
	public int estimatedSize() {
		final double m = getLength();
		final double i = trueBitCount;
		final double k = getHashCount();
		// cast to int is safe here because Integer.MAX_VALUE / hashCount is the
		// upper bound for the estimated number of elements
		if (i == m) {
			return (int) (m / k);
		} else {
			return (int) Math.round(
					Math.log(1.0d - i / m) / (k * Math.log(1.0d - 1.0d / m)));
		}
	}

	/**
	 * Returns {@code true} if this filter contains no elements.
	 * 
	 */
	public boolean isEmpty() {
		return trueBitCount == 0;
	}

	/**
	 * Removes all elements from this filter.
	 * 
	 */
	public void clear() {
		bits.clear();
		trueBitCount = 0;
	}

	/**
	 * returns the BitSet used to represent the elements in this filter
	 */
	protected BitSet getBits() {
		return bits;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof BloomFilter)) {
			return false;
		}
		if (this == other) {
			return true;
		}

		@SuppressWarnings("rawtypes")
		BloomFilter otherFilter = (BloomFilter) other;
		return this.hasher.equals(otherFilter.hasher)
				&& this.bits.equals(otherFilter.getBits());
	}

	@Override
	public int hashCode() {
		return bits.hashCode();
	}

}
