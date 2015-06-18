package ch.claude_martin.function;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ch.claude_martin.function.sequence.Seq;
import ch.claude_martin.function.tuple.Pair;
import ch.claude_martin.function.tuple.Pair.UniPair;
import ch.claude_martin.function.tuple.Quad;
import ch.claude_martin.function.tuple.Triplet;

/** Utility methods for functions.
 * 
 * @author Claude Martin */
public final class Functions {

  private Functions() {
    throw new RuntimeException("Can't create utility class!");
  }

  public static <T, R> Consumer<T> toVoid(final Function<T, R> f) {
    return f::apply;
  }

  public static <T, U, R> BiConsumer<T, U> toVoid(final BiFunction<T, U, R> f) {
    return f::apply;
  }

  public static <T, R> Fn<T, R> toConstant(final Supplier<R> supplier) {
    return x -> supplier.get();
  }

  public static <T, U, R> BiFunction<T, U, R> toConstant2(final Supplier<R> supplier) {
    return (t, u) -> supplier.get();
  }

  public static <T, U, R> BiFunction<U, T, R> swap(final BiFunction<T, U, R> f) {
    return (t, u) -> f.apply(u, t);
  }

  public static <T, U, R> Fn2<T, U, R> curry(final Function<Entry<T, U>, R> f) {
    return t -> u -> f.apply(Pair.of(t, u));
  }

  public static <T, U, V, R> Fn3<T, U, V, R> curry3(final Function<Triplet<T, U, V>, R> f) {
    return t -> u -> v -> f.apply(Triplet.of(t, u, v));
  }

  public static <T, U, V, W, R> Fn4<T, U, V, W, R> curry4(final Function<Quad<T, U, V, W>, R> f) {
    return t -> u -> v -> w -> f.apply(Quad.of(t, u, v, w));
  }

  public static <T, U, R> Fn2<T, U, R> curry(final BiFunction<T, U, R> f) {
    return t -> u -> f.apply(t, u);
  }

  public static <T, U, V, R> Fn3<T, U, V, R> curry(final TriFn<T, U, V, R> f) {
    return t -> u -> v -> f.apply3(t, u, v);
  }

  public static <T, U, V, W, R> Fn4<T, U, V, W, R> curry(final QuadFn<T, U, V, W, R> f) {
    return t -> u -> v -> w -> f.apply4(t, u, v, w);
  }

  public static <T, U, R> Fn<Entry<T, U>, R> uncurry2(
      final Function<T, ? extends Function<? super U, ? extends R>> f) {
    return e -> f.apply(e.getKey()).apply(e.getValue());
  }

  public static <T, U, R> Fn<Entry<T, U>, R> uncurryBi(
      final BiFunction<? super T, ? super U, ? extends R> f) {
    return e -> f.apply(e.getKey(), e.getValue());
  }

  public static <T, U, V, R> Fn<Triplet<T, U, V>, R> uncurry3(
      final Function<T, ? extends Function<? super U, ? extends Function<? super V, ? extends R>>> f) {
    requireNonNull(f, "f");
    return t -> f.apply(t._1()).apply(t._2()).apply(t._3());
  }

  public static <T, U, V, R> Fn<Triplet<T, U, V>, R> uncurryTri(
      final TriFn<? super T, ? super U, ? super V, ? extends R> f) {
    requireNonNull(f, "f");
    return t -> f.apply3(t._1(), t._2(), t._3());
  }

  public static <T, U, V, W, R> Fn<Quad<T, U, V, W>, R> uncurry4(
      final Function<T, ? extends Function<? super U, ? extends Function<? super V, ? extends Function<? super W, ? extends R>>>> f) {
    requireNonNull(f, "f");
    return t -> f.apply(t._1()).apply(t._2()).apply(t._3()).apply(t._4());
  }

