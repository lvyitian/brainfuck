
package top.dsbbs2.brainfuck;

import java.io.FileInputStream;
import java.util.Objects;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class Main
{
  public static void main(final String[] args) throws Throwable
  {
    if (args.length == 0) {
      final Scanner input = new Scanner(System.in);
      input.useDelimiter("\n");
      while (true) {
        final String source = input.next().trim();
        if (Objects.equals(source, "exit")) {
          break;
        }
        try {
          Main.eval(source, new Vector<>(), new Reference<>(new AtomicInteger(0)), input);
        } catch (final Throwable e) {
          e.printStackTrace();
        }
      }
      input.close();
    } else {
      Main.eval(Main.readTextFile(args[0], "UTF8"), new Vector<>(), new Reference<>(new AtomicInteger(0)),
          new Scanner(System.in));
    }
  }

  public static String readTextFile(final String f, final String e) throws Throwable
  {
    try (FileInputStream inputStream = new FileInputStream(f)) {
      final byte[] buf = new byte[inputStream.available()];
      inputStream.read(buf);
      return new String(buf, e);
    }
  }

  public static String charArrToString(final char[] arr, final int start, final int end)
  {
    final StringBuilder builder = new StringBuilder();
    for (int i = start; i < end; i++) {
      builder.append(arr[i]);
    }
    return builder.toString();
  }

  public static void extendArray(final Vector<Character> ca, final Reference<AtomicInteger> index)
  {
    while (index.value.get() >= ca.size()) {
      ca.add('\0');
    }
  }

  public static long countString(final String o, final String target)
  {
    long count = 0;
    int lastIndex = o.indexOf(target);
    while (lastIndex != -1) {
      count++;
      lastIndex = o.indexOf(target, lastIndex + 1);
    }
    return count;
  }

  public static Vector<CustomEntry<Integer, Integer>> readLoops(final String source)
  {
    final Vector<CustomEntry<Integer, Integer>> ret = new Vector<>();
    final char[] tmp = source.toCharArray();
    int count = 0;
    for (int i = 0; i < tmp.length; i++) {
      final char c = tmp[i];
      if (Objects.equals(c, '[')) {
        if (count == 0) {
          ret.add(new CustomEntry<>(i, -1));
        }
        count++;
      } else if (Objects.equals(c, ']')) {
        count--;
        if (count == 0) {
          final CustomEntry<Integer, Integer> tce = ret.stream().filter(i2 -> Objects.equals(i2.getValue(), -1))
              .findFirst().orElse(null);
          if (tce == null) {
            throw new BrainfuckException(i, "unexpected null value");
          }
          tce.setValue(i);
        }
      }
    }
    return ret;
  }

  public static void eval(final String source, final Vector<Character> ca, final Reference<AtomicInteger> index,
      final Scanner s)
  {
    if (Main.countString(source, "[") != Main.countString(source, "]")) {
      throw new BrainfuckException("the number of [ doesn't match with the number of ]");
    }
    Main.extendArray(ca, index);
    final char[] arr = source.toCharArray();
    final Vector<CustomEntry<Integer, Integer>> locs = Main.readLoops(source);
    for (int c = 0; c < arr.length; c++) {
      try {
        final char i = arr[c];
        if (Objects.equals(i, ' ') || Objects.equals(i, '\n') || Objects.equals(i, '\r')) {
          continue;
        }
        final int c2 = c;
        final CustomEntry<Integer, Integer> tce = locs.parallelStream().filter(i2 -> Objects.equals(i2.getKey(), c2))
            .findFirst().orElse(null);
        if (tce != null) {
          final String ws = Main.charArrToString(arr, c + 1, tce.getValue());
          while (ca.get(index.value.get()) != '\0') {
            Main.eval(ws, ca, index, s);
          }
          c = tce.getValue();
          continue;
        }

        if (Objects.equals(i, '>')) {
          index.value.incrementAndGet();
          Main.extendArray(ca, index);
          continue;
        }
        if (Objects.equals(i, '<')) {
          index.value.decrementAndGet();
          continue;
        }
        if (Objects.equals(i, '+')) {
          final char ch = ca.get(index.value.get());
          ca.remove(index.value.get());
          ca.add(index.value.get(), (char) (ch + 1));
          continue;
        }
        if (Objects.equals(i, '-')) {
          final char ch = ca.get(index.value.get());
          ca.remove(index.value.get());
          ca.add(index.value.get(), (char) (ch - 1));
          continue;
        }
        if (Objects.equals(i, '.')) {
          System.out.print(ca.get(index.value.get()));
          continue;
        }
        if (Objects.equals(i, ',')) {
          ca.remove(index.value.get());
          try {
            ca.add(index.value.get(), s.next().toCharArray()[0]);
          } catch (final Throwable e) {
            throw new RuntimeException(e);
          }
          continue;
        }
      } catch (final Throwable exc) {
        throw new BrainfuckException(c + 1, exc);
      }
    }
  }
}
