package ch.claude_martin.function.sequence;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
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

  public static Seq<Integer> ofInts(final int... elements) {
    requireNonNull(elements, "elements");
    final Integer[] ints = new Integer[elements.length];
    for (int x = 0; x < elements.length; x++)
      ints[x] = elements[x];
    return new ArraySeq<>(ints, 0);
  }

  public static Seq<Long> ofLongs(final long... elements) {
    requireNonNull(elements, "elements");
    final Long[] longs = new Long[elements.length];
    for (int x = 0; x < elements.length; x++)
      longs[x] = elements[x];
    return new ArraySeq<>(longs, 0);
  }

  public static Seq<Double> ofDoubles(final double... elements) {
    requireNonNull(elements, "elements");
    final Double[] doubles = new Double[elements.length];
    for (int x = 0; x < elements.length; x++)
      doubles[x] = elements[x];
    return new ArraySeq<>(doubles, 0);
  }

  /** Creates a clone of the given elements. */
  @SafeVarargs
  public static <E> Seq<E> of(final E... elements) {
    requireNonNull(elements, "elements");
    if (elements.length == 0)
      return empty();
    requireNonNull(elements, "elements");
    return new ArraySeq<>(elements.clone(), 0);
  }

  public static <E> Seq<E> ofCollection(final Collection<E> elements) {
    requireNonNull(elements, "elements");
    if (elements.isEmpty())
      return empty();
    if (elements instanceof Seq)
      return (Seq<E>) elements;
    // collection could be mutable, so a copy must be created.
    final java.util.List<E> list;
    if (elements instanceof java.util.List)
      list = (java.util.List<E>) elements;
    else
      list = new ArrayList<>(elements);
    final ListIterator<E> itr = list.listIterator(list.size());
    LinkedSeq<E> result = Seq.empty();
    while (itr.hasPrevious())
      result = new LinkedSeq<>(itr.previous(), result);
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T, A extends T, B extends T> Seq<T> concat(final Seq<A> s1, final Seq<B> s2) {
    requireNonNull(s1, "s1");
    requireNonNull(s2, "s2");
    return ((Seq<T>) s1).append(s2);
  }

  public static <E> Seq<E> generate(final Callable<E> callable) {
    return new LazySeq<>(callable);
  }

  public static <E> Seq<E> generate(final Predicate<Consumer<E>> generator) {
    return new LazySeq<>(generator);
  }

  public static <E> Seq<E> iterate(final E seed, final UnaryOperator<E> f) {
    final AtomicReference<E> i = new AtomicReference<>(seed);
    return generate(() -> {
      return i.getAndUpdate(f);
    });
  }

  public static Seq<Integer> iterate(final int seed, final IntUnaryOperator f) {
    final AtomicInteger i = new AtomicInteger(seed);
    return generate(() -> {
      return i.getAndUpdate(f);
    });
  }

  public static Seq<Long> iterate(final long seed, final LongUnaryOperator f) {
    final AtomicLong i = new AtomicLong(seed);
    return generate(() -> {
      return i.getAndUpdate(f);
    });
  }

  public static Seq<Integer> range(final int start, final int end) {
    if (end < start)
      throw new IllegalArgumentException();
    return iterate(start, (IntUnaryOperator) i -> {
      if (i == end)
        throw new RuntimeException();
      return 1 + i;
    });
  }

  public static Seq<Integer> rangeClosed(final int first, final int last) {
    return range(first, last + 1);
  }

  public static Seq<Long> range(final long start, final long end) {
    return iterate(start, (LongUnaryOperator) i -> {
      if (i == end)
        throw new RuntimeException();
      return 1 + i;
    });
  }

  public static Seq<Long> rangeClosed(final long first, final long last) {
    return range(first, last + 1);
  }

  @SuppressWarnings("unchecked")
  public static <E> Seq<E> seq(final E head, final Seq<? extends E> tail) {
    return new LinkedSeq<>(head, (Seq<E>) tail);
  }

  /** Collect elements of a finite stream to a sequence. The collector will create a linked sequence
   * of all elements. */
  public static <T> Collector<T, ?, Seq<T>> toSeq() {
    final Supplier<List<T>> supplier = ArrayList::new;
    final BiConsumer<List<T>, T> accumulator = (l, t) -> l.add(t);
    final BinaryOperator<List<T>> combiner = (a, b) -> {
      a.addAll(b);
      return a;
    };
    final Function<List<T>, Seq<T>> finisher = Seq::ofCollection;
    return Collector.of(supplier, accumulator, combiner, finisher);
  }

  /** Extract the first element of a list, which must be non-empty. */
  public abstract E head();

  /** Extract the elements after the head of a list, which must be non-empty. */
  public abstract Seq<E> tail();

  /** returns the last item. */
  public abstract E last();

  /** returns the list without its {@link #last last item}. */
  public abstract Seq<E> init();

  /** length of the sequence. */
  public abstract long length();

  /** Returns true if the number of elements is known and finite. */
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
  public default Seq<E> append(final E e, @SuppressWarnings("unchecked") final E... more) {
    return this.append(new LinkedSeq<>(e, Seq.of(more)));
  }

  /** Appends a given sequence. Creates a new list containing all elements of this and the given
   * sequence. */
  @SuppressWarnings("unchecked")
  public default Seq<E> append(final Seq<? extends E> list) {
    requireNonNull(list, "list");

    if (list.isEmpty() || !this.isFinite())
      return this;

    // final Seq<E> result = (Seq<E>) list;
    if (this.isEmpty())
      return (Seq<E>) list;

    if (this.length() == 1)
      return new LinkedSeq<>(this.head(), (Seq<E>) list);
    final AtomicReference<Pair<Seq<E>, Boolean>> ref = new AtomicReference<>(Pair.of(this, true));

    return new LazySeq<>(() -> {
      return ref.getAndUpdate(p -> {
        final Seq<E> tail = p._1().tail();
        final Boolean isFirst = p._2();
        if (tail.isEmpty()) {
          if (isFirst)
            return Pair.of((Seq<E>) list, false);
          return Pair.of(tail, false);
        }
        return Pair.of(tail, isFirst);
      })._1().head();
    });
  }

  @Override
  public default E get(final int index) {
    if (index < 0 || this.isEmpty())
      throw new IndexOutOfBoundsException();
    if (index == 0)
      return this.head();
    return this.tail().get(index - 1);
  }

  public default Seq<E> take(final long n) {
    if (this.isEmpty() || n <= 0)
      return Seq.empty();
    if (n == INFINITY)
      return this;
    if (this instanceof LinkedSeq || this instanceof ArraySeq)
      // These are finite and non-lazy
      if (n >= this.length())
        return this;

    final AtomicReference<Pair<Long, Seq<E>>> i = new AtomicReference<>(Pair.of(0L, Seq.this));
    return generate(() -> {
      final Pair<Long, Seq<E>> p = i.getAndUpdate(o -> Pair.of(o._1() + 1L, o._2().tail()));
      if (p._1() == n)
        throw new NoSuchElementException();
      return p._2().head();
    });
  }

  public default Seq<E> drop(final long n) {
    if (n > this.length())
      return Seq.empty();
    if (n <= 0)
      return this;
    if (n == 1)
      return this.tail();
    return this.tail().drop(n - 1);
  }

  public default Seq<E> repeat() {
    if (this.isEmpty() || !this.isFinite())
      return this;
    return new RepeatingSeq<>(this);
  }

  public default Seq<E> repeat(final int offset, final long length) {
    if (length == 0 || this.isEmpty())
      return Seq.empty();
    if (!this.isFinite())
      return this.drop(offset).take(length);
    return new RepeatingSeq<>(this, offset, length);
  }

  public default Seq<E> filter(final Predicate<? super E> predicate) {
    requireNonNull(predicate, "predicate");
    if (this.isEmpty())
      return Seq.empty();
    final AtomicReference<Seq<E>> i = new AtomicReference<>(Seq.seq(null, Seq.this));
    return generate(() -> {
      return i.updateAndGet(s -> {
        do
          s = s.tail();
        while (!predicate.test(s.head()));
        return s;
      }).head();
    });
  }

  /** The partition function takes a predicate a list and returns the pair of lists of elements which
   * do and do not satisfy the predicate, respectively; */
  public default Pair<Seq<E>, Seq<E>> partition(final Predicate<? super E> predicate) {
    Seq<E> a = Seq.empty(), b = Seq.empty();
    for (final E e : this)
      if (predicate.test(e))
        a = new LinkedSeq<>(e, a);
      else
        b = new LinkedSeq<>(e, b);
    return Pair.of(a, b);
  }

  /** Removes duplicate elements from a sequence. */
  public default Seq<E> distinct() {
    if (isEmpty())
      return empty();
    final Set<E> set = new HashSet<>();
    final AtomicReference<Seq<E>> ref = new AtomicReference<>(//
        Seq.seq(null, Seq.this));
    return generate(() -> {
      return ref.updateAndGet(s -> {
        do
          s = s.tail();
        while (!set.contains(s.head()));
        return s;
      }).head();
    });
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
    final AtomicReference<Seq<E>> i = new AtomicReference<>(Seq.this);
    return generate(() -> mapper.apply(i.getAndUpdate(s -> s.tail()).head()));
  }

  public default Seq<E> sorted() {
    return this.stream().sorted().collect(Seq.toSeq());
  }

  public default Seq<E> sorted(final Comparator<? super E> comparator) {
    return this.stream().sorted(comparator).collect(Seq.toSeq());
  }

  @Override
  public default Object[] toArray() {
    try {
      if (!this.isFinite() || this.length() > Integer.MAX_VALUE - 8)
        throw new OutOfMemoryError();
    } catch (final StackOverflowError e) {
      throw new OutOfMemoryError();
    }
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
  public default boolean contains(final Object o) {
    if (this.isEmpty())
      return false;
    return Objects.equals(this.head(), o) || this.tail().contains(o);
  }

  @Override
  public default Iterator<E> iterator() {

    return new Iterator<E>() {
      Seq<E> remaining = Seq.this;

      @Override
      public boolean hasNext() {
        return !this.remaining.isEmpty();
      }

      @Override
      public E next() {
        final E e = this.remaining.head();
        this.remaining = this.remaining.tail();
        return e;
      }

    };

  }

  @SuppressWarnings("unchecked")
  @Override
  public default <T> T[] toArray(final T[] a) {
    if (this.length() > Integer.MAX_VALUE)
      throw new OutOfMemoryError();
    if (a.length == this.size()) {
      Seq<E> l = this;
      for (int i = 0; i < a.length; i++) {
        a[i] = (T) l.head();
        l = l.tail();
      }
      return a;
    }
    return this.toArray(Arrays.copyOf(a, this.size()));
  }

  @Override
  public default int size() {
    final long length = this.length();
    if (length > Integer.MAX_VALUE)
      return Integer.MAX_VALUE;
    return (int) length;
  }

  public abstract Seq<E> reverse();
}