  public static <T, U, V, W, R> Fn<Quad<T, U, V, W>, R> uncurryQuad(
      final QuadFn<? super T, ? super U, ? super V, ? super W, ? extends R> f) {
    requireNonNull(f, "f");
    return q -> f.apply4(q._1(), q._2(), q._3(), q._4());
  }

  /** Curried to {@link BiFunction}.
   * 
   * @see #toBiFunction2(Function) */
  public static <T, U, R> BiFn<T, U, R> toBiFunction(
      final Function<T, ? extends Function<? super U, ? extends R>> f) {
    requireNonNull(f, "f");
    return (t, u) -> f.apply(t).apply(u);
  }

  /** Uncurried to {@link BiFunction}.
   * 
   * @see #toBiFunction(Function) */
  public static <T, U, R> BiFn<T, U, R> toBiFunction2(
      final Function<? super Entry<T, U>, ? extends R> f) {
    requireNonNull(f, "f");
    return (t, u) -> f.apply(Pair.of(t, u));
  }

  public static <T, R> Supplier<R> set1st(final Function<? super T, ? extends R> f, final T first) {
    requireNonNull(f, "f");
    return () -> f.apply(first);
  }

  public static <T, U, R> Fn<U, R> set1st(final BiFunction<? super T, ? super U, ? extends R> f,
      final T first) {
    requireNonNull(f, "f");
    return snd -> f.apply(first, snd);
  }

  public static <T, U, V, R> BiFn<U, V, R> set1st(
      final TriFn<? super T, ? super U, ? super V, ? extends R> f, final T first) {
    requireNonNull(f, "f");
    return (snd, trd) -> f.apply3(first, snd, trd);
  }

  public static <T, U, V, W, R> TriFn<U, V, W, R> set1st(
      final QuadFn<? super T, ? super U, ? super V, ? super W, ? extends R> f, final T first) {
    requireNonNull(f, "f");
    return (snd, trd, fth) -> f.apply4(first, snd, trd, fth);
  }

  public static <T, U, R> Fn<T, R> set2nd(final BiFunction<? super T, ? super U, ? extends R> f,
      final U second) {
    requireNonNull(f, "f");
    return first -> f.apply(first, second);
  }

  public static <T, U, V, R> BiFn<T, V, R> set2nd(
      final TriFn<? super T, ? super U, ? super V, ? extends R> f, final U second) {
    requireNonNull(f, "f");
    return (first, third) -> f.apply3(first, second, third);
  }

  public static <T, U, V, W, R> TriFn<T, V, W, R> set2nd(
      final QuadFn<? super T, ? super U, ? super V, ? super W, ? extends R> f, final U second) {
    requireNonNull(f, "f");
    return (first, third, fourth) -> f.apply4(first, second, third, fourth);
  }

  public static <T, U, R> Fn<T, R> set2nd(
      final Function<? super T, ? extends Function<? super U, ? extends R>> f, final U second) {
    requireNonNull(f, "f");
    return first -> f.apply(first).apply(second);
  }

  public static <T, U, V, R> Fn2<T, U, R> set3rd(
      final Function<? super T, ? extends Function<? super U, ? extends Function<? super V, ? extends R>>> f,
          final V third) {
    requireNonNull(f, "f");
    return first -> second -> f.apply(first).apply(second).apply(third);
  }

  public static <T, U, V, R> BiFn<T, U, R> set3rd(final TriFn<T, U, V, R> f, final V third) {
    requireNonNull(f, "f");
    return (first, second) -> f.apply3(first, second, third);
  }

  public static <T, U, V, W, R> TriFn<T, U, W, R> set3rd(final QuadFn<T, U, V, W, R> f,
      final V third) {
    requireNonNull(f, "f");
    return (first, second, fourth) -> f.apply4(first, second, third, fourth);
  }

  public static <T, U, V, W, R> Fn3<T, U, V, R> set4th(
      final Function<? super T, ? extends Function<? super U, ? extends Function<? super V, ? extends Function<? super W, ? extends R>>>> f,
          final W fourth) {
    requireNonNull(f, "f");
    return first -> second -> third -> f.apply(first).apply(second).apply(third).apply(fourth);
  }

