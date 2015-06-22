package ch.claude_martin.function.sequence;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RepeatingSeqTest {

  Seq<Integer> s0123       = Seq.ofInts(0, 1, 2, 3);
  Seq<Integer> s1230123012 = this.s0123.repeat(1, 10);
  Seq<Integer> s30         = this.s0123.repeat(3, 2);
  Seq<Integer> sInfinite   = this.s0123.repeat();
  Seq<Object>  sObjects    = Seq.of(null, this.s30, Optional.empty(), null).repeat();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

  }

  @Before
  public void setUp() throws Exception {
    assertEquals(Seq.of(1, 2, 3, 0, 1, 2, 3, 0, 1, 2), this.s1230123012);
    assertEquals(Seq.of(3, 0), this.s30);
  }

  @Test
  public final void testIsEmpty() {
    assertTrue(Seq.empty().repeat().isEmpty());
    assertFalse(Seq.of(1, 2, 3).repeat().isEmpty());
    assertFalse(this.s30.isEmpty());
    assertFalse(this.sObjects.isEmpty());
  }

  @Test
  public final void testReverse() {
    assertTrue(!this.sInfinite.reverse().isFinite());
    assertTrue(!this.sObjects.reverse().isFinite());

    assertEquals(this.s30, this.s30.reverse().reverse());
    assertEquals(this.s1230123012, this.s1230123012.reverse().reverse());

    for (int x = 3; x <= 5; x++)
      for (int y = 7; y <= 11; y += 2) {
        final Seq<Integer> s = Seq.range(x, y);
        assertEquals(s, s.reverse().reverse());
      }
  }

  @Test
  public final void testContainsObject() {
    assertTrue(this.s30.contains(3));
    assertTrue(this.s30.contains(0));
    assertFalse(this.s30.contains(1));

    assertTrue(this.sObjects.contains(null));
    assertFalse(this.sInfinite.contains(null));
  }

  @Test
  public final void testIterator() {
    int count = 0;
    for (final Integer i : this.s1230123012) {
      count++;
      assertTrue(i < 100);
    }
    assertEquals(this.s1230123012.size(), count);
  }

  @Test
  public final void testToArray() {
    assertArrayEquals(new Integer[] { 3, 0 }, this.s30.toArray());
    assertArrayEquals(new Integer[] { 3, 0 }, this.s30.toArray(new Integer[2]));

    assertArrayEquals(new Integer[] { 1, 2, 3, 0, 1, 2, 3, 0, 1, 2 },
        this.s1230123012.toArray(new Integer[0]));

  }

  @Test
  public final void testGetInt() {
    assertEquals(3, (int) this.s30.get(0));
    assertEquals(0, (int) this.s30.get(1));

    assertEquals(2, (int) this.s1230123012.get(1));
    assertEquals(2, (int) this.s1230123012.get(5));
    assertEquals(2, (int) this.s1230123012.get(9));
  }

  @Test
  public final void testIndexOf() {

    assertEquals(0, this.s30.indexOf(3));
    assertEquals(1, this.s30.indexOf(0));
    assertEquals(-1, this.s30.indexOf(1));
    assertEquals(-1, this.s30.indexOf(2));

    assertEquals(3, this.s1230123012.indexOf(0));
    assertEquals(0, this.s1230123012.indexOf(1));
    assertEquals(1, this.s1230123012.indexOf(2));
    assertEquals(2, this.s1230123012.indexOf(3));
    assertEquals(-1, this.s1230123012.indexOf(4));
  }

  @Test
  public final void testLastIndexOf() {

    assertEquals(0, this.s30.lastIndexOf(3));
    assertEquals(1, this.s30.lastIndexOf(0));
    assertEquals(-1, this.s30.lastIndexOf(1));
    assertEquals(-1, this.s30.lastIndexOf(2));

    assertEquals(7, this.s1230123012.lastIndexOf(0));
    assertEquals(8, this.s1230123012.lastIndexOf(1));
    assertEquals(9, this.s1230123012.lastIndexOf(2));
    assertEquals(6, this.s1230123012.lastIndexOf(3));
    assertEquals(-1, this.s1230123012.lastIndexOf(4));

    // [2,3,0,1,2,3,0,1,*2*]
    assertEquals(8, this.s0123.repeat(2, 9).lastIndexOf(2));
    // [5,6,0,1,2,3,4,5,6,0,1,*2*,3]
    assertEquals(11, Seq.range(0, 7).repeat(5, 13).lastIndexOf(2));
  }

  @Test
  public final void testHead() {
    assertEquals(3, (int) this.s30.head());
    assertEquals(1, (int) this.s1230123012.head());
  }

  @Test
  public final void testTail() {
    assertEquals(Seq.of(0), this.s30.tail());
  }

  @Test
  public final void testLength() {
    assertEquals(2, this.s30.length());
    assertEquals(10, this.s1230123012.length());
  }

  @Test
  public final void testSorted() {
    Seq<Integer> sorted = this.s30.sorted();
    assertEquals(Seq.of(0, 3), sorted);
    sorted = this.s1230123012.sorted();
    assertEquals(Seq.of(0, 0, 1, 1, 1, 2, 2, 2, 3, 3), sorted);
  }

}
