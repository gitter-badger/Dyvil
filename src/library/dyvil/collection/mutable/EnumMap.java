package dyvil.collection.mutable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.Type;
import dyvil.lang.literal.ClassConvertible;
import dyvil.lang.literal.TypeConvertible;

import dyvil.annotation.sealed;
import dyvil.collection.ImmutableMap;
import dyvil.collection.MutableMap;
import dyvil.collection.impl.AbstractEnumMap;

@ClassConvertible
@TypeConvertible
public class EnumMap<K extends Enum<K>, V> extends AbstractEnumMap<K, V> implements MutableMap<K, V>
{
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(Type<K> type)
	{
		return new EnumMap(type);
	}
	
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(Class<K> type)
	{
		return new EnumMap(type);
	}
	
	public @sealed EnumMap(Class<K> type, K[] keys, V[] values, int size)
	{
		super(type, keys, values, size);
	}
	
	public EnumMap(Class<K> type)
	{
		super(type);
	}
	
	public EnumMap(Type<K> type)
	{
		super(type.getTheClass());
	}
	
	@Override
	protected void removeAt(int index)
	{
		this.values[index] = null;
		this.size--;
	}
	
	@Override
	public void clear()
	{
		this.size = 0;
		Arrays.fill(this.values, null);
	}
	
	@Override
	public V put(K key, V value)
	{
		if (!this.checkType(key))
		{
			return null;
		}
		
		int index = index(key);
		V oldValue = (V) this.values[index];
		this.values[index] = value;
		if (oldValue == null)
		{
			this.size++;
		}
		return oldValue;
	}
	
	@Override
	public V removeKey(Object key)
	{
		if (!this.checkType(key))
		{
			return null;
		}
		
		int index = index(key);
		V oldValue = (V) this.values[index];
		
		if (oldValue != null)
		{
			this.size--;
			this.values[index] = null;
			return oldValue;
		}
		return null;
	}
	
	@Override
	public boolean remove(Object key, Object value)
	{
		if (!this.checkType(key))
		{
			return false;
		}
		
		int index = index(key);
		Object oldValue = this.values[index];
		if (oldValue != null && oldValue.equals(value))
		{
			this.size--;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removeValue(Object value)
	{
		boolean removed = false;
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			if (Objects.equals(this.values[i], value))
			{
				this.size--;
				this.values[i] = null;
				removed = true;
			}
		}
		return removed;
	}
	
	@Override
	public void map(BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			V v = (V) this.values[i];
			if (v != null)
			{
				this.values[i] = mapper.apply(this.keys[i], v);
			}
		}
	}
	
	@Override
	public void filter(BiPredicate<? super K, ? super V> condition)
	{
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			V v = (V) this.values[i];
			if (v != null && !condition.test(this.keys[i], v))
			{
				this.values[i] = null;
				this.size--;
			}
		}
	}
	
	@Override
	public MutableMap<K, V> copy()
	{
		return new EnumMap(this.type, this.keys, this.values.clone(), this.size);
	}
	
	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new EnumMap(this.type);
	}
	
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return new dyvil.collection.immutable.EnumMap(this.type, this.keys, this.values.clone(), this.size);
	}
}
