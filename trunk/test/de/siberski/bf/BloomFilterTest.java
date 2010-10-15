package de.siberski.bf;

import java.util.Arrays;

import org.junit.Test;

import de.siberski.bf.BloomFilter;
import de.siberski.bf.BloomFilterMaker;
import static org.junit.Assert.*;

/**
 * Tests for BloomFilter operation. BloomFilter relies on hashCode() to set and
 * test for instances. The tests use Integer objects to keep things as simple as
 * possible.
 * 
 */
public class BloomFilterTest {
	public final static int DEFAULT_LENGTH = 64;
	public final static int DEFAULT_HASH_COUNT = 2;

	//TODO this is a very poor test set, needs to be complemented with
	//     automatically generated large sets.
	public Integer[] testInstances = new Integer[] { 42, 0, -3235698,
			Integer.MAX_VALUE, Integer.MIN_VALUE };

	
	public static  BloomFilterMaker<Integer> filterMaker = 
		new BloomFilterMaker<Integer>()
			.length(DEFAULT_LENGTH)
			.hashCount(DEFAULT_HASH_COUNT);

	@Test
	public void testGetters() {
		BloomFilter<Integer> bf = filterMaker.makeFilter();
		assertEquals(DEFAULT_LENGTH, bf.getLength());
		assertEquals(DEFAULT_HASH_COUNT, bf.getHashCount());
	}

	@Test
	public void testEmptyFilter() {
		BloomFilter<Integer> bf = filterMaker.makeFilter();
		assertTrue(bf.isEmpty());

		for (int i : testInstances) {
			assertFalse(bf.mightContain(i));
		}

		assertTrue(0 == bf.estimatedSize());
		assertTrue(0.0 == bf.getFalsePositiveProbability());
	}

	@Test
	public void testAdd() {
		for (Integer i : testInstances) {
			BloomFilter<Integer> bf = filterMaker.makeFilter();
			bf.add(i);
			assertTrue(bf.mightContain(i));
		}
	}

	@Test
	public void testAddAll() {
		BloomFilter<Integer> bf = filterMaker.makeFilter();
		bf.addAll(Arrays.asList(testInstances));

		for (Integer i : testInstances) {
			assertTrue(bf.mightContain(i));
		}
	}

	@Test
	public void testClear() {
		BloomFilter<Integer> bf = filterMaker.makeFilter();
		bf.addAll(Arrays.asList(testInstances));
		bf.clear();
		
		for (Integer i : testInstances) {
			assertFalse(bf.mightContain(i));
		}
	}

	@Test
	public void retainAllFilter() {
		for (Integer i : testInstances) {
			BloomFilter<Integer> bf1 = filterMaker.makeFilter();
			bf1.addAll(Arrays.asList(testInstances));

			BloomFilter<Integer> bf2 = filterMaker.makeFilter();
			bf2.add(i);

			bf1.retainAll(bf2);

			assertEquals(bf2, bf1);
		}
	}

	@Test
	public void addAllFilter() {
		for (Integer i : testInstances) {
			BloomFilter<Integer> bf1 = filterMaker.makeFilter();
			bf1.addAll(Arrays.asList(testInstances));
			BloomFilter<Integer> bf2 = filterMaker.makeFilter();
			bf2.addAll(Arrays.asList(testInstances));

			BloomFilter<Integer> bf3 = filterMaker.makeFilter();
			bf3.add(i);

			bf1.addAll(bf3);

			assertEquals(bf2, bf1);
		}
	}
}
