package com.sensoro.beacon.core;

class Pair<K, V> {
	public K key;

	public V value;

	public Pair() {
	}

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Pair)) {
			return false;
		}
		Pair<K, V> pair = (Pair<K, V>) o;
		if (this.key.equals(pair.key) && this.value.equals(pair.value)) {
			return true;
		}
		return false;
	}
}
