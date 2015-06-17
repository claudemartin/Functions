package ch.claude_martin.function;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import ch.claude_martin.function.tuple.Pair;

/** Sequence implemented as finite, linked list. */
public final class Seq<E> extends AbstractList<E> {
  final E      _head;
  final long   _length;
  final Seq<E> _tail;

  /** Extract the first element of a list, which must be non-empty. */
  public final E head() {
    if (this.isEmpty())
      throw new NoSuchElementException();
    return this._head;
  }

  /** Extract the last element of a list, which must be non-empty. */
  public E last() {
    if (this.isEmpty())
      throw new NoSuchElementException();
    if (this._length == 1)
      return this._head;
    return this.tail().last();
  }

  /** Extract the elements after the head of a list, which must be non-empty. */
  public Seq<E> tail() {
    if (this.isEmpty())
      throw new NoSuchElementException();
    return this._tail;
  }

  /** Return all the elements of a list except the last one. The list must be non-empty. */
  public Seq<E> init() {
    if (this.isEmpty())
      throw new NoSuchElementException();
    // init [x] = []
    if (this._length == 1)
      return empty();
    // init (x:xs) = x : init xs
    return new Seq<>(this.head(), this._tail.init());
  }

  private static final Object NOTHING = new Object();
  public static final Seq<?>  EMPTY   = new Seq<>();

  public static <E> Seq<E> seq(final E head, final Seq<E> tail) {
    return new Seq<>(head, tail);
  }

  @SafeVarargs
  public static <E> Seq<E> of(final E... elements) {
    requireNonNull(elements, "elements");
    Seq<E> result = empty();
    for (int i = elements.length - 1; i >= 0; i--)
      result = new Seq<>(elements[i], result);
    return result;
  }

