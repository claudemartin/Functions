package ch.claude_martin.function.sequence;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Returns elements until any exception is thrown. */
public final class LazySeq<E> extends AbstractSeq<E> {

  private final static Object UNINITIALIZED = new Object();
  private final Predicate<Consumer<E>> generator;
  @SuppressWarnings("unchecked")
  private volatile E          _head         = (E) UNINITIALIZED;
  private volatile Seq<E>     _tail         = null;
  private volatile long       _length       = -1;

  LazySeq(final Predicate<Consumer<E>> generator) {
    requireNonNull(generator, "generator");
    this.generator = generator;
  }
  LazySeq(final Callable<E> callable) {
    super();
    requireNonNull(callable, "callable");
    this.generator = c -> {
      try {
        c.accept(callable.call());
        return true;
      } catch (final Throwable e) {
        return false;
      }
    };
  }

  @SuppressWarnings("unchecked")
  private void lazyHeadTail() {
    if (this._head != UNINITIALIZED && this._tail != null)
      return;
    synchronized (this) {
      if (this._head != UNINITIALIZED)
        return;
      final boolean exisits = this.generator.test(x -> this._head = x);
      if (!exisits) {
        // This becomes am empty sequence:
        this._head = (E) NOTHING;
        this._tail = null;
        this._length = 0;
        assert this.isEmpty();
        return;
      }
      this._tail = new LazySeq<>(this.generator);
    }
  }

  @Override
  public E head() {
    E head = this._head;
    if (head == UNINITIALIZED) {
      this.lazyHeadTail();
      head = this._head;
    }

    if (head == NOTHING)
      throw new NoSuchElementException();
    return head;
  }

  @Override
  public Seq<E> tail() {
    E head = this._head;
    Seq<E> tail = this._tail;
    if (head == UNINITIALIZED || tail == null) {
      this.lazyHeadTail();
      head = this._head;
      tail = this._tail;
    }
    if (tail == null)
      throw new NoSuchElementException();
    return tail;
  }

  @Override
  public long length() {
    long len = this._length;
    if (len != -1)
      return len;

    if (this.isEmpty())
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
    if (head == UNINITIALIZED) {
      this.lazyHeadTail();
      head = this._head;
    }
    return head == NOTHING;
  }
}
