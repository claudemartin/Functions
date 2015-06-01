package ch.claude_martin.function;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Closures that throw exceptions. Most predefined functional interfaces do not throw any checked
 * exceptions. These utility methods help using methods that throw checked or unchecked exceptions.
 * */
public class Exceptions {

  public static class SneakyException extends RuntimeException {
    private static final long serialVersionUID = 2339428349576960472L;

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

  @FunctionalInterface
  public static interface RiskyFn<T, R> extends Fn<T, R> {

    /** Tries to invoke this function but throws a {@link SneakyException} if execution fails. */
    @Override
    public default R apply(final T t) {
      try {
        return this.tryApply(t);
      } catch (final Throwable x) {
        throw rte(x);
      }
    };

    public abstract R tryApply(final T t) throws Throwable;

    /** Converts a checked exception to an unchecked (sneaky) exception. */
    @Override
    public default Fn<T, R> sneaky() {
      return Exceptions.sneaky(this);
    }

    @Override
    public default Fn<T, R> orElse(final R value) {
      requireNonNull(value, "value");
      return Exceptions.orElse(this, value).andThen((final R r) -> r == null ? value : r);
    }

    public default Fn<T, R> orElse(final R ifNull, final R ifException) {
      return Exceptions.orElse(this, ifNull, ifException);
    }

    public default Fn<T, R> orElseGet(final Supplier<R> ifNull,
        final Function<Throwable, R> ifException) {
      return Exceptions.orElseGet(this, ifNull, ifException);
    }

    @Override
    public default Fn<T, R> orNull() {
      return Exceptions.orNull(this);
    }

    public default Fn<T, R> retry() {
      return Exceptions.retry(this);
    }

    public default Fn<T, R> log(final Consumer<Throwable> logger) {
      return Exceptions.log(this, logger);
    }

    public default Fn<T, Maybe<R>> toMaybe(final RiskyFn<T, R> f) {
      return Exceptions.toMaybe(this);
    }

    public default Fn<T, R> named(final String name) {
      return Exceptions.named(this, name);
    }
  }

  @FunctionalInterface
  public static interface RiskyBiFn<T, U, R> extends BiFn<T, U, R> {

    /** Tries to invoke this function but throws a {@link RuntimeException} if execution fails. */
    @Override
    public default R apply(final T t, final U u) {
      try {
        return this.tryApply(t, u);
      } catch (final Throwable x) {
        throw rte(x);
      }
    };

    public abstract R tryApply(final T t, final U u) throws Throwable;

    @Override
    public default BiFn<T, U, R> sneaky() {
      return Exceptions.sneaky(this);
    }

    @Override
    public default BiFn<T, U, R> orElse(final R value) {
      requireNonNull(value, "value");
      return Exceptions.orElse(this, value).andThen((final R r) -> r == null ? value : r);
    }

    public default BiFn<T, U, R> orElse(final R ifNull, final R ifException) {
      return Exceptions.orElse(this, ifNull, ifException);
    }

    public default BiFn<T, U, R> orElseGet(final Supplier<R> ifNull,
        final Function<Throwable, R> ifException) {
      return Exceptions.orElseGet(this, ifNull, ifException);
    }

    @Override
    public default BiFn<T, U, R> orNull() {
      return Exceptions.orNull(this);
    }

    public default BiFn<T, U, R> log(final Consumer<Throwable> logger) {
      return Exceptions.log(this, logger);
    }

    public default BiFn<T, U, Maybe<R>> toMaybe(final RiskyBiFn<T, U, R> f) {
      return Exceptions.toMaybe(this);
    }

    public default BiFn<T, U, R> named(final String name) {
      return Exceptions.named(this, name);
    }

  }

  /** Creates a function that will have a name so that stac ktraces are easier to read. */
  public static <T, R> Fn<T, R> named(final Function<T, R> f, final String name) {
    return t -> {
      try {
        return f.apply(t);
      } catch (final Throwable e) {
        throw new RuntimeException(name + " could not be executed because of " + e, e);
      }
    };
  }

