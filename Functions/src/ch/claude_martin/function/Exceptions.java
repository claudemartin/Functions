package ch.claude_martin.function;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/** Closures that throw exceptions. Most predefined functional interfaces do not throw any checked
 * exceptions. These utility methods help using methods that throw checked or unchecked exceptions. */
public final class Exceptions {

  private Exceptions() {
    throw new RuntimeException("Can't create utility class!");
  }

  static Throwable getCause(final Throwable t) {
    if (t instanceof SneakyException) {
      final Throwable cause = t.getCause();
      if (cause != null)
        return cause;
    }
    return t;
  }

  public static class SneakyException extends RuntimeException {
    private static final long serialVersionUID = 2339428349576960472L;

    public static RuntimeException of(final Throwable t) {
      if (t instanceof RuntimeException)
        return (RuntimeException) t;
      return new SneakyException("Execution of function caused a checked, but uncaught exception.",
          t);
    }

    public SneakyException() {
      super();
    }

    public SneakyException(final String message) {
      super(message);
    }

    public SneakyException(final String message, final Throwable cause,
        final boolean enableSuppression, final boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
    }

    public SneakyException(final String message, final Throwable cause) {
      super(message, cause);
    }

    public SneakyException(final Throwable cause) {
      super(cause);
    }

  }

  static String getMessage(final Throwable e, final String name) {
    return String.format("'%s' could not be executed because of: %s", getCause(e), name);
  }

  /** Creates a function that will have a name so that stack traces are easier to read. This must be
   * used after any other exceptions-related modifications of the function. */
  public static <T, R> Fn<T, R> named(final Function<? super T, ? extends R> f, final String name) {
    return t -> {
      try {
        return f.apply(t);
      } catch (final Throwable e) {
        throw new RuntimeException(getMessage(e, name), e);
      }
    };
  }

  /** Creates a function that will have a name so that stack traces are easier to read. */
  public static <T, U, R> BiFn<T, U, R> named(
      final BiFunction<? super T, ? super U, ? extends R> f, final String name) {
    return (t, u) -> {
      try {
        return f.apply(t, u);
      } catch (final Throwable e) {
        throw new RuntimeException(getMessage(e, name), e);
      }
    };
  }

  /** Returns the same function, because a risky function is also a sneaky function. Just use
   * {@link Function#apply} instead of RiskyFn#tryApply. */
  public static <T, R> Fn<T, R> sneaky(final RiskyFn<T, R> f) {
    return f;
  }

  /** Returns the same function, because a risky function is also a sneaky function. Just use
   * {@link BiFunction#apply} instead of RiskyBiFn#tryApply2. */
  public static <T, U, R> BiFn<T, U, R> sneaky(final RiskyBiFn<T, U, R> f) {
    return f;
  }

  /** Returns the same function, because a risky function is also a sneaky function. Just use
   * {@link TriFn#apply3} instead of RiskyTriFn#tryApply3. */
  public static <T, U, V, R> TriFn<T, U, V, R> sneaky(final RiskyTriFn<T, U, V, R> f) {
    return f;
  }

  /** Returns the same function, because a risky function is also a sneaky function. Just use
   * {@link QuadFn#apply4} instead of RiskyQuadFn#tryApply4. */
  public static <T, U, V, W, R> QuadFn<T, U, V, W, R> sneaky(final RiskyQuadFn<T, U, V, W, R> f) {
    return f;
  }

  public static <T, U, R> BiFn<T, U, R> handle(final BiFunction<T, U, R> f,
      final Consumer<Throwable> handler) {
    requireNonNull(f, "f");
    requireNonNull(handler, "handler");
    return (t, u) -> {
      try {
        return f.apply(t, u);
      } catch (final Throwable e) {
        handler.accept(e);
        throw SneakyException.of(e);
      }
    };
  }

  /** Returns the given value if any exception is thrown. This still may return null, if the given
   * function returns null. */
  public static <T, R> Fn<T, R> orElse(final Function<T, R> f, final R value) {
    return orElse(f, value, value);
  }

  public static <T, U, R> BiFn<T, U, R> orElse(final BiFunction<T, U, R> f, final R value) {
    return orElse(f, value, value);
  }

  public static <T, R> Fn<T, R> orElse(final Function<T, R> f, final R ifNull, final R ifException) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        final R result = f.apply(t);
        if (result == null)
          return ifNull;
        return result;
      } catch (final Throwable e) {
        return ifException;
      }
    };
  }

  public static <T, U, R> BiFn<T, U, R> orElse(final BiFunction<T, U, R> f, final R ifNull,
      final R ifException) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        final R result = f.apply(t, u);
        if (result == null)
          return ifNull;
        return result;
      } catch (final Throwable e) {
        return ifException;
      }
    };
  }

  public static <T, R> Fn<T, Optional<R>> toOptional(final Function<T, R> f) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        return Optional.ofNullable(f.apply(t));
      } catch (final Throwable e) {
        return Optional.empty();
      }
    };
  }

  public static <T, U, R> BiFn<T, U, Optional<R>> toOptional(final BiFunction<T, U, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        return Optional.ofNullable(f.apply(t, u));
      } catch (final Throwable e) {
        return Optional.empty();
      }
    };
  }

  public static <T, R> Fn<T, Maybe<R>> toMaybe(final Function<T, R> f) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        return Maybe.ofValue(f.apply(t));
      } catch (final Throwable e) {
        return Maybe.ofException(e);
      }
    };
  }

  public static <T, U, R> BiFn<T, U, Maybe<R>> toMaybe(
      final BiFunction<T, U, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        return Maybe.ofValue(f.apply(t, u));
      } catch (final Throwable e) {
        return Maybe.ofException(e);
      }
    };
  }

  public static <T, R> Fn<T, R> toNull(final Function<T, R> f) {
    return orElse(f, null);
  }

  public static <T, U, R> BiFn<T, U, R> toNull(final BiFunction<T, U, R> f) {
    return orElse(f, null);
  }

  public static <T, R> Fn<T, R> retry(final Function<T, R> f) {
    requireNonNull(f, "f");
    return (t) -> {
      final Thread thread = Thread.currentThread();
      while (true)
        try {
          if (thread.isInterrupted())
            throw new RuntimeException("Thread was interrupted", new InterruptedException());
          return f.apply(t);
        } catch (final Throwable e) {
        }
    };
  }

  public static <T, U, R> BiFn<T, U, R> retry(final BiFunction<T, U, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> {
      final Thread thread = Thread.currentThread();
      while (true)
        try {
          if (thread.isInterrupted())
            throw new RuntimeException("Thread was interrupted", new InterruptedException());
          return f.apply(t, u);
        } catch (final Throwable e) {
        }
    };
  }

}
