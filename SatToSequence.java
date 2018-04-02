import java.util.*;
import java.io.*;

public class SatToSequence {
  public static void main(String[] args) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    if (args.length!=2) {
      printHelp();
    }
    int maxSymbol = Integer.parseInt(args[0]);
    int seqLength = Integer.parseInt(args[1]);
    br.readLine();
    String[] assignments = br.readLine().split(" ");
    int nBits = 1;
    while ((1<<nBits)<=maxSymbol) {
      nBits++;
    }
    int[] sequence = new int[seqLength];
    for (int i=0; i<seqLength; i++) {
      int currentVal = 0;
      for (int j=0; j<nBits; j++) {
        currentVal *= 2;
        if (Integer.parseInt(assignments[nBits*i + nBits-j-1])>0) {
          currentVal++;
        }
      }
      sequence[i] = currentVal;
    }
    System.out.println(Arrays.toString(sequence));
  }
  static void printHelp() {
    System.out.println("Usage:\n java SatToSequence maxSymbol seqLength < SatOutput");
  }
}
