package ch.claude_martin.function.tuple;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public interface Tuple<T> extends Comparable<T> {

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


}
