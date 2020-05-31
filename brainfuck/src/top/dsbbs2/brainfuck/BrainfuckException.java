package top.dsbbs2.brainfuck;

public class BrainfuckException extends RuntimeException
{
  private static final long serialVersionUID = 338638869899042931L;
  public BrainfuckException(int loc)
  {
    super("exception occurred at " + loc);
  }
  public BrainfuckException(int loc,Throwable t)
  {
    super("exception occurred at " + loc, t);
  }
  public BrainfuckException(String s)
  {
    super(s);
  }
  public BrainfuckException(int loc,String s)
  {
    super(s+" at "+loc);
  }
}