  public static <T, U, V, W, R> TriFn<T, U, V, R> set4th(final QuadFn<T, U, V, W, R> f,
      final W fourth) {
    requireNonNull(f, "f");
    return (first, second, third) -> f.apply4(first, second, third, fourth);
  }

  public static <T> Seq<UniPair<T>> zipUni(final Collection<? extends T> a,
      final Collection<? extends T> b) {
    Seq<UniPair<T>> result = Seq.empty();
    for (final UniPair<T> q : zip(UniPair::<T> of, a, b))
      result = Seq.seq(q, result);
    return result;
  }

  public static <A, B> Seq<Pair<A, B>> zip(final Collection<? extends A> a,
      final Collection<? extends B> b) {
    Seq<Pair<A, B>> result = Seq.empty();
    for (final Pair<A, B> q : zip(Pair::<A, B> of, a, b))
      result = Seq.seq(q, result);
    return result;
  }

  static <A, B, PAIR> Iterable<PAIR> zip(
      final BiFunction<? super A, ? super B, ? extends PAIR> zipper, //
      final Iterable<? extends A> a, final Iterable<? extends B> b) {
    requireNonNull(zipper, "zipper");
    final Iterator<? extends A> itrA = requireNonNull(a, "a").iterator();
    final Iterator<? extends B> itrB = requireNonNull(b, "b").iterator();

    return () -> {
      return new Iterator<PAIR>() {
        @Override
        public boolean hasNext() {
          return itrA.hasNext() && itrB.hasNext();
        }

        @Override
        public PAIR next() {
          return zipper.apply(itrA.next(), itrB.next());
        }
      };
    };
  }

  public static <A, B, C> Seq<Triplet<A, B, C>> zip(final Collection<? extends A> a,
      final Collection<? extends B> b, final Collection<? extends C> c) {
    Seq<Triplet<A, B, C>> result = Seq.empty();
    for (final Triplet<A, B, C> q : zip(Triplet::<A, B, C> of, a, b, c))
      result = Seq.seq(q, result);
    return result;
  }

  static <A, B, C, TRIPLET> Iterable<TRIPLET> zip(
      final TriFn<? super A, ? super B, ? super C, ? extends TRIPLET> zipper, //
      final Iterable<? extends A> a, final Iterable<? extends B> b, final Iterable<? extends C> c) {
    requireNonNull(zipper, "zipper");
    final Iterator<? extends A> itrA = requireNonNull(a, "a").iterator();
    final Iterator<? extends B> itrB = requireNonNull(b, "b").iterator();
    final Iterator<? extends C> itrC = requireNonNull(c, "c").iterator();

    return () -> {
      return new Iterator<TRIPLET>() {
        @Override
        public boolean hasNext() {
          return itrA.hasNext() && itrB.hasNext() && itrC.hasNext();
        }

        @Override
        public TRIPLET next() {
          return zipper.apply3(itrA.next(), itrB.next(), itrC.next());
        }
      };
    };
  }

  public static <A, B, C, D> Seq<Quad<A, B, C, D>> zip(final Collection<? extends A> a,
      final Collection<? extends B> b, final Collection<? extends C> c,
      final Collection<? extends D> d) {
    Seq<Quad<A, B, C, D>> result = Seq.empty();
    for (final Quad<A, B, C, D> q : zip(Quad::<A, B, C, D> of, a, b, c, d))
      result = Seq.seq(q, result);
    return result;
  }

