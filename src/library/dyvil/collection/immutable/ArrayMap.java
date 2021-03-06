package dyvil.collection.immutable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractArrayMap;
import dyvil.util.ImmutableException;

public class ArrayMap<K, V> extends AbstractArrayMap<K, V> implements ImmutableMap<K, V>
{
	public ArrayMap(K[] keys, V[] values)
	{
		super(keys, values);
	}
	
	public ArrayMap(K[] keys, V[] values, int size)
	{
		super(keys, values);
	}
	
	public ArrayMap(Object[] keys, Object[] values, boolean trusted)
	{
		super(keys, values, keys.length, trusted);
	}
	
	public ArrayMap(Object[] keys, Object[] values, int size, boolean trusted)
	{
		super(keys, values, size, trusted);
	}
	
	public ArrayMap(Map<K, V> map)
	{
		super(map);
	}
	
	@Override
	protected void removeAt(int index)
	{
		throw new ImmutableException("Iterator.remove() on Immutable Map");
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(key, this.keys[i]))
			{
				Object[] keys = this.keys.clone();
				Object[] values = this.values.clone();
				values[i] = value;
				return new ArrayMap(keys, values, this.size, true);
			}
		}
		
		int len = this.size + 1;
		Object[] keys = new Object[len];
		Object[] values = new Object[len];
		System.arraycopy(this.keys, 0, keys, 0, this.size);
		System.arraycopy(this.values, 0, values, 0, this.size);
		keys[this.size] = key;
		values[this.size] = value;
		return new ArrayMap(keys, values, len, true);
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		int index = this.size;
		int maxLength = index + map.size();
		Object[] keys = new Object[maxLength];
		Object[] values = new Object[maxLength];
		System.arraycopy(this.keys, 0, keys, 0, index);
		System.arraycopy(this.values, 0, values, 0, index);
		
		outer:
		for (Entry<? extends K, ? extends V> entry : map)
		{
			K key = entry.getKey();
			for (int i = 0; i < this.size; i++)
			{
				if (Objects.equals(keys[i], key))
				{
					values[i] = entry.getValue();
					continue outer;
				}
			}
			keys[index] = entry.getKey();
			values[index++] = entry.getValue();
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$at(Object key)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = (K) this.keys[i];
			if (Objects.equals(key, k))
			{
				continue;
			}
			
			keys[index] = k;
			values[index++] = this.values[i];
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = (K) this.keys[i];
			if (Objects.equals(key, k))
			{
				continue;
			}
			V v = (V) this.values[i];
			if (Objects.equals(value, v))
			{
				continue;
			}
			keys[index] = k;
			values[index++] = v;
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			V v = (V) this.values[i];
			if (Objects.equals(value, v))
			{
				continue;
			}
			
			keys[index] = this.keys[i];
			values[index++] = v;
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = (K) this.keys[i];
			V v = (V) this.values[i];
			if (map.contains(k, v))
			{
				continue;
			}
			
			keys[index] = k;
			values[index++] = v;
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Collection<?> collection)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = (K) this.keys[i];
			if (collection.contains(k))
			{
				continue;
			}
			
			keys[index] = k;
			values[index++] = this.values[i];
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		System.arraycopy(this.keys, 0, keys, 0, this.size);
		for (int i = 0; i < this.size; i++)
		{
			values[i] = mapper.apply((K) this.keys[i], (V) this.values[i]);
		}
		return new ArrayMap(keys, values, this.size, true);
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends U, ? extends R>> mapper)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int newSize = 0;
		outer:
		for (int i = 0; i < this.size; i++)
		{
			Entry<? extends U, ? extends R> entry = mapper.apply((K) this.keys[i], (V) this.values[i]);
			if (entry == null)
			{
				continue;
			}
			
			U key = entry.getKey();
			R value = entry.getValue();
			
			for (int j = 0; j < i; j++)
			{
				if (Objects.equals(keys[j], key))
				{
					values[j] = value;
					continue outer;
				}
			}
			
			keys[newSize] = key;
			values[newSize++] = value;
		}
		return null;
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends U, ? extends R>>> mapper)
	{
		dyvil.collection.mutable.ArrayMap<U, R> mutable = new dyvil.collection.mutable.ArrayMap(this.keys, this.values, this.size);
		mutable.flatMap((BiFunction) mapper);
		return mutable.trustedImmutable();
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = (K) this.keys[i];
			V v = (V) this.values[i];
			if (!condition.test(k, v))
			{
				continue;
			}
			
			keys[index] = k;
			values[index] = this.values[i];
			index++;
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return new ArrayMap(this.keys, this.values, this.size);
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return new dyvil.collection.mutable.ArrayMap(this.keys, this.values, this.size);
	}
}
