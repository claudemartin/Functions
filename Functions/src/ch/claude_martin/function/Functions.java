package ch.claude_martin.function;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Functions {

  public static <T, R> Consumer<T> toVoid(final Function<T, R> f) {
    return f::apply;
  }

  public static <T, U, R> BiConsumer<T, U> toVoid(final BiFunction<T, U, R> f) {
    return f::apply;
  }

  public static <T, R> Function<T, R> toConstant(final Supplier<R> supplier) {
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

  public static <T, U, R> Function<T, Function<U, R>> curry(final Function<Entry<T, U>, R> f) {
    return t -> u -> f.apply(toPair(t, u));
  }

  public static <T, U, R> Function<T, Function<U, R>> curry(final BiFunction<T, U, R> f) {
    return t -> u -> f.apply(t, u);
  }

  public static <T, U, R> Function<Entry<T, U>, R> uncurry(final Function<T, Function<U, R>> f) {
    return e -> f.apply(e.getKey()).apply(e.getValue());
  }

  public static <T, U, R> Function<Entry<T, U>, R> uncurry2(final BiFunction<T, U, R> f) {
    return e -> f.apply(e.getKey(), e.getValue());
  }

  /**
   * Curried to {@link BiFunction}.
   * 
   * @see #toBiFunction2(Function)
   */
  public static <T, U, R> BiFunction<T, U, R> toBiFunction(final Function<T, Function<U, R>> f) {
    return (t, u) -> f.apply(t).apply(u);
  }

  /**
   * Uncurried to {@link BiFunction}.
   * 
   * @see #toBiFunction(Function)
   */
  public static <T, U, R> BiFunction<T, U, R> toBiFunction2(final Function<Entry<T, U>, R> f) {
    return (t, u) -> f.apply(toPair(t, u));
  }

  public static <T, R> Supplier<R> setFirst(final Function<T, R> f, final T first) {
    return () -> f.apply(first);
  }

  public static <T, U, R> Function<U, R> setFirst(final BiFunction<T, U, R> f, final T first) {
    return snd -> f.apply(first, snd);
  }

  public static <T, U, R> Function<T, R> setSecond(final BiFunction<T, U, R> f, final U second) {
    return first -> f.apply(first, second);
  }

  public static <T, U, R> Function<T, R> setSecond(final Function<T, Function<U, R>> f,
      final U second) {
    return first -> f.apply(first).apply(second);
  }

  public static <T, U, V, R> Function<T, Function<U, R>> setThird(
      final Function<T, Function<U, Function<V, R>>> f, final V third) {
    return first -> second -> f.apply(first).apply(second).apply(third);
  }

  public static <A, B> List<Pair<A, B>> zip(final Collection<A> a, final Collection<B> b) {
    requireNonNull(a, "a");
    requireNonNull(b, "b");
    return zip(//
        () -> new ArrayList<>(Math.min(a.size(), b.size())), // creates new List
        Functions::toPair, // Creates Entry of two elements
        a, b); // both lists.
  }

  static <A, B, PAIR> List<PAIR> zip(final Supplier<List<PAIR>> supplier,
      final BiFunction<A, B, PAIR> zipper, final Iterable<A> a, final Iterable<B> b) {
    requireNonNull(supplier, "supplier");
    requireNonNull(zipper, "zipper");
    requireNonNull(a, "a");
    requireNonNull(b, "b");
    final Iterator<A> itrA = a.iterator();
    final Iterator<B> itrB = b.iterator();
    final List<PAIR> result = supplier.get();
    while (itrA.hasNext() && itrB.hasNext())
      result.add(zipper.apply(itrA.next(), itrB.next()));
    return result;
  }

  public static <A, B> Pair<List<A>, List<B>> unzip(final List<Entry<A, B>> pairs) {
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

  public static <A, B> Map<A, B> toMap(final List<Entry<A, B>> pairs) {
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

  public static <T, R> Function<T, R> cached(final Function<T, R> f) {
    return cached(f, ConcurrentHashMap::new);
  }

  public static <T, R> Function<T, R> cached(final Function<T, R> f,
      final Supplier<Map<T, R>> supplier) {
    requireNonNull(f, "f");
    requireNonNull(supplier, "supplier");
    final Map<T, R> cache = supplier.get();
    return t -> cache.computeIfAbsent(t, f);
  }

  public static <T, U, R> BiFunction<T, U, R> cached(final BiFunction<T, U, R> f) {
    return cached(f, ConcurrentHashMap::new);
  }

  public static <T, U, R> BiFunction<T, U, R> cached(final BiFunction<T, U, R> f,
      final Supplier<Map<Entry<T, U>, R>> supplier) {
    requireNonNull(f, "f");
    requireNonNull(supplier, "supplier");
    final Map<Entry<T, U>, R> cache = supplier.get();
    return (t, u) -> cache.computeIfAbsent(toPair(t, u), e -> f.apply(t, u));
  }

  public static <T, R> Function<Supplier<T>, R> lazy(final Function<T, R> f) {
    requireNonNull(f, "f");
    return s -> f.apply(s.get());
  }

  public static <T, U, R> Function<Supplier<T>, Function<Supplier<U>, R>> lazy(
      final BiFunction<T, U, R> f) {
    requireNonNull(f, "f");
    return t -> u -> f.apply(t.get(), u.get());
  }

  public static <T, R> Function<T, R> eager(final Function<Supplier<T>, R> f) {
    requireNonNull(f, "f");
    return t -> f.apply(() -> t);
  }

  public static <T, U, R> Function<T, Function<U, R>> eager(
      final BiFunction<Supplier<T>, Supplier<U>, R> f) {
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

  public static <T> void forEachReversed(final T head, final Function<T, T> next,
      final Consumer<T> action) {
    forEachReversed(head, next, null, action);
  }

  public static <T> void forEachReversed(final T head, final Function<T, T> next, final T end,
      final Consumer<T> action) {
    requireNonNull(next, "next");
    requireNonNull(action, "action");
    if (Objects.equals(head, end))
      return;
    forEachReversed(next.apply(head), next, end, action);
    action.accept(head);
  }

  public static <T> Optional<T> find(final T head, final Function<T, T> next, final T end,
      final Predicate<T> predicate) {
    requireNonNull(predicate, "predicate");
    return stream(head, next, end).filter(predicate).findAny();
  }

  public static <T> Stream<T> stream(final T head, final Function<T, T> next, final T end) {
    requireNonNull(head, "head");
    requireNonNull(next, "next");
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(iterator(head, next, end), Spliterator.ORDERED), false);
  }

  public static <T> Iterator<T> iterator(final T head, final Function<T, T> next, final T end) {
    return new Iterator<T>() {
      T currentT = head;
      T nextT = next.apply(head);

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
}
