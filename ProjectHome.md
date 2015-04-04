This project provides a Java Bloom filter implementation with configurable hashing algorithm.

The core classes are `BloomFilterMaker` and `BloomFilter`.

`BloomFilterMaker` is used to specify the Bloom filter characteristics (expected number of elements, required false positive probability, etc.). It creates `BloomFilter` instances which exhibit the specified characteristics (via `BloomFilterMaker.makeFilter()` ).

`BloomFilter` has a very similar interface to `Set`. The main differences is that the method `BloomFilter.mightContain()` replaces `Set.contains()`.

See the Javadoc for further details.