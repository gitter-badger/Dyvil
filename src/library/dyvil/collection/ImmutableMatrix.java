package dyvil.collection;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import dyvil.lang.*;
import dyvil.lang.literal.ArrayConvertible;

import dyvil.annotation.Covariant;
import dyvil.collection.immutable.ArrayMatrix;
import dyvil.tuple.Tuple2;
import dyvil.util.Immutable;
import dyvil.util.ImmutableException;

@ArrayConvertible
public interface ImmutableMatrix<@Covariant E> extends Matrix<E>, Immutable
{
	public static <E> ImmutableMatrix<E> apply(E[]... elements)
	{
		return new ArrayMatrix(elements);
	}
	
	public static <E> ImmutableMatrix<E> create(int rows, int columns)
	{
		return new ArrayMatrix<E>(rows, columns);
	}
	
	// Accessors
	
	@Override
	public int rows();
	
	@Override
	public int columns();
	
	@Override
	public boolean contains(Object element);
	
	@Override
	public E subscript(int row, int column);
	
	@Override
	public E get(int row, int column);
	
	// Sub-view Operations
	
	@Override
	public ImmutableMatrix<E> subMatrix(int row, int rows, int column, int columns);
	
	@Override
	public ImmutableList<E> row(int row);
	
	@Override
	public ImmutableList<E> column(int column);
	
	// Non-mutating Operations
	
	@Override
	public ImmutableList<E> flatten();
	
	@Override
	public ImmutableMatrix<E> transposed();
	
	@Override
	public <R> ImmutableMatrix<R> mapped(Function<? super E, ? extends R> mapper);
	
	// Mutating Operations
	
	@Override
	public default void resize(int rows, int columns)
	{
		throw new ImmutableException("resize() on Immutable Matrix");
	}
	
	@Override
	public default void addRow(List<E> row)
	{
		throw new ImmutableException("addRow() on Immutable Matrix");
	}
	
	@Override
	public default void addColumn(List<E> column)
	{
		throw new ImmutableException("addColumn() on Immutable Matrix");
	}
	
	@Override
	public default void insertRow(int index, List<E> row)
	{
		throw new ImmutableException("insertRow() on Immutable Matrix");
	}
	
	@Override
	public default void insertColumn(int index, List<E> column)
	{
		throw new ImmutableException("insertColumn() on Immutable Matrix");
	}
	
	@Override
	public default void subscript_$eq(int row, int column, E element)
	{
		throw new ImmutableException("update() on Immutable Matrix");
	}
	
	@Override
	public default E set(int row, int column, E element)
	{
		throw new ImmutableException("set() on Immutable Matrix");
	}
	
	@Override
	public default void removeRow(int index)
	{
		throw new ImmutableException("removeRow() on Immutable Matrix");
	}
	
	@Override
	public default void removeColumn(int column)
	{
		throw new ImmutableException("removeColumn() on Immutable Matrix");
	}
	
	@Override
	public default void clear()
	{
		throw new ImmutableException("clear() on Immutable Matrix");
	}
	
	@Override
	public default void transpose()
	{
		throw new ImmutableException("transpose() on Immutable Matrix");
	}
	
	@Override
	public default void map(UnaryOperator<E> mapper)
	{
		throw new ImmutableException("map() on Immutable Matrix");
	}
	
	// Search Operations
	
	@Override
	public int rowOf(Object element);
	
	@Override
	public int columnOf(Object element);
	
	@Override
	public Tuple2<Int, Int> cellOf(Object element);
	
	// toArray
	
	@Override
	public void rowArray(int row, Object[] store);
	
	@Override
	public void columnArray(int column, Object[] store);
	
	@Override
	public void toArray(Object[][] store);
	
	@Override
	public void toCellArray(Object[] store);
	
	// Copying
	
	@Override
	public ImmutableMatrix<E> copy();
	
	@Override
	public MutableMatrix<E> mutable();
	
	@Override
	public default MutableMatrix<E> mutableCopy()
	{
		return this.mutable();
	}
	
	@Override
	public default ImmutableMatrix<E> immutable()
	{
		return this;
	}
	
	@Override
	public default ImmutableMatrix<E> immutableCopy()
	{
		return this.copy();
	}
}
