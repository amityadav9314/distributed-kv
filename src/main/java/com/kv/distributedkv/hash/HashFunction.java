package com.kv.distributedkv.hash;

@FunctionalInterface
public interface HashFunction {
    long hash(String key);
}