  /** Creates a function that will have a name so that stac ktraces are easier to read. */
  public static <T, U, R> BiFn<T, U, R> named(final BiFunction<T, U, R> f, final String name) {
    return (t, u) -> {
      try {
        return f.apply(t, u);
      } catch (final Throwable e) {
        throw new RuntimeException(name + " could not be executed because of " + e, e);
      }
    };
  }

  /** @see #toSneakyException(Function) */
  public static <T, R> Fn<T, R> sneaky(final RiskyFn<T, R> f) {
    return toSneakyException(f);
  }

  /** @see #toSneakyException(BiFunction) */
  public static <T, U, R> BiFn<T, U, R> sneaky(final RiskyBiFn<T, U, R> f) {
    return toSneakyException(f);
  }

  static RuntimeException rte(final Throwable t) {
    if (t instanceof RuntimeException)
      return (RuntimeException) t;
    return new SneakyException("Execution of function caused a checked, but uncaught exception.", t);
  }

  public static <T, R> Fn<T, R> toSneakyException(final RiskyFn<T, R> f) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        return f.tryApply(t);
      } catch (final Throwable e) {
        throw rte(e);
      }
    };
  }

  public static <T, U, R> BiFn<T, U, R> toSneakyException(final RiskyBiFn<T, U, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        return f.tryApply(t, u);
      } catch (final Throwable e) {
        throw rte(e);
      }
    };
  }

  public static <T, R> Fn<T, R> log(final RiskyFn<T, R> f, final Consumer<Throwable> logger) {
    requireNonNull(f, "f");
    requireNonNull(logger, "logger");
    return (t) -> {
      try {
        return f.tryApply(t);
      } catch (final Throwable e) {
        logger.accept(e);
        throw rte(e);
      }
    };
  }

  public static <T, U, R> BiFn<T, U, R> log(final RiskyBiFn<T, U, R> f,
      final Consumer<Throwable> logger) {
    requireNonNull(f, "f");
    requireNonNull(logger, "logger");
    return (t, u) -> {
      try {
        return f.tryApply(t, u);
      } catch (final Throwable e) {
        logger.accept(e);
        throw rte(e);
      }
    };
  }

  /**
   * Returns the given value if any exception is thrown. This still may return null, if the given
   * function returns null.
   */
  public static <T, R> Fn<T, R> orElse(final RiskyFn<T, R> f, final R value) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        return f.tryApply(t);
      } catch (final Throwable e) {
        return value;
      }
    };
  }

  public static <T, U, R> BiFn<T, U, R> orElse(final RiskyBiFn<T, U, R> f, final R value) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        return f.tryApply(t, u);
      } catch (final Throwable e) {
        return value;
      }
    };
  }

  public static <T, R> Fn<T, R> orElse(final RiskyFn<T, R> f, final R ifNull, final R ifException) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        final R result = f.tryApply(t);
        if (result == null)
          return ifNull;
        return result;
      } catch (final Throwable e) {
        return ifException;
      }
    };
  }

  public static <T, U, R> BiFn<T, U, R> orElse(final RiskyBiFn<T, U, R> f, final R ifNull,
      final R ifException) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        final R result = f.tryApply(t, u);
        if (result == null)
          return ifNull;
        return result;
      } catch (final Throwable e) {
        return ifException;
      }
    };
  }

  public static <T, R> Fn<T, R> orNull(final RiskyFn<T, R> f) {
    requireNonNull(f, "f");
    return orElse(f, null);
  }

  public static <T, U, R> BiFn<T, U, R> orNull(final RiskyBiFn<T, U, R> f) {
    requireNonNull(f, "f");
    return orElse(f, null);
  }

  public static <T, R> Fn<T, R> orElseGet(final RiskyFn<T, R> f, final Supplier<R> supplier) {
    requireNonNull(f, "f");
    requireNonNull(supplier, "supplier");
    return (t) -> {
      try {
        return f.tryApply(t);
      } catch (final Throwable e) {
        return supplier.get();
      }
    };
  }

  public static <T, U, R> BiFn<T, U, R> orElseGet(final RiskyBiFn<T, U, R> f,
      final Supplier<R> supplier) {
    requireNonNull(f, "f");
    requireNonNull(supplier, "supplier");
    return (t, u) -> {
      try {
        return f.tryApply(t, u);
      } catch (final Throwable e) {
        return supplier.get();
      }
    };
  }

  public static <T, R> Fn<T, R> orElseGet(final RiskyFn<T, R> f, final Supplier<R> ifNull,
      final Function<Throwable, R> ifException) {
    requireNonNull(f, "f");
    requireNonNull(ifNull, "ifNull");
    requireNonNull(ifException, "ifException");
    return (t) -> {
      try {
        final R result = f.tryApply(t);
        if (result == null)
          return ifNull.get();
        return result;
      } catch (final Throwable e) {
        return ifException.apply(e);
      }
    };
  }

  public static <T, U, R> BiFn<T, U, R> orElseGet(final RiskyBiFn<T, U, R> f,
      final Supplier<R> ifNull, final Function<Throwable, R> ifException) {
    requireNonNull(f, "f");
    requireNonNull(ifNull, "ifNull");
    requireNonNull(ifException, "ifException");
    return (t, u) -> {
      try {
        final R result = f.tryApply(t, u);
        if (result == null)
          return ifNull.get();
        return result;
      } catch (final Throwable e) {
        return ifException.apply(e);
      }
    };
  }

  public static <T, R> Fn<T, Optional<R>> toOptional(final RiskyFn<T, R> f) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        return Optional.ofNullable(f.tryApply(t));
      } catch (final Throwable e) {
        return Optional.empty();
      }
    };
  }

  public static <T, U, R> BiFn<T, U, Optional<R>> toOptional(final RiskyBiFn<T, U, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        return Optional.ofNullable(f.tryApply(t, u));
      } catch (final Throwable e) {
        return Optional.empty();
      }
    };
  }

  public static <T, R> Fn<T, Maybe<R>> toMaybe(final RiskyFn<T, R> f) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        return Maybe.ofValue(f.tryApply(t));
      } catch (final Throwable e) {
        return Maybe.ofException(e);
      }
    };
  }

  public static <T, U, R> BiFn<T, U, Maybe<R>> toMaybe(final RiskyBiFn<T, U, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        return Maybe.ofValue(f.tryApply(t, u));
      } catch (final Throwable e) {
        return Maybe.ofException(e);
      }
    };
  }

  public static <T, R> Fn<T, R> toNull(final RiskyFn<T, R> f) {
    requireNonNull(f, "f");
    return (t) -> {
      try {
        return f.tryApply(t);
      } catch (final Throwable e) {
        return null;
      }
    };
  }

  public static <T, U, R> BiFn<T, U, R> toNull(final RiskyBiFn<T, U, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> {
      try {
        return f.tryApply(t, u);
      } catch (final Throwable e) {
        return null;
      }
    };
  }

  public static <T, R> Fn<T, R> retry(final RiskyFn<T, R> f) {
    requireNonNull(f, "f");
    return (t) -> {
      final Thread thread = Thread.currentThread();
      while (true)
        try {
          if (thread.isInterrupted())
            throw new RuntimeException("Thread was interrupted", new InterruptedException());
          return f.tryApply(t);
        } catch (final Throwable e) {
        }
    };
  }

  public static <T, U, R> BiFn<T, U, R> retry(final RiskyBiFn<T, U, R> f) {
    requireNonNull(f, "f");
    return (t, u) -> {
      final Thread thread = Thread.currentThread();
      while (true)
        try {
          if (thread.isInterrupted())
            throw new RuntimeException("Thread was interrupted", new InterruptedException());
          return f.tryApply(t, u);
        } catch (final Throwable e) {
        }
    };
  }

}
