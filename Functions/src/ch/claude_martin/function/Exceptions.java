package ch.claude_martin.function;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Exceptions {

  public static <T, R> Function<T, R> named(final Function<T, R> f, final String name) {
    return t -> {
      try {
        return f.apply(t);
      } catch (final Throwable e) {
        throw new RuntimeException(name + " could not be executed because of " + e, e);
      }
    };
  }

  /** @see #toRuntimeException(Function) */
  public static <T, R> Function<T, R> sneaky(final Function<T, R> f) {
    return toRuntimeException(f);
  }

  /** @see #toRuntimeException(BiFunction) */
  public static <T, U, R> BiFunction<T, U, R> sneaky(final BiFunction<T, U, R> f) {
    return toRuntimeException(f);
  }

  private static RuntimeException rte(final Throwable t) {
    if (t instanceof RuntimeException)
      return (RuntimeException) t;
    return new RuntimeException("Execution of function caused a checked, but uncaught exception.",
        t);
  }

  public static <T, R> Function<T, R> toRuntimeException(final Function<T, R> f) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        return f.apply(t);
      } catch (final Throwable e) {
        throw rte(e);
      }
    };
  }

  public static <T, U, R> BiFunction<T, U, R> toRuntimeException(final BiFunction<T, U, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        return f.apply(t, u);
      } catch (final Throwable e) {
        throw rte(e);
      }
    };
  }

  public static <T, R> Function<T, R> log(final Function<T, R> f, final Consumer<Throwable> logger) {
    requireNonNull(f, "f");
    requireNonNull(logger, "logger");
    return (t) -> {
      try {
        return f.apply(t);
      } catch (final Throwable e) {
        logger.accept(e);
        throw rte(e);
      }
    };
  }

  public static <T, U, R> BiFunction<T, U, R> log(final BiFunction<T, U, R> f,
      final Consumer<Throwable> logger) {
    requireNonNull(f, "f");
    requireNonNull(logger, "logger");
    return (t, u) -> {
      try {
        return f.apply(t, u);
      } catch (final Throwable e) {
        logger.accept(e);
        throw rte(e);
      }
    };
  }

  public static <T, R> Function<T, R> orElse(final Function<T, R> f, final R value) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        return f.apply(t);
      } catch (final Throwable e) {
        return value;
      }
    };
  }

  public static <T, U, R> BiFunction<T, U, R> orElse(final BiFunction<T, U, R> f, final R value) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        return f.apply(t, u);
      } catch (final Throwable e) {
        return value;
      }
    };
  }

  public static <T, R> Function<T, R> orElse(final Function<T, R> f, final R ifNull,
      final R ifException) {
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

  public static <T, U, R> BiFunction<T, U, R> orElse(final BiFunction<T, U, R> f, final R ifNull,
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

  public static <T, R> Function<T, R> orNull(final Function<T, R> f) {
    requireNonNull(f, "f");
    return orElse(f, null);
  }

  public static <T, U, R> BiFunction<T, U, R> orNull(final BiFunction<T, U, R> f) {
    requireNonNull(f, "f");
    return orElse(f, null);
  }

  public static <T, R> Function<T, R> orElseGet(final Function<T, R> f, final Supplier<R> supplier) {
    requireNonNull(f, "f");
    requireNonNull(supplier, "supplier");
    return (t) -> {
      try {
        return f.apply(t);
      } catch (final Throwable e) {
        return supplier.get();
      }
    };
  }

  public static <T, U, R> BiFunction<T, U, R> orElseGet(final BiFunction<T, U, R> f,
      final Supplier<R> supplier) {
    requireNonNull(f, "f");
    requireNonNull(supplier, "supplier");
    return (t, u) -> {
      try {
        return f.apply(t, u);
      } catch (final Throwable e) {
        return supplier.get();
      }
    };
  }

  public static <T, R> Function<T, R> orElseGet(final Function<T, R> f, final Supplier<R> ifNull,
      final Function<Throwable, R> ifException) {
    requireNonNull(f, "f");
    requireNonNull(ifNull, "ifNull");
    requireNonNull(ifException, "ifException");
    return (t) -> {
      try {
        final R result = f.apply(t);
        if (result == null)
          return ifNull.get();
        return result;
      } catch (final Throwable e) {
        return ifException.apply(e);
      }
    };
  }

  public static <T, U, R> BiFunction<T, U, R> orElseGet(final BiFunction<T, U, R> f,
      final Supplier<R> ifNull, final Function<Throwable, R> ifException) {
    requireNonNull(f, "f");
    requireNonNull(ifNull, "ifNull");
    requireNonNull(ifException, "ifException");
    return (t, u) -> {
      try {
        final R result = f.apply(t, u);
        if (result == null)
          return ifNull.get();
        return result;
      } catch (final Throwable e) {
        return ifException.apply(e);
      }
    };
  }

  public static <T, R> Function<T, Optional<R>> toOptional(final Function<T, R> f) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        return Optional.ofNullable(f.apply(t));
      } catch (final Throwable e) {
        return Optional.empty();
      }
    };
  }

  public static <T, U, R> BiFunction<T, U, Optional<R>> toOptional(final BiFunction<T, U, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        return Optional.ofNullable(f.apply(t, u));
      } catch (final Throwable e) {
        return Optional.empty();
      }
    };
  }

  public static <T, R> Function<T, Maybe<R>> toMaybe(final Function<T, R> f) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        return Maybe.ofValue(f.apply(t));
      } catch (final Throwable e) {
        return Maybe.ofException(e);
      }
    };
  }

  public static <T, U, R> BiFunction<T, U, Maybe<R>> toMaybe(final BiFunction<T, U, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        return Maybe.ofValue(f.apply(t, u));
      } catch (final Throwable e) {
        return Maybe.ofException(e);
      }
    };
  }

  public static <T, R> Function<T, R> toNull(final Function<T, R> f) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        return f.apply(t);
      } catch (final Throwable e) {
        return null;
      }
    };
  }

  public static <T, U, R> BiFunction<T, U, R> toNull(final BiFunction<T, U, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        return f.apply(t, u);
      } catch (final Throwable e) {
        return null;
      }
    };
  }

  public static <T, R> Function<T, R> retry(final Function<T, R> f) {
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

  public static <T, U, R> BiFunction<T, U, R> retry(final BiFunction<T, U, R> f) {
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