  public static <E> Seq<E> ofCollection(final Collection<E> elements) {
    requireNonNull(elements, "elements");
    final java.util.List<E> list;
    if (elements instanceof java.util.List)
      list = (java.util.List<E>) elements;
    else
      list = new ArrayList<>(elements);
    final ListIterator<E> itr = list.listIterator(list.size());
    Seq<E> result = empty();
    while (itr.hasPrevious())
      result = new Seq<>(itr.previous(), result);
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <E> Seq<E> empty() {
    return (Seq<E>) EMPTY;
  }

  @SuppressWarnings("unchecked")
  private Seq() {
    this._head = (E) NOTHING;
    this._tail = null;
    this._length = 0;
  }

  public Seq(final E head, final Seq<E> tail) {
    this._head = head;
    this._tail = tail;
    this._length = 1 + tail._length;
  }

  public long length() {
    assert Functions.stream(this, Seq::tail, EMPTY).count() == this._length;
    return this._length;
  }

  @Override
  public boolean isEmpty() {
    return this._length == 0;
  }

  public Seq<E> reverse() {
    if (this._length <= 1)
      return this;
    Seq<E> result = empty();
    Seq<E> remaining = this;
    while (!remaining.isEmpty()) {
      result = new Seq<>(remaining._head, result);
      remaining = remaining._tail;
    }
    return result;
  }

  public E fold(final BiFunction<? super E, ? super E, ? extends E> accumulator) {
    if (this.isEmpty())
      throw new NoSuchElementException();
    return this.foldLeft(accumulator, null);
  }

  public <B> B foldLeft(final BiFunction<? super B, ? super E, B> accumulator, final B identity) {
    B result = identity;
    for (final E element : this)
      result = accumulator.apply(result, element);
    return result;
  }

  public <B> B foldRight(final BiFunction<? super E, ? super B, B> accumulator, final B identity) {
    if (this.isEmpty())
      return identity;
    return accumulator.apply(this._head, this._tail.foldRight(accumulator, identity));
  }

  /** Append one or more elements. Creates a new list containing all elements of this and the given
   * elements. */
  public Seq<E> append(final E e, @SuppressWarnings("unchecked") final E... more) {
    return this.append(seq(e, of(more)));
  }

  /** Appends a given sequence. Creates a new list containing all elements of this and the given
   * sequence. */
  @SuppressWarnings("unchecked")
  public Seq<E> append(final Seq<? extends E> list) {
    requireNonNull(list, "list");

    if (list.isEmpty())
      return this;

    Seq<E> result = (Seq<E>) list;
    if (this.isEmpty())
      return result;

    if (this._length == 1)
      return new Seq<>(this._head, result);

    final Object[] elements = this.toArray();
    for (int i = elements.length - 1; i >= 0; i--)
      result = new Seq<>((E) elements[i], result);
    return result;
  }

  public Seq<E> take(final int n) {
    if (n < 0 || n > this._length)
      throw new IllegalArgumentException();
    if (n == 0)
      return empty();
    if (n == this._length)
      return this;
    return new Seq<>(this._head, this._tail.take(n - 1));
  }

  public Seq<E> drop(final int n) {
    if (n < 0 || n > this._length)
      throw new IllegalArgumentException();
    if (n == 0)
      return this;
    if (n == 1)
      return this._tail;
    return this._tail.drop(n - 1);
  }

  public Seq<E> filter(final Predicate<? super E> predicate) {
    if (this.isEmpty())
      return this;
    if (predicate.test(this._head))
      return new Seq<>(this.head(), this._tail.filter(predicate));
    return this._tail.filter(predicate);
  }

  /** The partition function takes a predicate a list and returns the pair of lists of elements which
   * do and do not satisfy the predicate, respectively; */
  public Pair<Seq<E>, Seq<E>> partition(final Predicate<? super E> predicate) {
    Seq<E> a = empty(), b = empty();
    for (final E e : this)
      if (predicate.test(e))
        a = new Seq<>(e, a);
      else
        b = new Seq<>(e, b);
    return Pair.of(a, b);
  }

  /** Removes duplicate elements from a sequence. */
  public Seq<E> distinct() {
    return this.stream().distinct().collect(toSeq());
  }

  public boolean all(final Predicate<E> predicate) {
    requireNonNull(predicate, "predicate");
    if (this.isEmpty())
      return true;
    return predicate.test(this._head) && this._tail.all(predicate);
  }

  public boolean any(final Predicate<E> predicate) {
    requireNonNull(predicate, "predicate");
    if (this.isEmpty())
      return false;
    return predicate.test(this._head) || this._tail.any(predicate);
  }

  public <T> Seq<T> map(final Function<? super E, ? extends T> mapper) {
    requireNonNull(mapper, "mapper");
    if (this.isEmpty())
      return empty();
    return new Seq<>(mapper.apply(this._head), this._tail.map(mapper));
  }

  public Seq<E> sorted() {
    return this.stream().sorted().collect(toSeq());
  }

  public Seq<E> sorted(final Comparator<? super E> comparator) {
    return this.stream().sorted(comparator).collect(toSeq());
  }

  @Override
  public Object[] toArray() {
    return Functions.stream(this, Seq::tail, empty()).toArray();
  }

  @Override
  public String toString() {
    if (this._length == 0)
      return "[]";
    final StringJoiner joiner = new StringJoiner(",", "[", "]");
    this.forEach(e -> joiner.add(Objects.toString(e)));
    return joiner.toString();
  }

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

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o instanceof Seq) {
      final Seq<?> s = (Seq<?>) o;
      if (this.isEmpty())
        return s.isEmpty();
      return this.length() == s.length() && this.head().equals(s.head())
          && this.tail().equals(s.tail());
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (this.isEmpty())
      return 0;
    return Objects.hashCode(this._head) * 31 + this._tail.hashCode();
  }

  // Collection / List:

  @Override
  public void forEach(final Consumer<? super E> action) {
    requireNonNull(action, "action");
    Seq<E> remaining = this;
    while (!remaining.isEmpty()) {
      action.accept(remaining._head);
      remaining = remaining._tail;
    }
  }

  @Override
  public int size() {
    if (this._length > Integer.MAX_VALUE)
      return Integer.MAX_VALUE;
    return (int) this._length;
  }

  /** Use {@link #sorted(Comparator)} instead! */
  @Override
  public void sort(final Comparator<? super E> comparator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(final Object o) {
    return Functions.find(this, Seq::tail, empty(), e -> Objects.equals(e, o)).isPresent();
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      Seq<E> remaining = Seq.this;

      @Override
      public boolean hasNext() {
        return !this.remaining.isEmpty();
      }

      @Override
      public E next() {
        final E e = this.remaining._head;
        this.remaining = this.remaining.tail();
        return e;
      }

    };
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T[] toArray(final T[] a) {
    if (a.length == this.size()) {
      Seq<E> l = this;
      for (int i = 0; i < a.length; i++) {
        a[i] = (T) l._head;
        l = l._tail;
      }
    }
    return this.toArray(Arrays.copyOf(a, this.size()));
  }

  @Override
  public E get(final int index) {
    if (index < 0 || index >= this._length)
      throw new IndexOutOfBoundsException();
    if (index == 0)
      return this._head;
    return this.tail().get(index - 1);
  }

}