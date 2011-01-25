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

import java.util.BitSet;

/**
 * This class encapsulates the hashing algorithm used to compute the hash values
 * of a key to be added to a Bloom filter.
 * 
 */

public abstract class Hasher {
	public abstract static class Maker {
		int hashCount = -1;

		public abstract Hasher makeHasher();

		public Hasher.Maker hashCount(int hashCount) {
			this.hashCount = hashCount;
			return this;
		}
	}

	// number of bits to be set for each element
	private int hashCount = -1;

	/**
	 * creates a new hasher
	 * 
	 * @param hashCount
	 *            number of hash functions, i.e., number of bits set per element
	 */
	public Hasher(Maker maker) {
		setHashCount(maker.hashCount);
	}

	/**
	 * hashes the key into the bit set.
	 * 
	 * Note to implementers: this method needs to be thread safe.
	 * 
	 * @param key
	 *            key to be inserted
	 * @param bits
	 *            bit set
	 * @return number of bits who were set to true as result of this operation
	 */
	public abstract int addKey(int key, BitSet bits);

	/**
	 * checks if the key is in the bit set. As usual for Bloom filters, this
	 * check can be false positive.
	 * 
	 * Note to implementers: this method needs to be thread safe.
	 * 
	 * @param key
	 *            key to be checked
	 * @param bits
	 *            bit set
	 * @return {@code true} if all bit positions for this key are set,
	 *         {@code false} otherwise
	 */
	public abstract boolean testKey(int key, BitSet bits);

	/**
	 * returns the number of hash functions used in this hasher
	 */
	public int getHashCount() {
		return hashCount;
	}

	/**
	 * sets the number of hash functions
	 */
	protected void setHashCount(int hashCount) {
		if (hashCount < 1) {
			throw new IllegalArgumentException(
					"hashCount must be larger than 0");
		}
		if (this.hashCount != -1) {
			throw new IllegalStateException("hashCount already set to "
					+ this.hashCount);
		}

		this.hashCount = hashCount;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Hasher))
			return false;
		if (this == other)
			return true;
		Hasher otherHasher = (Hasher) other;
		return getClass() == other.getClass()
				&& hashCount == otherHasher.hashCount;
	}

	@Override
	public int hashCode() {
		return hashCount;
	}

	@Override
	public String toString() {
		return getClass().getName() + "[" + hashCount + "]";
	}
}
