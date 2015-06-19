package ch.claude_martin.function.sequence;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

public final class ArraySeq<E> extends AbstractSeq<E> {
  private final E[] array;
  private final int offset;

  ArraySeq(final E[] array, final int offset) {
    super();
    this.array = array;
    this.offset = offset;
  }

  @Override
  public E head() {
    return this.array[this.offset];
  }

  @Override
  public Seq<E> tail() {
    final int newOffset = this.offset + 1;
    if (newOffset == this.array.length)
      return Seq.empty();
    return new ArraySeq<>(this.array, newOffset);
  }

  @Override
  public long length() {
    return this.array.length - this.offset;
  }

  @Override
  public Iterator<E> iterator() {
    return Arrays.asList(this.array).iterator();
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Seq<E> sorted() {
    return this.sorted((Comparator) Comparator.naturalOrder());
  }

  @Override
  public Seq<E> sorted(final Comparator<? super E> comparator) {
    final int size = this.array.length - this.offset;
    @SuppressWarnings("unchecked")
    final E[] copy = (E[]) new Object[size];
    System.arraycopy(this.array, this.offset, copy, 0, size);
    Arrays.sort(copy, comparator);
    return new ArraySeq<>(copy, 0);
  }

}
