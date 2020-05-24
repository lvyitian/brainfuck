
package top.dsbbs2.brainfuck;

import java.util.Objects;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class Main
{
  public static void main(final String[] args)
  {
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

  public static void eval(final String source, final Vector<Character> ca, final Reference<AtomicInteger> index,
      final Scanner s)
  {
    if (Main.countString(source, "[") != Main.countString(source, "]")) {
      throw new RuntimeException("the number of [ doesn't match with the number of ]");
    }
    Main.extendArray(ca, index);
    final char[] arr = source.toCharArray();
    for (int c = 0; c < arr.length; c++) {
      final char i = arr[c];
      if (Objects.equals(i, ' ') || Objects.equals(i, '\n') || Objects.equals(i, '\r')) {
        continue;
      }
      if (Objects.equals(i, '[')) {
        final int tempi = source.lastIndexOf(']');
        final String ws = Main.charArrToString(arr, c + 1, tempi);
        while (ca.get(index.value.get()) != '\0') {
          Main.eval(ws, ca, index, s);
        }
        c = tempi;
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
      throw new RuntimeException("bad token at " + (c + 1));
    }
  }
}
