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
 * This hasher uses a Mersenne Twister as pseudo-random number generator.
 * 
 */
public class MersenneHasher extends Hasher {
	public static class Maker extends Hasher.Maker {
		@Override
		public Hasher makeHasher() {
			assert hashCount > 0;
			return new MersenneHasher(this);
		}
	}

	public MersenneHasher(MersenneHasher.Maker maker) {
		super(maker);
	}

	@Override
	public int addKey(int key, BitSet bits) {
		int additionalTrueBits = 0;
		// we need to create a new MersenneTwister to
		// ensure thread-safety
		MersenneTwisterFast mt = new MersenneTwisterFast(key);
		int upperBound = bits.size();
		for (int i = 0; i < getHashCount(); i++) {
			int hash = mt.nextInt(upperBound);
			if (!bits.get(hash)) {
				additionalTrueBits++;
				bits.set(hash);
			}
		}
		return additionalTrueBits;
	}

	@Override
	public boolean testKey(int key, BitSet bits) {
		// we need to create a new MersenneTwister to
		// ensure thread-safety
		MersenneTwisterFast mt = new MersenneTwisterFast(key);
		int upperBound = bits.size();
		for (int i = 0; i < getHashCount(); i++) {
			int hash = mt.nextInt(upperBound);
			if (!bits.get(hash)) {
				return false;
			}
		}
		return true;
	}
}
