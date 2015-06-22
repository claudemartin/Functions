package ch.claude_martin.function.sequence;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.claude_martin.function.tuple.Pair;

@RunWith(value = Parameterized.class)
public class SeqTest {
  private final Seq<?>  sequence;
  private final boolean isFinite;

  public SeqTest(final String name, final Seq<?> s) {
    this.sequence = s;

    boolean _f = false;
    Seq<?> _s = s;
    for (int i = 0; i < 1000; i++) {
      if (_s.isEmpty()) {
        _f = true; // found end!
        break;
      }
      _s = _s.tail();
    }
    this.isFinite = _f;

  }

  static void assertThrows(final Class<? extends Throwable> type, final Runnable r) {
    try {
      r.run();
      fail("expected: " + type);
    } catch (final Throwable e) {
      if (!type.isAssignableFrom(e.getClass()))
        assertEquals(type, e.getClass());
      // expected!
    }
  }

  @Parameters(name = "{0}")
  public static Iterable<Object[]> params() {
    try {
      final SecureRandom rng = new SecureRandom();
      final Seq<Long> s = Seq.generate(() -> rng.nextLong());
      final Seq<Integer> oneTo4 = Seq.ofInts(1, 2, 3, 4);
      final Seq<Object> misc = Seq.<Object> seq(rng, Seq.seq(null, oneTo4));
      return Arrays.asList(//
          new Object[][] { //
              { "array", oneTo4 }, //
              { "linked", Seq.seq(1, Seq.seq(2, Seq.seq(3, Seq.seq(4, Seq.empty())))) }, //
              { "empty", Seq.empty() },//
              { "null", Seq.of(null, oneTo4) }, //
              { "misc", misc },//
              { "range", Seq.range(-128, 128) }, //
              { "infinite", s },//
              { "iterate", Seq.iterate(System.nanoTime(), x -> rng.nextLong()) },//
              { "append", oneTo4.append(oneTo4) },//
              { "repeat", misc.repeat(4, 11) },//
              { "take", s.take(4) }
          });
    } catch (final Throwable e) {
      throw new RuntimeException("Can't create sequences for test.", e);
    }
  }

  @Test
  public final void testSize() {
    if (!this.isFinite)
      return;
    final int size = this.sequence.size();

    assert size == this.sequence.length();
  }

  @Test
  public final void testIsEmpty() {
    if (!this.isFinite)
      assertFalse(this.sequence.isEmpty());
    else
      assertTrue(this.sequence.isEmpty() == (this.sequence.length() == 0));
  }

  @Test
  public final void testToSeq() {
    if (!this.isFinite)
      return;
    final Seq<?> clone = this.sequence.stream().collect(Seq.toSeq());
    assertEquals(this.sequence, clone);
  }

  @Test
  public final void testHead() {
    if (this.sequence.isEmpty())
      assertThrows(NoSuchElementException.class, this.sequence::head);
    else
      assertEquals(this.sequence.get(0), this.sequence.head());
  }

  @Test
  public final void testTail() {
    if (this.sequence.isEmpty())
      assertThrows(NoSuchElementException.class, this.sequence::tail);
    else
      assertEquals(this.sequence.length() - 1, this.sequence.tail().length());
  }

  @Test
  public final void testLast() {
    if (!this.isFinite)
      return;
    if (this.sequence.isEmpty())
      assertThrows(NoSuchElementException.class, this.sequence::last);
    else
      assertEquals(this.sequence.reverse().head(), this.sequence.last());
  }

  @Test
  public final void testInit() {
    if (!this.isFinite) {
      // There's no last element so it should return itself if it's infinite!
      assertEquals(this.sequence.take(10), this.sequence.init().take(10));
      return;
    }
    final long length = this.sequence.length();
    if (this.sequence.isEmpty())
      assertThrows(NoSuchElementException.class, this.sequence::init);
    else
      assertEquals(this.sequence.take(length - 1), this.sequence.init());
  }

  @Test
  public final void testLength() {
    if (!this.isFinite)
      return;
    assertEquals(this.sequence.stream().count(), this.sequence.length());
  }

  @Test
  public final void testIsFinite() {
    if (!this.isFinite)
      return;
    assertEquals(this.isFinite, this.sequence.isFinite());
  }

  @Test
  public final void testFold() {
    // not tested becuase it's the same as foldLeft.
  }

  @Test
  public final void testFoldLeft() {
    if (!this.isFinite)
      return;

    final BinaryOperator<String> accumulator = String::concat;
    final String string = this.sequence.map(Objects::toString).foldLeft(accumulator, "X");
    assertEquals(this.sequence.stream().map(Objects::toString).reduce("X", accumulator), string);
  }

