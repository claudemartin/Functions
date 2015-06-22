package ch.claude_martin.function.sequence;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;

public final class ArraySeq<E> extends AbstractSeq<E> {
  private final E[] array;
  private final int offset;
  private ArraySeq<E> tail = null;

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
    if (this.tail == null)
      this.tail = new ArraySeq<>(this.array, newOffset);
    return this.tail;
  }

  @Override
  public long length() {
    return this.array.length - this.offset;
  }

  @Override
  public E get(final int index) {
    if (index < 0 || index >= this.array.length)
      throw new IndexOutOfBoundsException();
    return this.array[index];
  }

  @Override
  public Iterator<E> iterator() {
    if (this.offset == 0)
      return Arrays.asList(this.array).iterator();
    return super.iterator();
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

  @Override
  public Object[] toArray() {
    return Arrays.copyOf(this.array, this.array.length, Object[].class);
  }

  @Override
  public <T> T[] toArray(T[] a) {
    if(a.length != this.array.length)
      a = Arrays.copyOf(a, this.array.length);
    System.arraycopy(this.array, 0, a, 0, a.length);
    return a;
  }

  @Override
  public Stream<E> stream() {
    return Arrays.stream(this.array);
  }

  @Override
  public Spliterator<E> spliterator() {
    return Arrays.spliterator(this.array);
  }
}
