package ch.claude_martin.function.sequence;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/* UNDER CONSTRUCTION! */
final class RepeatingSeq<E> extends AbstractSeq<E> {
  final int    offset;
  final long   length;
  final Seq<E> sequence;

  RepeatingSeq(final Seq<E> sequence) {
    this(sequence, 0, INFINITY);
  }

  RepeatingSeq(final Seq<E> sequence, final int offset, final long length) {
    requireNonNull(sequence, "sequence");
    if (offset < 0 || offset >= sequence.length())
      throw new IllegalArgumentException();
    if (length <= 0 || sequence.isEmpty())
      throw new IllegalArgumentException();
    // TODO : If sequence is already RepeatingSeq then it's inner sequence could be used!
    this.sequence = sequence;
    this.offset = offset;
    this.length = length;
  }

  @Override
  public boolean isEmpty() {
    return this.length == 0;
  }

  @Override
  public E get(final int index) {
    if (index < 0 || index >= this.length)
      throw new IndexOutOfBoundsException();
    return this.sequence.get((this.offset + index) % this.sequence.size());
  }

  @Override
  public int indexOf(final Object o) {
    int i = this.sequence.drop(this.offset).indexOf(o);
    if (i != -1 || this.offset == 0)
      return i;
    i = this.sequence.indexOf(o);
    if (i == -1)
      return i;
    i = this.sequence.size() - this.offset + i;
    if (i >= this.length)
      return -1;
    return i;
  }

  @Override
  public int lastIndexOf(final Object o) {
    if (!this.isFinite())
      throw new UnsupportedOperationException("no last index in infinite sequence");
    // TODO what if length > Integer.MAX_VALUE ???
    final int revIndex = this.reverse().indexOf(o);
    if (revIndex == -1)
      return -1;
    final int index = this.size() - 1 - revIndex;
    return index < 0 ? -1 : index;
  }

  @Override
  public E head() {
    return this.sequence.drop(this.offset).head();
  }

  @Override
  public Seq<E> tail() {
    int newOffset = this.offset + 1;
    if (newOffset == this.sequence.size())
      newOffset = 0;
    if (this.length == 1)
      return Seq.empty();
    return new RepeatingSeq<>(this.sequence, newOffset, this.length - 1);
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
    if (this.length >= this.sequence.length())
      return this.sequence.contains(o);
    return super.contains(o);
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      Seq<E> next = RepeatingSeq.this.sequence.drop(RepeatingSeq.this.offset);
      int    pos  = 0;
      @Override
      public boolean hasNext() {
        return this.pos < RepeatingSeq.this.length;
      }

      @Override
      public E next() {
        if (!this.hasNext())
          throw new NoSuchElementException();
        final E result = this.next.head();
        this.next = this.next.tail();
        if (this.next.isEmpty())
          this.next = RepeatingSeq.this.sequence;
        this.pos++;
        return result;
      }
    };
  }

  @Override
  public Seq<E> reverse() {
    final long seqLen = this.sequence.length();
    // final int newOffset = (int) ((this.length - (seqLen - this.offset)) % seqLen);

    final long a = seqLen - this.offset;
    final long b = this.length - a;
    final long c = b % seqLen;
    final int d = (int) (seqLen - c);
    return new RepeatingSeq<>(this.sequence.reverse(), d, this.length);
  }

  @Override
  public Seq<E> take(final int n) {
    return new RepeatingSeq<>(this.sequence, this.offset, n);
  }

  @Override
  public Seq<E> drop(final int n) {
    final long newLength = this.length - n;
    if (newLength == 0)
      return Seq.empty();
    if (newLength < 0)
      throw new IllegalArgumentException();
    final int newOffset = (this.offset + n) % this.sequence.size();
    return new RepeatingSeq<>(this.sequence, newOffset, newLength);
  }

}