  @Test
  public final void testFoldRight() {
    if (!this.isFinite)
      return;

    final BinaryOperator<String> accumulator = String::concat;
    final String string = this.sequence.map(Objects::toString).foldRight(accumulator, "X");
    assertEquals(this.sequence.stream().map(Objects::toString).collect(Collectors.joining()) + "X",
        string);
  }

  @SuppressWarnings("unchecked")
  @Test
  public final void testAppend() {
    if (!this.isFinite) {
      // append is lazy, so it should return fast but not really do anything.
      Seq.concat(this.sequence, this.sequence);
      return;
    }

    final Seq<Object> s = (Seq<Object>) this.sequence;

    final Seq<Object> s2 = s.append(s);
    assertEquals(2 * s.length(), s2.length());

    assertEquals(Seq.empty(), Seq.empty().append(Seq.empty()));
    assertEquals(s, s.append(Seq.empty()));
    assertEquals(s, Seq.empty().append(s));

    assertEquals(s.length() + 3, s.append(Seq.of(1, 2, 3)).length());

  }

  @Test
  public final void testGet() {
    if (this.sequence.isEmpty()) {
      assertThrows(IndexOutOfBoundsException.class, () -> this.sequence.get(0));
      return;
    }
    if (!this.isFinite) {
      assertSame(this.sequence.head(), this.sequence.get(0));
      assertSame(this.sequence.get(123), this.sequence.get(123));
      return;
    }

    assertThrows(IndexOutOfBoundsException.class, () -> this.sequence.get(this.sequence.size()));
    assertThrows(IndexOutOfBoundsException.class, () -> this.sequence.get(-1));
  }

  @Test
  public final void testTake() {
    assertEquals(Seq.empty(), this.sequence.take(0));
    assertEquals(Seq.empty(), this.sequence.take(-5));

    if (this.sequence.isEmpty())
      assertEquals(Seq.empty(), this.sequence.take(5));
    else
      assertEquals(Seq.of(this.sequence.head()), this.sequence.take(1));

    assertSame(this.sequence, this.sequence.take(Seq.INFINITY));

    if (!this.isFinite)
      return;

    assertEquals(this.sequence, this.sequence.take(this.sequence.length()));
    assertEquals(this.sequence, this.sequence.take(this.sequence.length() + 100));


    assertEquals(Seq.of(1, 2, 3, 4), Seq.range(1, 100).take(4));
  }

  @Test
  public final void testDrop() {

  }

  @Test
  public final void testRepeat() {
    if (this.sequence.isEmpty()) {
      assertEquals(Seq.empty(), this.sequence.repeat());
      return;
    }

    if (!this.isFinite) {
      assertEquals(this.sequence.head(), this.sequence.repeat().head());
      return;
    }

    final long length = this.sequence.length();
    assertEquals(this.sequence, this.sequence.repeat(0, length));

    for (int offset = 0; offset < length; offset++)
      for (int len = 0; len < length * 3; len += 3) {
        final Seq<?> r = this.sequence.repeat(offset, len);
        assertEquals("off=" + offset + ",len=" + len, len, r.length());
        if (len > 0)
          assertEquals(this.sequence.get(offset), r.head());
      }
  }

  @Test
  public final void testFilter() {
    if (!this.isFinite) {
      // must be lazy and return quickly:
      this.sequence.filter(Objects::nonNull).take(10).forEach(e -> assertNotNull(e));
      return;
    }

    assertEquals(Seq.empty(), this.sequence.filter(e -> false));
    assertEquals(this.sequence, this.sequence.filter(e -> true));

    final Seq<Integer> s = Seq.ofInts(1, 2, 3, 4, 5);
    assertEquals(s.tail(), s.filter(i -> i != 1));
    assertEquals(Seq.ofInts(1), s.filter(i -> i == 1));
  }

  @Test
  public final void testPartition() {
    if(!this.isFinite)
      return;
    final Random r = new Random();
    @SuppressWarnings({ "rawtypes", "unchecked" })
    final Pair<Seq, Seq> partition = (Pair) this.sequence.partition(e -> r.nextBoolean());
    assertEquals(this.sequence.length(), partition._1().length() + partition._2().length());
  }

  @Test
  public final void testDistinct() {
    if (!this.isFinite)
      return;
    final Seq<?> set = this.sequence.distinct();

    assertTrue(set.length() <= this.sequence.length());
    assertEquals(set.stream().distinct().collect(Collectors.toSet()), new HashSet<Object>(set));

    for (final Object e : this.sequence)
      assertEquals(set.indexOf(e), set.lastIndexOf(e));
  }

