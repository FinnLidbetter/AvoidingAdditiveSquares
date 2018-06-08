import java.util.*;

public class SparseEncodingSat {
  static int varCount = 0;
  static int sequenceLength;
  static int maxSquareLength;
  static int minSymbol = 0;
  static int maxSymbol;
  static int alphabetSize;
  static int testShift = 0;
  static int[] minValidSeqSums = new int[]{0,1,1,3,3,4,4,7,8,8,9,10,11,11,12,15,17,17,19,20,21,22,23,24,25,27};
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
    
    // sparseVars[i][j] is sparse variable corresponding to the subword of length i+1 starting at index j
    SparseVariable[][] sparseVars = new SparseVariable[maxSquareLength][sequenceLength];
    for (int i=1; i<=maxSquareLength; i++) {
      for (int j=0; j<sequenceLength-i+1; j++) {
        if (j+2*i>sequenceLength && j-i<0) {
          continue;
        }
        int currBooleanVar = assign();
        sparseVars[i-1][j] = new SparseVariable(currBooleanVar, i*minSymbol + minSequenceSum(i), i*maxSymbol - minSequenceSum(i));
        for (int k=sparseVars[i-1][j].lo+1; k<=sparseVars[i-1][j].hi; k++) {
          currBooleanVar = assign();
        }
        total.add(forceSingleValue(sparseVars[i-1][j]));
      }
    }

    for (int i=2; i<=maxSquareLength; i++) {
      for (int j=0; j<sequenceLength-i+1; j++) {
        if (j+2*i>sequenceLength && j-i<0) {
          continue;
        }
        SparseVariable leftSummand = sparseVars[i-2][j];
        SparseVariable rightSummand = sparseVars[0][j+i-1];
        SparseVariable result = sparseVars[i-1][j];
        total.add(forceAddition(leftSummand, rightSummand, result));
      }
    }
    for (int i=1; i<=maxSquareLength; i++) {
      for (int j=0; j<sequenceLength-2*i+1; j++) { 
        SparseVariable left = sparseVars[i-1][j];
        SparseVariable right = sparseVars[i-1][j+i];
        total.add(forceInequality(left,right));
      }
    }
    System.out.println("p cnf "+varCount+" "+total.clauses.size());
    System.out.println(total);
  }

  static Formula forceSingleValue(SparseVariable var) {
    Formula singleValueFormula = new Formula();
    for (int i=var.lo; i<=var.hi; i++) {
      for (int j=i+1; j<=var.hi; j++) {
        singleValueFormula.add(new Clause(-(var.id+(i-var.lo)), -(var.id+(j-var.lo))));
      }
    }
    Clause atLeastOneValue = new Clause();
    for (int i=var.lo; i<=var.hi; i++) {
      atLeastOneValue.add(var.id+(i-var.lo));
    }
    singleValueFormula.add(atLeastOneValue);
    return singleValueFormula;
  }

  static Formula forceInequality(SparseVariable v1, SparseVariable v2) {
    Formula neqFormula = new Formula();
    if (v1.hi!=v2.hi || v1.lo!=v2.lo) {
      System.err.println("ERROR forcing inequality on variables with different ranges!");
    }
    for (int i=Math.max(v1.lo, v2.lo); i<=Math.min(v1.hi, v2.hi); i++) {
      neqFormula.add(new Clause(-(v1.id+(i-v1.lo)), -(v2.id+(i-v2.lo))));
    }
    return neqFormula;
  }

  static Formula forceAddition(SparseVariable summand1, SparseVariable summand2, SparseVariable result) {
    Formula additionFormula = new Formula();
    for (int i=summand1.lo; i<=summand1.hi; i++) {
      for (int j=summand2.lo; j<=summand2.hi; j++) {
        int sum = i+j;
        if (result.lo<=sum && sum<=result.hi) {
          additionFormula.add(new Clause(-(summand1.id+(i-summand1.lo)), -(summand2.id+(j-summand2.lo)), result.id+(sum-result.lo)));
        }
      }
    }
    return additionFormula;
  }

  static void printHelp() {
    System.err.println("Usage: java SparseEncodingSat MAX_SYMBOL SEQUENCE_LENGTH [MAX_SQUARE_LENGTH]");
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

  static int minSequenceSum(int length) {
    if (length-1<minValidSeqSums.length) {
      return minValidSeqSums[length-1];
    }
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
}
class SparseVariable {
  int id; // boolean variable id of lowest value that this SparseVariable can attain
  int lo;
  int hi;
  public SparseVariable(int id, int lo, int hi) {
    this.id = id;
    this.lo = lo;
    this.hi = hi;
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
