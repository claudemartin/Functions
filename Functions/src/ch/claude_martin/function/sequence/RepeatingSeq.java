package ch.claude_martin.function.sequence;

import static java.util.Objects.requireNonNull;

import java.util.AbstractList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/* UNDER CONSTRUCTION! */
final class RepeatingSeq<E> extends AbstractList<E> implements Seq<E> {
  private final int    offset;
  private final long   length;
  private final Seq<E> sequence;

  RepeatingSeq(final Seq<E> sequence) {
    this(sequence, 0, INFINITY);
  }

  RepeatingSeq(final Seq<E> sequence, final int offset, final long length) {
    requireNonNull(sequence, "sequence");
    if (offset < 0 || offset >= sequence.length())
      throw new IllegalArgumentException();
    if (sequence.isEmpty())
      throw new IllegalArgumentException();
    // TODO : If sequence is already RepeatingSeq then this could use it's inner sequence!
    this.sequence = sequence;
    this.offset = offset;
    this.length = length;
  }

  @Override
  public boolean isEmpty() {
    return this.length == 0;
  }

  @Override
  public int size() {
    return Seq.super.size();
  }

  @Override
  public E get(final int index) {
    if (index < 0 || index >= this.length)
      throw new IndexOutOfBoundsException();
    return this.sequence.get(this.offset + index % this.sequence.size());
  }

  @Override
  public int indexOf(final Object o) {
    int i = this.sequence.drop(this.offset).indexOf(o);
    if (i != -1 || this.offset == 0)
      return i;
    i = this.sequence.indexOf(o);
    if (i == -1)
      return i;
    return this.sequence.size() - this.offset + i;
  }

  @Override
  public int lastIndexOf(final Object o) {
    if (!this.isFinite())
      throw new UnsupportedOperationException("no last index for infinite sequence");
    // TODO
    return -999;
  }

  @Override
  public E head() {
    return this.sequence.drop(this.offset).head();
  }

  @Override
  public Seq<E> tail() {
    return this.sequence.drop(this.offset).tail();
  }

  @Override
  public E last() {
    throw new NoSuchElementException();
  }

  @Override
  public LinkedSeq<E> init() {
    throw new NoSuchElementException();
  }

  @Override
  public long length() {
    return this.length;
  }

  @Override
  public Seq<E> sorted(final Comparator<? super E> comparator) {
    throw new OutOfMemoryError();
  }

  @Override
  public boolean contains(final Object o) {
    return this.sequence.contains(o);
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      Seq<E> next = RepeatingSeq.this.sequence.drop(RepeatingSeq.this.offset);

      @Override
      public boolean hasNext() {
        return true;
      }

      @Override
      public E next() {
        final E result = this.next.head();
        this.next = this.next.tail();
        if (this.next.isEmpty())
          this.next = RepeatingSeq.this.sequence;
        return result;
      }
    };
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    throw new OutOfMemoryError();
  }

  @Override
  public Seq<E> reverse() {
    return new RepeatingSeq<>(this.sequence.reverse(), 0/* TODO */, this.length);
  }

  @Override
  public Seq<E> take(final int n) {
    return new RepeatingSeq<>(this.sequence.reverse(), this.offset, n);
  }

  @Override
  public Seq<E> drop(final int n) {
    // TODO Auto-generated method stub
    return Seq.super.drop(n);
  }

}
