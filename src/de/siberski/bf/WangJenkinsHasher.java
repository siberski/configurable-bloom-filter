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

import java.util.BitSet;

/**
 * This hasher uses a variant of Wang-Jenkins hashing
 * as pseudo-random number generator. 
 * (copied from Doug Leas java.util.concurrent.HashMap). 
 * Double hashing copied from Benjamin Manes' BloomFilter
 * class at http://code.google.com/p/concurrentlinkedhashmap/wiki/BloomFilter 
 *
 */
public class WangJenkinsHasher extends Hasher {
	public static class Maker extends Hasher.Maker {
		@Override
		public Hasher makeHasher() {
			assert hashCount > 0;
			return new WangJenkinsHasher(this);
		}
	}

	public WangJenkinsHasher(WangJenkinsHasher.Maker maker) {
		super(maker);
	}

	@Override
	public int addKey(int key, BitSet bits) {
		int additionalTrueBits = 0;

        // Double hashing allows calculating multiple index locations
		int length = bits.size();
        int probe = 1 + abs(key % length);
        int seed = scramble(key);
        for (int i=0; i<getHashCount(); i++) {
        	int hash = abs(seed ^ i*probe) % length;
			if(!bits.get(hash)){
				additionalTrueBits++;
				bits.set(hash);
			}
        }
        return additionalTrueBits;
	}

	@Override
	public boolean testKey(int key, BitSet bits) {
        // Double hashing allows calculating multiple index locations
		int length = bits.size();
        int probe = 1 + abs(key % length);
        int seed = scramble(key);
        for (int i=0; i<getHashCount(); i++) {
        	int hash =  abs(seed ^ i*probe) % length;
            if ( ! bits.get( hash ) ) {
            	return false;
            }
        }
		return true;
	}

	
	
    /**
     * Doug Lea's hashing algorithm used in the collection libraries.
     */
    private int scramble(int key) {
        // Spread bits using variant of single-word Wang/Jenkins hash
        key += (key <<  15) ^ 0xffffcd7d;
        key ^= (key >>> 10);
        key += (key <<   3);
        key ^= (key >>>  6);
        key += (key <<   2) + (key << 14);
        return key ^ (key >>> 16);
    }
}