  @Test
  public final void testAll() {
    if (this.sequence.isEmpty())
      assertTrue(this.sequence.all(v -> false));
    if (!this.isFinite)
      return;
    assertTrue(this.sequence.all(v -> true));

    assertTrue(this.sequence.all(this.sequence::contains));
  }

  @Test
  public final void testAny() {
    if (this.sequence.isEmpty())
      assertFalse(this.sequence.any(v -> true));
    else {
      assertTrue(this.sequence.any(v -> true));
      assertTrue(this.sequence.any(v -> v == this.sequence.head()));
    }
    if (this.isFinite)
      assertFalse(this.sequence.any(v -> false));
  }

  @Test
  public final void testMap() {
    // must be lazy and return quickly, even for infinite sequences:
    final Seq<?> ident = this.sequence.map(Function.identity());

    final int size = this.isFinite ? this.sequence.size() : 10;
    for (int i = 0; i < size; i++)
      assertSame(this.sequence.get(i), ident.get(i));
    if (!this.isFinite)
      return;

    if (this.sequence.isEmpty()) {
      assertEquals(Seq.empty(), this.sequence.map(x -> {
        fail("nothing to be mapped!");
        return null;
      }));
      return;
    }
    final Seq<String> strings = this.sequence.map(x -> Objects.toString(x));
    assertEquals(Objects.toString(this.sequence.head()), strings.head());
  }

  @Test
  public final void testSort() {
    // see testSorted !
    assertThrows(UnsupportedOperationException.class, () -> this.sequence.sort((a, b) -> 0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public final void testSorted() {
    if (!this.isFinite)
      return;
    if (this.sequence.isEmpty()) {
      assertEquals(Seq.empty(), this.sequence.sorted());
      return;
    }

    // Only works if all elements are comparable!
    assumeTrue(this.sequence.all(e -> e == null || e instanceof Comparable));

    assertEquals(this.sequence.stream().sorted().collect(Seq.toSeq()), this.sequence.sorted());

    assertEquals(this.sequence.stream().sorted().collect(Seq.toSeq()), this.sequence.sorted());

    @SuppressWarnings("rawtypes")
    final Comparator c = Comparator.nullsFirst(Comparator.naturalOrder()).reversed();
    assertEquals(this.sequence.stream().sorted(c).collect(Seq.toSeq()), this.sequence.sorted(c));
  }

  @Test
  public final void testToArray() {
    if (!this.isFinite) {
      assertThrows(OutOfMemoryError.class, () -> this.sequence.toArray());
      return;
    }

    assertArrayEquals(this.sequence.stream().toArray(), this.sequence.toArray());
  }

  @Test
  public final void testToString() {
    if (!this.isFinite)
      return;

    final String string = this.sequence.toString();
    if (this.sequence.isEmpty())
      assertEquals(Collections.emptySet().toString(), string);
    else
      assertEquals(1, string.indexOf(Objects.toString(this.sequence.head())));

    assertEquals("[1, 2, 3]", Seq.of(1, 2, 3).toString());
  }

  @Test
  public final void testForEach() {
    if (!this.isFinite)
      return;
    if (this.sequence.isEmpty())
      this.sequence.forEach(e -> fail("noting to 'foreach'"));
    final List<Object> list = new LinkedList<>();
    this.sequence.forEach(list::add);
    assertEquals(this.sequence, list);
  }

  @Test
  public final void testContains() {
    if (this.sequence.isEmpty()) {
      assertFalse(this.sequence.contains(null));
      assertFalse(this.sequence.contains(this.sequence));
      return;
    }
    assertTrue(this.sequence.contains(this.sequence.head()));
    if (this.isFinite)
      assertFalse(this.sequence.contains(new Object()));
    else
      assertTrue(this.sequence.contains(this.sequence.get(100)));

    assertTrue(Seq.of(5, null).contains(null));
    assertTrue(Seq.of(this.sequence).contains(this.sequence));
  }

  @Test
  public final void testIterator() {
    final Iterator<?> itr = this.sequence.iterator();

    if (!this.isFinite) {
      assertTrue(itr.hasNext());
      return;
    }

    // UnsupportedOperationException or IllegalStateException:
    assertThrows(RuntimeException.class, itr::remove);

    if (!this.sequence.isEmpty()) {
      assertSame(this.sequence.head(), itr.next());
      itr.forEachRemaining(x -> {
        assertTrue(this.sequence.contains(x));
      });
    }
    assertFalse(itr.hasNext());

    assertThrows(UnsupportedOperationException.class, itr::remove);
  }


  @Test
  public final void testReverse() {
    if (!this.isFinite) {
      // Must be lazy and return quickly:
      this.sequence.reverse();
      return;
    }

    if (this.sequence.isEmpty()) {
      assertEquals(Seq.empty(), this.sequence.reverse());
      return;
    }


  }

}
