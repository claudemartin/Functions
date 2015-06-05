package ch.claude_martin.function.tuple;

import java.util.Objects;

/** @see Triplet */
class TripletImpl<A, B, C> implements Triplet<A, B, C> {
  private static final long serialVersionUID = -201458571323494036L;

  /** @see UniTriplet */
  static class UniTripletImpl<T> extends TripletImpl<T, T, T> implements UniTriplet<T> {
    private static final long serialVersionUID = -6521205601425075746L;

    public UniTripletImpl(final T a, final T b, final T c) {
      super(a, b, c);
    }
  }

  public TripletImpl(final A a, final B b, final C c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  private final A           a;
  private final B           b;
  private final C           c;

  @Override
  public A _1() {
    return this.a;
  }

  @Override
  public B _2() {
    return this.b;
  }

  @Override
  public C _3() {
    return this.c;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Triplet))
      return false;
    final Triplet<?, ?, ?> e = (Triplet<?, ?, ?>) o;
    return Objects.equals(this.a, e._1()) && Objects.equals(this.b, e._2())
        && Objects.equals(this.c, e._3());
  }

  @Override
  public int hashCode() {
    return (this.a == null ? 0 : this.a.hashCode()) ^ (this.b == null ? 0 : this.b.hashCode())
        ^ (this.c == null ? 0 : this.c.hashCode());
  }

  @Override
  public String toString() {
    return "(" + this.a + ", " + this.b + ", " + this.c + ")";
  }

  @Override
  public int compareTo(final Triplet<A, B, C> o) {
    return Objects.compare(this, o, COMPARATOR);
  }
}
