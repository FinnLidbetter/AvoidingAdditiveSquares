import java.util.*;
import java.io.*;
public class Validator {
  public static void main(String[] args) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String line = br.readLine();
    try {
    String[] s = line.substring(1,line.length()-1).split(",");
    for (int i=0; i<s.length; i++) {
      s[i] = s[i].trim();
    }
    int[] arr = new int[s.length];
    int[] prefixSums = new int[arr.length+1];
    for (int i=0; i<arr.length; i++) {
      arr[i] = Integer.parseInt(s[i]);
      prefixSums[i+1] = prefixSums[i]+arr[i]; 
    }   
    
    int maxSquareLen = arr.length/2;
    boolean valid = true;
    for (int squareLen = 1; squareLen<=maxSquareLen; squareLen++) {
      for (int i=0; i<=arr.length-2*squareLen; i++) {
        int sum1 = prefixSums[i+squareLen]-prefixSums[i];
        int sum2 = prefixSums[i+2*squareLen]-prefixSums[i+squareLen];
        if (sum1==sum2) {
          valid = false;
          System.out.printf("ERROR: sum[%d,%d] = sum[%d,%d]\n",i,i+squareLen-1, i+squareLen, i+2*squareLen-1);
        }
      }
    }
    if (valid)
      System.out.println("Valid");
    else
      System.out.println("Invalid");
    } catch (Exception e) {
      printHelp();
      return;
    }
  }
  static void printHelp() {
    System.out.println("Usage: java Validator < FILE_CONTAINING_SEQUENCE");
    System.out.println("\tFILE_CONTAINING_SEQUENCE is a single line formatted as a comma separated\n\tlist of values with opening and closing parentheses, e.g.,\n\t\t [1,2,3,1]");
  }
}
