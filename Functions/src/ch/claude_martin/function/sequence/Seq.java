package ch.claude_martin.function.sequence;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import ch.claude_martin.function.tuple.Pair;

public interface Seq<E> extends List<E> {

  public static final Object       NOTHING  = new Object();
  public static final LinkedSeq<?> EMPTY    = new LinkedSeq<>();
  public static final long         INFINITY = Long.MAX_VALUE;

  @SuppressWarnings("unchecked")
  public static <E> LinkedSeq<E> empty() {
    return (LinkedSeq<E>) EMPTY;
  }

  public static <E> Seq<E> seq(final E head, final Seq<E> tail) {
    return new LinkedSeq<E>(head, tail);
  }

  public static <E> Seq<E> seq(final Seq<E> sequence) {
    requireNonNull(sequence, "sequence");
    if (sequence.isEmpty() || !sequence.isFinite())
      return sequence;
    return new RepeatingSeq<>(sequence);
  }

  /** Collect elements of a finite stream to a sequence. */
  public static <T> Collector<T, ?, Seq<T>> toSeq() {
    final Supplier<List<T>> supplier = ArrayList::new;
    final BiConsumer<List<T>, T> accumulator = (l, t) -> l.add(t);
    final BinaryOperator<List<T>> combiner = (a, b) -> {
      a.addAll(b);
      return a;
    };
    final Function<List<T>, Seq<T>> finisher = LinkedSeq::ofCollection;
    return Collector.of(supplier, accumulator, combiner, finisher);
  }

  /** Extract the first element of a list, which must be non-empty. */
  public abstract E head();

  /** Extract the elements after the head of a list, which must be non-empty. */
  public abstract Seq<E> tail();

  /** returns the last item. */
  public abstract E last();

  /** returns the list without its {@link #last last item}. */
  public abstract LinkedSeq<E> init();

  public abstract long length();

  public default boolean isFinite() {
    return this.length() != INFINITY;
  }

  @Override
  public default boolean isEmpty() {
    return this.length() == 0;
  }

  public default E fold(final BiFunction<? super E, ? super E, ? extends E> accumulator) {
    if (this.isEmpty())
      throw new NoSuchElementException();
    return this.foldLeft(accumulator, null);
  }

  public default <B> B foldLeft(final BiFunction<? super B, ? super E, B> accumulator,
      final B identity) {
    B result = identity;
    for (final E element : this)
      result = accumulator.apply(result, element);
    return result;
  }

  public default <B> B foldRight(final BiFunction<? super E, ? super B, B> accumulator,
      final B identity) {
    if (this.isEmpty())
      return identity;
    return accumulator.apply(this.head(), this.tail().foldRight(accumulator, identity));
  }

  /** Append one or more elements. Creates a new list containing all elements of this and the given
   * elements. */
  public default Seq<E> append(final E e, final E... more) {
    return this.append(new LinkedSeq<>(e, LinkedSeq.of(more)));
  }

  /** Appends a given sequence. Creates a new list containing all elements of this and the given
   * sequence. */
  @SuppressWarnings("unchecked")
  public default Seq<E> append(final Seq<? extends E> list) {
    requireNonNull(list, "list");

    if (list.isEmpty())
      return this;

    Seq<E> result = (Seq<E>) list;
    if (this.isEmpty())
      return result;

    if (this.length() == 1)
      return new LinkedSeq<>(this.head(), result);

    final Object[] elements = this.toArray();
    for (int i = elements.length - 1; i >= 0; i--)
      result = new LinkedSeq<>((E) elements[i], result);
    return result;
  }

  public default Seq<E> take(final int n) {
    if (n < 0 || n > this.length())
      throw new IllegalArgumentException();
    if (n == 0)
      return Seq.empty();
    if (n == this.length())
      return this;
    return new LinkedSeq<>(this.head(), this.tail().take(n - 1));
  }

  public default Seq<E> drop(final int n) {
    if (n < 0 || n > this.length())
      throw new IllegalArgumentException();
    if (n == 0)
      return this;
    if (n == 1)
      return this.tail();
    return this.tail().drop(n - 1);
  }

  public default Seq<E> filter(final Predicate<? super E> predicate) {
    if (this.isEmpty())
      return this;
    if (!this.isFinite())
      throw new RuntimeException(); // infinite sequences must override!
    if (predicate.test(this.head()))
      return new LinkedSeq<>(this.head(), this.tail().filter(predicate));
    return this.tail().filter(predicate);
  }

  /** The partition function takes a predicate a list and returns the pair of lists of elements which
   * do and do not satisfy the predicate, respectively; */
  public default Pair<LinkedSeq<E>, LinkedSeq<E>> partition(final Predicate<? super E> predicate) {
    LinkedSeq<E> a = Seq.empty(), b = Seq.empty();
    for (final E e : this)
      if (predicate.test(e))
        a = new LinkedSeq<>(e, a);
      else
        b = new LinkedSeq<>(e, b);
    return Pair.of(a, b);
  }

  /** Removes duplicate elements from a sequence. */
  public default Seq<E> distinct() {
    return this.stream().distinct().collect(Seq.toSeq());
  }

  public default boolean all(final Predicate<E> predicate) {
    requireNonNull(predicate, "predicate");
    if (this.isEmpty())
      return true;
    return predicate.test(this.head()) && this.tail().all(predicate);
  }

  public default boolean any(final Predicate<E> predicate) {
    requireNonNull(predicate, "predicate");
    if (this.isEmpty())
      return false;
    return predicate.test(this.head()) || this.tail().any(predicate);
  }

  public default <T> Seq<T> map(final Function<? super E, ? extends T> mapper) {
    requireNonNull(mapper, "mapper");
    if (this.isEmpty())
      return Seq.empty();
    return new LinkedSeq<>(mapper.apply(this.head()), this.tail().map(mapper));
  }

  public default Seq<E> sorted() {
    return this.stream().sorted().collect(Seq.toSeq());
  }

  public default Seq<E> sorted(final Comparator<? super E> comparator) {
    return this.stream().sorted(comparator).collect(Seq.toSeq());
  }

  @Override
  public default Object[] toArray() {
    if (this.length() > Integer.MAX_VALUE)
      throw new OutOfMemoryError();
    final Object[] result = new Object[this.size()];
    int i = 0;
    for (Seq<E> e = this; !e.isEmpty(); e = e.tail())
      result[i++] = e.head();
    return result;
  }

  @Override
  public abstract String toString();

  @Override
  public default void forEach(final Consumer<? super E> action) {
    requireNonNull(action, "action");
    Seq<E> remaining = this;
    while (!remaining.isEmpty()) {
      action.accept(remaining.head());
      remaining = remaining.tail();
    }
  }

  /** Use {@link #sorted(Comparator)} instead! */
  @Override
  public default void sort(final Comparator<? super E> comparator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public abstract boolean contains(Object o);

  @Override
  public abstract Iterator<E> iterator();

  @Override
  public abstract <T> T[] toArray(T[] a);

  @Override
  public default int size() {
    final long length = this.length();
    if (length > Integer.MAX_VALUE)
      return Integer.MAX_VALUE;
    return (int) length;
  }

  public abstract Seq<E> reverse();
}