  static <A, B, C, D, QUAD> Iterable<QUAD> zip(
      final QuadFn<? super A, ? super B, ? super C, ? super D, ? extends QUAD> zipper, //
      final Iterable<? extends A> a, final Iterable<? extends B> b, final Iterable<? extends C> c,
      final Iterable<? extends D> d) {
    requireNonNull(zipper, "zipper");
    final Iterator<? extends A> itrA = requireNonNull(a, "a").iterator();
    final Iterator<? extends B> itrB = requireNonNull(b, "b").iterator();
    final Iterator<? extends C> itrC = requireNonNull(c, "c").iterator();
    final Iterator<? extends D> itrD = requireNonNull(d, "d").iterator();

    return () -> {
      return new Iterator<QUAD>() {
        @Override
        public boolean hasNext() {
          return itrA.hasNext() && itrB.hasNext() && itrC.hasNext() && itrD.hasNext();
        }

        @Override
        public QUAD next() {
          return zipper.apply4(itrA.next(), itrB.next(), itrC.next(), itrD.next());
        }
      };
    };
  }

  public static <A, B> Pair<List<A>, List<B>> unzip(final List<? extends Entry<A, B>> pairs) {
    requireNonNull(pairs, "pairs");
    final int size = pairs.size();
    final ArrayList<A> a = new ArrayList<>(size);
    final ArrayList<B> b = new ArrayList<>(size);
    final Pair<List<A>, List<B>> result = Pair.of(a, b);

    pairs.forEach(p -> {
      a.add(p.getKey());
      b.add(p.getValue());
    });
    return result;
  }

  public static <A, B, C> Triplet<List<A>, List<B>, List<C>> unzip3(
      final List<? extends Triplet<A, B, C>> triplets) {
    requireNonNull(triplets, "triplets");
    final int size = triplets.size();
    final ArrayList<A> a = new ArrayList<>(size);
    final ArrayList<B> b = new ArrayList<>(size);
    final ArrayList<C> c = new ArrayList<>(size);
    final Triplet<List<A>, List<B>, List<C>> result = Triplet.of(a, b, c);

    triplets.forEach(p -> {
      a.add(p._1());
      b.add(p._2());
      c.add(p._3());
    });
    return result;
  }

  public static <A, B, C, D> Quad<List<A>, List<B>, List<C>, List<D>> unzip4(
      final List<? extends Quad<A, B, C, D>> quads) {
    requireNonNull(quads, "quads");
    final int size = quads.size();
    final ArrayList<A> a = new ArrayList<>(size);
    final ArrayList<B> b = new ArrayList<>(size);
    final ArrayList<C> c = new ArrayList<>(size);
    final ArrayList<D> d = new ArrayList<>(size);
    final Quad<List<A>, List<B>, List<C>, List<D>> result = Quad.of(a, b, c, d);

    quads.forEach(p -> {
      a.add(p._1());
      b.add(p._2());
      c.add(p._3());
      d.add(p._4());
    });
    return result;
  }

