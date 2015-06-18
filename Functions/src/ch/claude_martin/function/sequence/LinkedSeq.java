package ch.claude_martin.function.sequence;

import static java.util.Objects.requireNonNull;

import java.util.*;

import ch.claude_martin.function.Functions;

/** Sequence implemented as finite, linked list. */
final class LinkedSeq<E> extends AbstractList<E> implements Seq<E> {
  final E      _head;
  final long   _length;
  final Seq<E> _tail;

  @Override
  public final E head() {
    if (this.isEmpty())
      throw new NoSuchElementException();
    return this._head;
  }

  @Override
  public E last() {
    if (!this.isFinite() || !this.isFinite())
      throw new NoSuchElementException();
    if (this._length == 1)
      return this._head;
    return this.tail().last();
  }


  @Override
  public Seq<E> tail() {
    if (this.isEmpty())
      throw new NoSuchElementException();
    return this._tail;
  }

  @Override
  public LinkedSeq<E> init() {
    if (this.isEmpty() || !this.isFinite())
      throw new NoSuchElementException();
    // init [x] = []
    if (this._length == 1)
      return Seq.empty();
    // init (x:xs) = x : init xs
    return new LinkedSeq<>(this.head(), this._tail.init());
  }


  @SafeVarargs
  public static <E> LinkedSeq<E> of(final E... elements) {
    requireNonNull(elements, "elements");
    LinkedSeq<E> result = Seq.empty();
    for (int i = elements.length - 1; i >= 0; i--)
      result = new LinkedSeq<>(elements[i], result);
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
    LinkedSeq<E> result = Seq.empty();
    while (itr.hasPrevious())
      result = new LinkedSeq<>(itr.previous(), result);
    return result;
  }


  @SuppressWarnings("unchecked")
  LinkedSeq() {
    this._head = (E) NOTHING;
    this._tail = null;
    this._length = 0;
  }

  LinkedSeq(final E head, final Seq<E> tail) {
    this._head = head;
    this._tail = tail;
    if (tail.isFinite())
      this._length = 1 + tail.length();
    else
      this._length = Long.MAX_VALUE;
  }


  @Override
  public long length() {
    assert Functions.stream((Seq<E>) this, Seq::tail, EMPTY).count() == this._length;
    return this._length;
  }

  @Override
  public int size() {
    return Seq.super.size();
  }

  @Override
  public LinkedSeq<E> reverse() {
    if (this._length <= 1)
      return this;
    LinkedSeq<E> result = Seq.empty();
    Seq<E> remaining = this;
    while (!remaining.isEmpty()) {
      result = new LinkedSeq<>(remaining.head(), result);
      remaining = remaining.tail();
    }
    return result;
  }


  @Override
  public String toString() {
    if (this._length == 0)
      return "[]";
    final StringJoiner joiner = new StringJoiner(",", "[", "]");
    this.forEach(e -> joiner.add(Objects.toString(e)));
    return joiner.toString();
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

  // Collection / List:



  @Override
  public void sort(final Comparator<? super E> comparator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(final Object o) {
    return Functions.find((Seq<E>) this, Seq::tail, Seq.empty(), e -> Objects.equals(e, o))
        .isPresent();
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      Seq<E> remaining = LinkedSeq.this;

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
  public <T> T[] toArray(final T[] a) {
    if (a.length == this.size()) {
      Seq<E> l = this;
      for (int i = 0; i < a.length; i++) {
        a[i] = (T) l.head();
        l = l.tail();
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