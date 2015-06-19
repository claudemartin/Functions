package ch.claude_martin.function.sequence;

import java.util.AbstractList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AbstractSeq<E> extends AbstractList<E> implements Seq<E> {

  public AbstractSeq() {
    super();
  }

  @Override
  public final E last() {
    if (this.isEmpty() || !this.isFinite())
      throw new NoSuchElementException();
    if (this.length() == 1)
      return this.head();
    return this.tail().last();
  }

  @Override
  public final LinkedSeq<E> init() {
    if (this.isEmpty() || !this.isFinite())
      throw new NoSuchElementException();
    // init [x] = []
    if (this.length() == 1)
      return Seq.empty();
    // init (x:xs) = x : init xs
    return new LinkedSeq<>(this.head(), this.tail().init());
  }

  @Override
  public final int size() {
    return Seq.super.size();
  }

  @Override
  public boolean isEmpty() {
    return Seq.super.isEmpty();
  }

  @Override
  public Seq<E> reverse() {
    if (this.length() <= 1)
      return this;
    Seq<E> result = Seq.empty();
    Seq<E> remaining = this;
    while (!remaining.isEmpty()) {
      result = new LinkedSeq<>(remaining.head(), result);
      remaining = remaining.tail();
    }
    return result;
  }

  @Override
  public final String toString() {
    return super.toString(); // AbstractCollection does a good job.
  }

  @Override
  public final boolean equals(final Object o) {
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
  public final void sort(final Comparator<? super E> comparator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(final Object o) {
    return Seq.super.contains(o);
  }

  @Override
  public Iterator<E> iterator() {
    return Seq.super.iterator();
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return Seq.super.toArray(a);
  }

  @Override
  public E get(final int index) {
    return Seq.super.get(index);
  }

}