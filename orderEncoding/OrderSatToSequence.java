import java.util.*;
import java.io.*;

public class OrderSatToSequence {
  public static void main(String[] args) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    if (args.length<2) {
      printHelp();
      return;
    }
    int maxSymbol;
    int sequenceLength;
    try {
      maxSymbol = Integer.parseInt(args[0]);
      sequenceLength = Integer.parseInt(args[1]);
    } catch(Exception e) {
      printHelp();
      return;
    }
    String type = br.readLine();
    if (!type.equals("SAT")) {
      System.out.println(type);
      return;
    }
    String[] s = br.readLine().split(" ");
    int index = 0;
    int[] seq = new int[sequenceLength];
    for (int i=0; i<sequenceLength; i++) {
      int val = -1;
      int var = Integer.parseInt(s[index]);
      while (var<0) {
        val++;
        index++;
        var = Integer.parseInt(s[index]);
      }
      seq[i] = val;
      while (var>0) {
        index++;
        var = Integer.parseInt(s[index]);
      }
    }
    System.out.println(Arrays.toString(seq));
    
  }
  static void printHelp() {
    System.out.println("Usage: java OrderSatToSequence MAX_SYMBOL SEQUENCE_LENGTH < DIMACS_SAT_OUTPUT");
  }
}
