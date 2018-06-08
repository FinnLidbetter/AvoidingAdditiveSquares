import java.util.*;


/**
 * Order encoding description can be found in Tamura, Taga, Kitagawa,
 * Banbara 2009 - Compiling Finite Linear CSP into SAT.
 */
public class OrderEncodingSat {
  static int varCount = 0;
  static int sequenceLength;
  static int maxSquareLength;
  static int minSymbol = 0;
  static int maxSymbol;
  static int alphabetSize;
  static int[] minValidSeqSums = new int[]{0,1,1,3,3,4,4,7,8,8,9,10,11,11,12,15,17,17,19,20,21,22,23,24,25,27};
  static int[] maxValidSeqSums = new int[]{2, 6, 9, 13, 15, 19, 22, 25, 27, 30, 34, 37, 40, 42, 46, 47, 50, 53, 57, 58, 62, 64, 67, 70, 73};
  static int testShift = 0;
  public static void main(String[] args) {
    if (args.length<2) {
      printHelp();
      return;
    }
    try {
      maxSymbol = Integer.parseInt(args[0]);
      alphabetSize = maxSymbol-minSymbol + 1;
      sequenceLength = Integer.parseInt(args[1]);
      maxSquareLength = sequenceLength/2;
      minSymbol+=testShift;
      maxSymbol+=testShift;
      if (args.length>=3) {
        maxSquareLength = Math.min(Integer.parseInt(args[2]), maxSquareLength);
      }
    } catch (Exception e) {
      printHelp();
      return;
    }

    Formula total = new Formula();
    
    // orderVars[i][j] is order variable corresponding to the subword of length i+1 starting at index j
    OrderVariable[][] orderVars = new OrderVariable[maxSquareLength][sequenceLength];
    for (int i=1; i<=maxSquareLength; i++) {
      for (int j=0; j<sequenceLength-i+1; j++) {
        if (j+2*i>sequenceLength && j-i<0) {
          continue;
        }
        // Assign a boolean variable for lo-1
        assign();

        int currBooleanVar = assign();
        if (i==1 && j==0) {
          // Break symmetry. Force first variable to take value in first half (round up) of the alphabet
          int firstLo = minSymbol;
          int firstHi = (minSymbol+maxSymbol)/2  + ((minSymbol+maxSymbol)%2==1 ? 1 : 0);
          orderVars[i-1][j] = new OrderVariable(currBooleanVar, firstLo, firstHi);
        } else if (j==0) {
          orderVars[i-1][j] = new OrderVariable(currBooleanVar, minSequenceSum(i), initialMaxSequenceSum(i)); // WARNING, this line assumes minSymbol=0   !!!
        } else {
          orderVars[i-1][j] = new OrderVariable(currBooleanVar, minSequenceSum(i), maxSequenceSum(i)); // WARNING, this line assumes minSymbol=0   !!!
        }
        total.add(forceOrderVariableBounds(orderVars[i-1][j]));

        // Impose ordering
        total.add(new Clause(-(currBooleanVar-1), currBooleanVar));
        
        for (int k=orderVars[i-1][j].lo+1; k<=orderVars[i-1][j].hi; k++) {
          currBooleanVar = assign();
          
          // Impose ordering
          total.add(new Clause(-(currBooleanVar-1), currBooleanVar));
        }
      }
    }

    for (int i=2; i<=maxSquareLength; i++) {
      for (int j=0; j<sequenceLength-i+1; j++) {
        if (j+2*i>sequenceLength && j-i<0){
          continue;
        }
        OrderVariable leftSummand = orderVars[i-2][j];
        OrderVariable rightSummand = orderVars[0][j+i-1];
        OrderVariable result = orderVars[i-1][j];
        total.add(OrderVariable.sum3Leq(leftSummand, 1, rightSummand, 1, result, -1, 0));
        total.add(OrderVariable.sum3Geq(leftSummand, 1, rightSummand, 1, result, -1, 0));
      }
    }
    for (int i=1; i<=maxSquareLength; i++) {
      for (int j=0; j<sequenceLength-2*i+1; j++) { 
        OrderVariable left = orderVars[i-1][j];
        OrderVariable right = orderVars[i-1][j+i];
        Formula disjunct1 = OrderVariable.sum2Leq(left, 1, right, -1, -1);
        Formula disjunct2 = OrderVariable.sum2Leq(left, -1, right, 1, -1);
        total.add(disjunction(disjunct1,disjunct2));
      }
    }
    System.out.println("p cnf "+varCount+" "+total.clauses.size());
    System.out.println(total);
  }

  static void printHelp() {
    System.err.println("Usage: java OrderEncodingSat MAX_SYMBOL SEQUENCE_LENGTH [MAX_SQUARE_LENGTH]");
  }

  static int assign() {
    varCount++;
    return varCount;
  }

  static Formula disjunction(Formula f1, Formula f2) {
    int d1 = assign();
    int d2 = assign();
    Formula result = new Formula(new Clause(d1,d2));
    for (Clause c : f1.clauses) {
      Clause disjunctClause = new Clause(-d1);
      for (Integer var : c.vars) {
        disjunctClause.add(var);
      }
      result.add(disjunctClause);
    }
    for (Clause c : f2.clauses) {
      Clause disjunctClause = new Clause(-d2);
      for (Integer var : c.vars) {
        disjunctClause.add(var);
      }
      result.add(disjunctClause);
    }
    return result;
  }

