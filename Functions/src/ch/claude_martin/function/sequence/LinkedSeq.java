package ch.claude_martin.function.sequence;

import java.util.NoSuchElementException;

import ch.claude_martin.function.Functions;

/** Sequence implemented as finite, linked list. */
public final class LinkedSeq<E> extends AbstractSeq<E> implements Seq<E> {
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
  public Seq<E> tail() {
    if (this.isEmpty())
      throw new NoSuchElementException();
    return this._tail;
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

  // Collection / List:

}