  public static <A, B> Map<A, B> toMap(final List<? extends Entry<A, B>> pairs) {
    return pairs.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  public static <R> Supplier<R> cached(final Supplier<R> f) {
    @SuppressWarnings("unchecked")
    final R nil = (R) new Object();
    final AtomicReference<R> ar = new AtomicReference<>(nil);
    return () -> {
      ar.compareAndSet(nil, f.get());
      return ar.get();
    };
  }

  public static <T, R> Fn<T, R> cached(final Function<T, R> f) {
    return cached(f, ConcurrentHashMap::new);
  }

  public static <T, R> Fn<T, R> cached(final Function<T, R> f, final Supplier<Map<T, R>> supplier) {
    requireNonNull(f, "f");
    requireNonNull(supplier, "supplier");
    final Map<T, R> cache = supplier.get();
    return t -> cache.computeIfAbsent(t, f);
  }

  public static <T, U, R> BiFn<T, U, R> cached(final BiFunction<T, U, R> f) {
    return cached(f, ConcurrentHashMap::new);
  }

  public static <T, U, R> BiFn<T, U, R> cached(final BiFunction<T, U, R> f,
      final Supplier<Map<Entry<T, U>, R>> supplier) {
    requireNonNull(f, "f");
    requireNonNull(supplier, "supplier");
    final Map<Entry<T, U>, R> cache = supplier.get();
    return (t, u) -> cache.computeIfAbsent(Pair.of(t, u), e -> f.apply(t, u));
  }

  public static <T, U, V, R> TriFn<T, U, V, R> cached(final TriFn<T, U, V, R> f) {
    return cached(f, ConcurrentHashMap::new);
  }

  public static <T, U, V, R> TriFn<T, U, V, R> cached(final TriFn<T, U, V, R> f,
      final Supplier<Map<Triplet<T, U, V>, R>> supplier) {
    requireNonNull(f, "f");
    requireNonNull(supplier, "supplier");
    final Map<Triplet<T, U, V>, R> cache = supplier.get();
    return (t, u, v) -> cache.computeIfAbsent(Triplet.of(t, u, v), e -> f.apply3(t, u, v));
  }

  public static <T, U, V, W, R> QuadFn<T, U, V, W, R> cached(final QuadFn<T, U, V, W, R> f) {
    return cached(f, ConcurrentHashMap::new);
  }

  public static <T, U, V, W, R> QuadFn<T, U, V, W, R> cached(final QuadFn<T, U, V, W, R> f,
      final Supplier<Map<Quad<T, U, V, W>, R>> supplier) {
    requireNonNull(f, "f");
    requireNonNull(supplier, "supplier");
    final Map<Quad<T, U, V, W>, R> cache = supplier.get();
    return (t, u, v, w) -> cache.computeIfAbsent(Quad.of(t, u, v, w), e -> f.apply4(t, u, v, w));
  }

  public static <T, R> Fn<Supplier<T>, R> lazy(final Function<T, R> f) {
    requireNonNull(f, "f");
    return s -> f.apply(s.get());
  }

  public static <T, U, R> Fn<Supplier<T>, Fn<Supplier<U>, R>> lazy(final BiFunction<T, U, R> f) {
    requireNonNull(f, "f");
    return t -> u -> f.apply(t.get(), u.get());
  }

  public static <T, R> Fn<T, R> eager(final Function<Supplier<T>, R> f) {
    requireNonNull(f, "f");
    return t -> f.apply(() -> t);
  }

  public static <T, U, R> BiFn<T, U, R> eager(final BiFunction<Supplier<T>, Supplier<U>, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> f.apply(() -> t, () -> u);
  }

  public static <T> void forEach(final T head, final Function<T, T> next, final Consumer<T> action) {
    forEach(head, next, null, action);
  }

  public static <T> void forEach(final T head, final Function<T, T> next, final T end,
      final Consumer<T> action) {
    requireNonNull(head, "head");
    requireNonNull(next, "next");
    requireNonNull(action, "action");
    for (T t = head; !Objects.equals(end, t); t = next.apply(t))
      action.accept(t);
  }

  public static <T> void forEachReversed(final T head, final Function<? super T, ? extends T> next,
      final Consumer<T> action) {
    forEachReversed(head, next, null, action);
  }

  public static <T> void forEachReversed(final T head, final Function<? super T, ? extends T> next,
      final T end, final Consumer<? super T> action) {
    requireNonNull(next, "next");
    requireNonNull(action, "action");
    if (Objects.equals(head, end))
      return;
    forEachReversed(next.apply(head), next, end, action);
    action.accept(head);
  }

  public static <T> Optional<T> find(final T head, final Function<? super T, ? extends T> next,
      final T end, final Predicate<? super T> predicate) {
    requireNonNull(predicate, "predicate");
    return stream(head, next, end).filter(predicate).findAny();
  }

  public static <T> Stream<T> stream(final T head, final Function<? super T, ? extends T> next,
      final T end) {
    requireNonNull(head, "head");
    requireNonNull(next, "next");
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(iterator(head, next, end), Spliterator.ORDERED), false);
  }

