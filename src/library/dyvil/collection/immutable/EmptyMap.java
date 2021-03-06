package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.lang.literal.NilConvertible;

import dyvil.annotation.object;
import dyvil.collection.*;
import dyvil.collection.iterator.EmptyIterator;

@NilConvertible
public @object class EmptyMap<K, V> implements ImmutableMap<K, V>
{
	public static final EmptyMap	instance	= new EmptyMap();
	
	public static <K, V> EmptyMap<K, V> apply()
	{
		return instance;
	}
	
	private EmptyMap()
	{
	}
	
	@Override
	public int size()
	{
		return 0;
	}
	
	@Override
	public boolean isEmpty()
	{
		return true;
	}
	
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return EmptyIterator.apply();
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return EmptyIterator.apply();
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return EmptyIterator.apply();
	}
	
	@Override
	public void forEach(Consumer<? super Entry<K, V>> action)
	{
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		return false;
	}
	
	@Override
	public boolean contains(Object key, Object value)
	{
		return false;
	}
	
	@Override
	public boolean containsValue(Object value)
	{
		return false;
	}
	
	@Override
	public V get(Object key)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		return ImmutableMap.apply(key, value);
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		return (ImmutableMap<K, V>) map.immutable();
	}
	
	@Override
	public ImmutableMap<K, V> $minus$at(Object key)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Collection<?> keys)
	{
		return this;
	}
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		return (ImmutableMap<K, U>) this;
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends U, ? extends R>> mapper)
	{
		return (ImmutableMap<U, R>) this;
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends U, ? extends R>>> mapper)
	{
		return (ImmutableMap<U, R>) this;
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return this;
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return MutableMap.apply();
	}
	
	@Override
	public String toString()
	{
		return "[]";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return Map.mapEquals(this, obj);
	}
	
	@Override
	public int hashCode()
	{
		return Map.mapHashCode(this);
	}
}
