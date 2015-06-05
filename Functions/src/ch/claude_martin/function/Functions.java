package ch.claude_martin.function;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;
import java.util.stream.*;
import java.util.stream.Collector.Characteristics;

import ch.claude_martin.function.tuple.Pair;
import ch.claude_martin.function.tuple.Pair.UniPair;

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

  public static <K, V> Pair<K, V> toPair(final K k, final V v) {
    return Pair.of(k, v);
  }

  public static <T, U, R> Fn2<T, U, R> curry(final Function<Entry<T, U>, R> f) {
    return t -> u -> f.apply(toPair(t, u));
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

  public static <T, U, R> Fn<Entry<? extends T, ? extends U>, R> uncurry(
      final Function<T, ? extends Function<? super U, ? extends R>> f) {
    return e -> f.apply(e.getKey()).apply(e.getValue());
  }

  public static <T, U, R> Fn<Entry<T, U>, R> uncurry2(
      final BiFunction<? super T, ? super U, ? extends R> f) {
    return e -> f.apply(e.getKey(), e.getValue());
  }

  /** Curried to {@link BiFunction}.
   * 
   * @see #toBiFunction2(Function) */
  public static <T, U, R> BiFn<T, U, R> toBiFunction(
      final Function<T, ? extends Function<? super U, ? extends R>> f) {
    return (t, u) -> f.apply(t).apply(u);
  }

  /** Uncurried to {@link BiFunction}.
   * 
   * @see #toBiFunction(Function) */
  public static <T, U, R> BiFn<T, U, R> toBiFunction2(
      final Function<? super Entry<T, U>, ? extends R> f) {
    return (t, u) -> f.apply(toPair(t, u));
  }

  public static <T, R> Supplier<R> setFirst(final Function<? super T, ? extends R> f, final T first) {
    return () -> f.apply(first);
  }

  public static <T, U, R> Fn<U, R> setFirst(final BiFunction<? super T, ? super U, ? extends R> f,
      final T first) {
    return snd -> f.apply(first, snd);
  }

  public static <T, U, R> Fn<T, R> setSecond(final BiFunction<? super T, ? super U, ? extends R> f,
      final U second) {
    return first -> f.apply(first, second);
  }

  public static <T, U, R> Fn<T, R> setSecond(
      final Function<? super T, ? extends Function<? super U, ? extends R>> f, final U second) {
    return first -> f.apply(first).apply(second);
  }

  public static <T, U, V, R> Fn<T, Fn<U, R>> setThird(
      final Function<? super T, ? extends Function<? super U, ? extends Function<? super V, ? extends R>>> f,
          final V third) {
    return first -> second -> f.apply(first).apply(second).apply(third);
  }

  @SuppressWarnings("unused")
  public static <T> List<UniPair<T>> zipUni(final Collection<? extends T> a,
      final Collection<? extends T> b) {
    requireNonNull(a, "a");
    requireNonNull(b, "b");
    return zip(//
        () -> new ArrayList<UniPair<T>>(Math.min(a.size(), b.size())), // creates new List
        Pair::uniform, // Creates Entry of two elements
        a, b); // both lists.
  }

  @SuppressWarnings("unused")
  public static <A, B> List<Pair<A, B>> zip(final Collection<? extends A> a,
      final Collection<? extends B> b) {
    requireNonNull(a, "a");
    requireNonNull(b, "b");
    return zip(//
        () -> new ArrayList<Pair<A, B>>(Math.min(a.size(), b.size())), // creates new List
        Functions::toPair, // Creates Entry of two elements
        a, b); // both lists.
  }

  static <A, B, PAIR> List<PAIR> zip(final Supplier<? extends List<PAIR>> supplier,
      final BiFunction<? super A, ? super B, ? extends PAIR> zipper, //
      final Iterable<? extends A> a, final Iterable<? extends B> b) {
    requireNonNull(supplier, "supplier");
    requireNonNull(zipper, "zipper");
    requireNonNull(a, "a");
    requireNonNull(b, "b");
    final Iterator<? extends A> itrA = a.iterator();
    final Iterator<? extends B> itrB = b.iterator();
    final List<PAIR> result = supplier.get();
    while (itrA.hasNext() && itrB.hasNext())
      result.add(zipper.apply(itrA.next(), itrB.next()));
    return result;
  }

  public static <A, B> Pair<List<A>, List<B>> unzip(final List<? extends Entry<A, B>> pairs) {
    requireNonNull(pairs, "pairs");
    final int size = pairs.size();
    final ArrayList<A> a = new ArrayList<>(size);
    final ArrayList<B> b = new ArrayList<>(size);
    final Pair<List<A>, List<B>> result = toPair(a, b);

    pairs.forEach(p -> {
      a.add(p.getKey());
      b.add(p.getValue());
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
    return (t, u) -> cache.computeIfAbsent(toPair(t, u), e -> f.apply(t, u));
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

  public static <T, U, R> Fn<T, Fn<U, R>> eager(final BiFunction<Supplier<T>, Supplier<U>, R> f) {
    requireNonNull(f, "f");
    return t -> u -> f.apply(() -> t, () -> u);
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

  public static <C, A, E> Collector<E, A, C> collector(final Supplier<A> supplier,
      final BiConsumer<A, E> accumulator, final BinaryOperator<A> combiner,
      final Function<A, C> finisher, final Collector.Characteristics... characteristics) {
    final EnumSet<Characteristics> set = EnumSet.noneOf(Characteristics.class);
    if (characteristics != null)
      set.addAll(asList(characteristics));
    return new Collector<E, A, C>() {

      @Override
      public Supplier<A> supplier() {
        return supplier;
      }

      @Override
      public BiConsumer<A, E> accumulator() {
        return accumulator;
      }

      @Override
      public BinaryOperator<A> combiner() {
        return combiner;
      }

      @Override
      public Function<A, C> finisher() {
        return finisher;
      }

      @Override
      public Set<java.util.stream.Collector.Characteristics> characteristics() {
        return unmodifiableSet(set);
      }

    };
  }

  /** Easy creation of a concurrent {@link Collector} from an existing, concurrent collection and an
   * accumulator. */
  public static <C, E> Collector<E, ?, C> collector(final C collection,
      final BiConsumer<C, E> accumulator) {
    return new Collector<E, C, C>() {

      @Override
      public Supplier<C> supplier() {
        return () -> collection;
      }

      @Override
      public BiConsumer<C, E> accumulator() {
        return accumulator;
      }

      @Override
      public BinaryOperator<C> combiner() {
        return (x, y) -> x;
      }

      @Override
      public Function<C, C> finisher() {
        return Function.identity();
      }

      @Override
      public Set<java.util.stream.Collector.Characteristics> characteristics() {
        return EnumSet.of(Characteristics.IDENTITY_FINISH, Characteristics.CONCURRENT);
      }
    };
  }

  /** Easy creation of a concurrent {@link Collector} from an existing collection. The collector can
   * be used on a parallel stream if the collection supports parallel access. */
  public static <C extends Collection<E>, E> Collector<E, ?, C> collector(final C collection) {
    return collector(collection, (c, e) -> c.add(e));
  }

  /** Easy creation of a concurrent {@link Collector} from a collection type. */
  public static <C extends Collection<E>, E> Collector<E, ?, C> collector(
      final Supplier<C> collection) {
    return collector(collection, Collection::add, (a, b) -> {
      a.addAll(b);
      return a;
    }, Function.identity(), Collections.singleton(Characteristics.IDENTITY_FINISH));
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
