package ch.claude_martin.function.tuple;

import java.util.Objects;

/** @see Quad */
class QuadImpl<A, B, C, D> implements Quad<A, B, C, D> {
  private static final long serialVersionUID = -4496369345024081861L;

  /** @see UniQuad */
  static class UniQuadImpl<T> extends QuadImpl<T, T, T, T> implements UniQuad<T> {
    private static final long serialVersionUID = 2144498575240333191L;

    public UniQuadImpl(final T a, final T b, final T c, final T d) {
      super(a, b, c, d);
    }
  }

  public QuadImpl(final A a, final B b, final C c, final D d) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
  }

  private final A a;
  private final B b;
  private final C c;
  private final D d;

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
  public D _4() {
    return this.d;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Quad))
      return false;
    final Quad<?, ?, ?, ?> e = (Quad<?, ?, ?, ?>) o;
    return Objects.equals(this.a, e._1()) && Objects.equals(this.b, e._2())
        && Objects.equals(this.c, e._3()) && Objects.equals(this.c, e._4());
  }

  @Override
  public int hashCode() {
    return (this.a == null ? 0 : this.a.hashCode()) ^ (this.b == null ? 0 : this.b.hashCode())
        ^ (this.c == null ? 0 : this.c.hashCode()) ^ (this.d == null ? 0 : this.d.hashCode());
  }

  @Override
  public String toString() {
    return "(" + this.a + ", " + this.b + ", " + this.c + ", " + this.d + ")";
  }

  @Override
  public int compareTo(final Quad<A, B, C, D> o) {
    return Objects.compare(this, o, COMPARATOR);
  }
}
