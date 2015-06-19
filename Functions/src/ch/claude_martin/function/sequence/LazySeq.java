package ch.claude_martin.function.sequence;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

public final class LazySeq<E> extends AbstractSeq<E> {

  private final static Object UNINITIALIZED = new Object();
  private final Callable<E>   callable;
  @SuppressWarnings("unchecked")
  private volatile E          _head         = (E) UNINITIALIZED;
  private volatile Seq<E>     _tail         = null;
  private volatile long       _length       = -1;

  LazySeq(final Callable<E> callable) {
    super();
    requireNonNull(callable, "callable");
    this.callable = callable;
  }

  @SuppressWarnings("unchecked")
  private void lazyHeadTail() {
    if (this._head != UNINITIALIZED && this._tail != null)
      return;
    synchronized (this) {
      if (this._head != UNINITIALIZED)
        return;
      try {
        this._head = this.callable.call();
        this._tail = new LazySeq<>(this.callable);
      } catch (final Exception e) {
        // This becomes am empty sequence:
        this._head = (E) NOTHING;
        this._tail = null;
        this._length = 0;
        assert this.isEmpty();
      }
    }
  }

  @Override
  public E head() {
    this.lazyHeadTail();
    final E head = this._head;
    if (head == NOTHING)
      throw new NoSuchElementException();
    return head;
  }

  @Override
  public Seq<E> tail() {
    this.lazyHeadTail();
    final Seq<E> tail = this._tail;
    if (tail == null)
      throw new NoSuchElementException();
    return tail;
  }

  @Override
  public long length() {
    long len = this._length;
    if (len != -1)
      return len;

    if (this.head() == NOTHING)
      len = 0;
    else {
      len = this.tail().length();
      if (len != Long.MAX_VALUE)
        len++;
    }
    return this._length = len;
  }

  @Override
  public boolean isEmpty() {
    E head = this._head;
    if (this._head == UNINITIALIZED) {
      this.lazyHeadTail();
      head = this._head;
    }
    return head == NOTHING;
  }
}
