package ch.claude_martin.function.tuple;

import static java.util.Objects.requireNonNull;

import java.util.*;

public interface Tuple<T> extends Comparable<T> {

  public static Tuple<?> of(final Object... elements) {
    requireNonNull(elements, "elements");
    final int arity = elements.length;
    switch (arity) {
    case 2:
      return Pair.of(elements[0], elements[1]);
    case 3:
      return Triplet.of(elements[0], elements[1], elements[2]);
    case 4:
      return Quad.of(elements[0], elements[1], elements[2], elements[3]);
    default:
      if (arity < 2)
        throw new IllegalArgumentException("minimum of two elements required");
      if (arity > 4)
        throw new IllegalArgumentException("too many elements");
      throw new IllegalArgumentException();
    }
  }

  @SuppressWarnings({ "rawtypes" })
  public static final Comparator<Comparable> NULLS_FIRST = Comparator.nullsFirst(Comparator
      .naturalOrder());

  public abstract int arity();

  public abstract Object[] toArray();

  /** Returns the n-th projection of this tuple if {@code 0 < n <= arity}, otherwise throws an
   * IndexOutOfBoundsException.
   * 
   * @param index
   *          number of the element to be returned
   * @return {@code ._<index+1>()} */
  public abstract Object get(final int index) throws IndexOutOfBoundsException;

  public default List<Object> toList() {
    return Arrays.asList(toArray());
  }

  public default boolean contains(final Object e) {
    for (final Object o : toArray())
      if (Objects.equals(o, e))
        return true;
    return false;
  }

  public default boolean containsAll(final Collection<?> collection) {
    requireNonNull(collection, "collection");
    for (final Object value : collection)
      if (!contains(value))
        return false;
    return true;
  }

  public default int indexOf(final Object element) {
    final Object[] array = this.toArray();
    for (int i = 0; i < array.length; i++)
      if (Objects.equals(array[i], element))
        return i;
    return -1;
  }

}
