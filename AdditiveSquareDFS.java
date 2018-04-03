import java.util.*;
public class AdditiveSquareDFS {
  static int MAX_LEN = 500;
  static int[] ALPHABET;
  static int ALPHABET_SIZE;
  static int[][] ALPHABETS;
  static int MAX_SUM_LENGTH = MAX_LEN/2;
  static Integer[] arr = new Integer[MAX_LEN];
  // sums[i][j] = sum from index i to index j inclusive.
  static Integer[][] sums = new Integer[MAX_LEN][MAX_LEN];
  static int bestMax = 0;
  static int[] bestArr = new int[MAX_LEN];
  public static void main(String[] args) {
    if (args.length<2 || args.length>3) {
      printHelp();
      return;
    }
    if (args[0].equals("-m")) {
      ALPHABET_SIZE = Integer.parseInt(args[1])+1;
      ALPHABET = new int[ALPHABET_SIZE];
      for (int i=0; i<ALPHABET_SIZE; i++) {
        ALPHABET[i] = i;
      }
    } else if (args[0].equals("-s")) {
      String[] symbols = args[1].split(",");
      ALPHABET_SIZE = symbols.length;
      ALPHABET = new int[ALPHABET_SIZE];
      for (int i=0; i<ALPHABET_SIZE; i++) {
        ALPHABET[i] = Integer.parseInt(symbols[i]);
      }
    } else {
      printHelp();
      return;
    }
    if (args.length==3) {
      MAX_SUM_LENGTH = Integer.parseInt(args[2]);
    }
    
    System.out.println(search(0));
  }
  public static int search(int index) {
    int max = 0;
    if (index>bestMax) {
      bestMax=index;
      for (int i=0; i<index; i++) {
        bestArr[i] = arr[i];
      }
      System.out.println(index+": "+Arrays.toString(bestArr));
    }
    for (int i=0; i<ALPHABET_SIZE; i++) {
      int currSymbol = ALPHABET[i];
      if (index>0 && currSymbol==arr[index-1]) {
        continue;
      }
      boolean valid = true;
      for (int squareLen=2; squareLen<=Math.min(((index+1)/2),MAX_SUM_LENGTH); squareLen++) {
        if (sums[index-squareLen+1][index-1]+currSymbol==sums[index-2*squareLen+1][index-squareLen]) {
          valid = false;
          break;
        }
      }
      if (valid) {
        arr[index] = currSymbol;
        for (int j=0; j<index; j++) {
          sums[j][index] = sums[j][index-1] + currSymbol;
        }
        sums[index][index] = currSymbol;
        
        max = Math.max(max, 1 + search(index+1));
        arr[index] = null;
        for (int j=0; j<=index; j++) {
          sums[j][index] = null;
        }
      }
    }
    return max;
  }
  static void printHelp() {
    System.out.println("Usage:\n java Additive2 -m maxSymbol [maxSumLength]\n java Additive2 -s symbol1,symbol2,... [maxSumLength]");
  }
}