  public static <T> Iterator<T> iterator(final T head, final Function<? super T, ? extends T> next,
      final T end) {
    return new Iterator<T>() {
      T currentT = head;
      T nextT    = next.apply(head);

      @Override
      public boolean hasNext() {
        return this.nextT != end;
      }

      @Override
      public T next() {
        if (this.nextT == end)
          throw new NoSuchElementException();
        this.currentT = this.nextT;
        this.nextT = next.apply(this.nextT);
        return this.currentT;
      }
    };
  }

  public static <T, V, R> Fn<T, R> compose(final Function<V, R> f,
      final Function<? super T, ? extends V> g) {
    requireNonNull(f, "f");
    requireNonNull(g, "g");
    return (final T t) -> f.apply(g.apply(t));
  }

  public static <T, V, W, R> Fn<T, R> compose(final Function<W, R> f,
      final Function<? super V, ? extends W> g, final Function<? super T, ? extends V> h) {
    requireNonNull(f, "f");
    requireNonNull(g, "g");
    requireNonNull(h, "h");
    return (final T t) -> f.apply(g.apply(h.apply(t)));
  }

  public static <T, V, W, X, R> Fn<T, R> compose(final Function<X, R> f,
      final Function<? super W, ? extends X> g, final Function<? super V, ? extends W> h,
      final Function<? super T, ? extends V> i) {
    requireNonNull(f, "f");
    requireNonNull(g, "g");
    requireNonNull(h, "h");
    requireNonNull(i, "i");
    return (final T t) -> f.apply(g.apply(h.apply(i.apply(t))));
  }

  public static <T, R> Fn<T, R> sync(final Function<T, R> f) {
    return sync(f, new ReentrantLock());
  }

  public static <T, R> Fn<T, R> sync(final Function<T, R> f, final Lock lock) {
    requireNonNull(f, "f");
    requireNonNull(lock, "lock");
    return t -> {
      lock.lock();
      try {
        return f.apply(t);
      } finally {
        lock.unlock();
      }
    };
  }

  public static <T, R> Fn<T, R> sync(final Function<T, R> f, final Object mutex) {
    requireNonNull(f, "f");
    requireNonNull(mutex, "mutex");
    if (mutex instanceof Lock)
      throw new RuntimeException("Mutex implements Lock, but wasn't used as such.");
    return t -> {
      synchronized (mutex) {
        return f.apply(t);
      }
    };
  }

  public static <T, U, R> BiFn<T, U, R> sync(final BiFunction<T, U, R> f) {
    return sync(f, new ReentrantLock());
  }

  public static <T, U, R> BiFn<T, U, R> sync(final BiFunction<T, U, R> f, final Lock lock) {
    requireNonNull(f, "f");
    requireNonNull(lock, "lock");
    return (t, u) -> {
      lock.lock();
      try {
        return f.apply(t, u);
      } finally {
        lock.unlock();
      }
    };
  }

  public static <T, U, R> BiFn<T, U, R> sync(final BiFunction<T, U, R> f, final Object mutex) {
    requireNonNull(f, "f");
    requireNonNull(mutex, "mutex");
    if (mutex instanceof Lock)
      throw new RuntimeException("Mutex implements Lock, but wasn't used as such.");
    return (t, u) -> {
      synchronized (mutex) {
        return f.apply(t, u);
      }
    };
  }

  /** Throws {@link NullPointerException} of in- or output is null. */
  public static <T, R> Fn<T, R> nonNull(final Function<T, R> f) {
    requireNonNull(f, "f");
    return (t) -> {
      requireNonNull(t, "input must not be null");
      return requireNonNull(f.apply(t), "output must not be null");
    };
  }

  /** Throws {@link NullPointerException} of in- or output is null. */
  public static <T, U, R> BiFn<T, U, R> nonNull(final BiFunction<T, U, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> {
      requireNonNull(t, "input must not be null");
      requireNonNull(u, "input must not be null");
      return requireNonNull(f.apply(t, u), "output must not be null");
    };
  }

}