  static Formula forceOrderVariableBounds(OrderVariable orderVar) {
    return new Formula(new Clause(-(orderVar.id-1)), new Clause(orderVar.id+(orderVar.hi-orderVar.lo)));
  }
  static int minSequenceSum(int length) {
    if (length-1<minValidSeqSums.length)
      return minValidSeqSums[length-1];
    int sum = 0;
    int div = 2;
    int summand = length/div;
    while (summand>0) {
      sum += summand;
      div *= 2;
      summand = length/div;
    }
    return sum;
  }
  static int initialMaxSequenceSum(int length) {
    if (length-1<maxValidSeqSums.length)
      return maxValidSeqSums[length-1];
    return length*maxSymbol - minSequenceSum(length);
  }

  static int maxSequenceSum(int length) {
    return length*maxSymbol - minSequenceSum(length);
  }
}
class OrderVariable {
  int id; // boolean var id of lowest primitive comparison
  int lo;
  int hi;
  public OrderVariable(int id, int lo, int hi) {
    this.id = id;
    this.lo = lo;
    this.hi = hi;
  }
  
  public static Formula sum2Leq(OrderVariable v1, int a1, OrderVariable v2, int a2, int c) {
    int lo1 = Math.min(a1*v1.lo, a1*v1.hi)-1;
    int hi1 = Math.max(a1*v1.lo, a1*v1.hi);
    int lo2 = Math.min(a2*v2.lo, a2*v2.hi)-1;
    int hi2 = Math.max(a2*v2.lo, a2*v2.hi);
    int diff = c-2+1;
    Formula leqFormula = new Formula();
    for (int i=lo1; i<=hi1; i++) {
      int j = diff-i;
      if (j>hi2 || j<lo2) {
        continue;
      }
      Clause leqClause = new Clause(getDisjunct(v1,a1,i), getDisjunct(v2,a2,j));
      leqFormula.add(leqClause);
    }
    return leqFormula;
  }

  public static Formula sum2Geq(OrderVariable v1, int a1, OrderVariable v2, int a2, int c) {
    return sum2Leq(v1, -a1, v2, -a1, -c);
  }

  // Produces a Formula forcing a1v1 + a2v2 + a3v3 <= c
  public static Formula sum3Leq(OrderVariable v1, int a1, OrderVariable v2, int a2, OrderVariable v3, int a3, int c) {
    int lo1 = Math.min(a1*v1.lo, a1*v1.hi)-1;
    int hi1 = Math.max(a1*v1.lo, a1*v1.hi);
    int lo2 = Math.min(a2*v2.lo, a2*v2.hi)-1;
    int hi2 = Math.max(a2*v2.lo, a2*v2.hi);
    int lo3 = Math.min(a3*v3.lo, a3*v3.hi)-1;
    int hi3 = Math.max(a3*v3.lo, a3*v3.hi);
    int diff = c-3+1;
    Formula leqFormula = new Formula();
    for (int i=lo1; i<=hi1; i++) {
      for (int j=lo2; j<=hi2; j++) {
        int k = diff-i-j;
        if (k>hi3 || k<lo3)
          continue;
        Clause leqClause = new Clause(getDisjunct(v1,a1,i), getDisjunct(v2,a2,j), getDisjunct(v3,a3,k));
        leqFormula.add(leqClause);
      }
    }
    return leqFormula;
  }
  
  public static Formula sum3Geq(OrderVariable v1, int a1, OrderVariable v2, int a2, OrderVariable v3, int a3, int c) {
    return sum3Leq(v1, -a1, v2, -a2, v3, -a3, -c);
  }

  // Produces the appropriate primitive comparison (Prop. 1 in TTKB)
  public static Integer getDisjunct(OrderVariable v, int a, int b) {
    if (a==0) {
      System.err.println("ERROR: zero multiplier passed into a comparison");
      return null;
    }
    if (a>0) {
      int cmp = b/a;
      return v.id+cmp-v.lo;
    } else {
      int cmp = b/a;
      if (a*cmp!=b) {
        cmp++;
      }
      cmp--;
      return -(v.id+cmp-v.lo);
    }
  }
}
class Formula {
  ArrayList<Clause> clauses;
  public Formula() {
    clauses = new ArrayList<Clause>();
  }

  public Formula(Clause ... addClauses) {
    clauses = new ArrayList<Clause>();
    for (Clause c:addClauses) {
      clauses.add(c);
    }
  }
  
  public void add(Clause c) {
    clauses.add(c);
  }

  public void add(Formula f2) {
    for (Clause c:f2.clauses) {
      clauses.add(c);
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Clause c:clauses) {
      sb.append(c.toString());
      sb.append("\n");
    }
    return sb.toString();
  }
}
class Clause {
  ArrayList<Integer> vars;
  public Clause() {
    vars = new ArrayList<Integer>();
  }
  public Clause(int ... addVars) {
    vars = new ArrayList<Integer>();
    for (int v:addVars) {
      vars.add(v);
    }
  }
  public void add(int var) {
    vars.add(var);
  }
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i:vars) {
      sb.append(i);
      sb.append(" ");
    }
    sb.append("0");
    return sb.toString();
  }
}
