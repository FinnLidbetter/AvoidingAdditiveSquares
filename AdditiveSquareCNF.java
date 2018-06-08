import java.util.*;

/**
 * WARNING this is a deprecated version of this file. It is kept here as this file is
 * referenced in the report included in this repository. For the current version see
 * the file: compactEncoding/BitAdderSat.java
 */

public class AdditiveSquareCNF {
  static int varCount = 0;
  static int MAX_SYMBOL_MODE = 0;
  static int VALUE_SET_MODE = 1;
  public static void main(String[] args) {
    
    int mode = 0;
    if (args.length<3 || args.length>4) {
      printHelp();
      return;
    }
    int nVals;
    int maxSymbol;
    int maxSumLength;
    int[] valSet = null;
    if (args[0].equals("-m")) {
      mode = MAX_SYMBOL_MODE;
      try {
        maxSymbol = Integer.parseInt(args[1]);
        nVals = Integer.parseInt(args[2]);
        maxSumLength = nVals/2;
        if (args.length>3) {
          maxSumLength = Integer.parseInt(args[3]);
        }
      } catch (IllegalArgumentException e) {
        printHelp();
        return;
      }
    } else if (args[0].equals("-s")) {
      mode = VALUE_SET_MODE;
      try {
        String[] strVals = args[1].split(",");
        valSet = new int[strVals.length];
        maxSymbol = 0;
        for (int i=0; i<strVals.length; i++) {
          valSet[i] = Integer.parseInt(strVals[i]);
          if (valSet[i]>maxSymbol) {
            maxSymbol = valSet[i];
          }
        }
        nVals = Integer.parseInt(args[2]);
        maxSumLength = nVals/2;
        if (args.length>3) {
          maxSumLength = Integer.parseInt(args[3]);
        }
      } catch (Exception e) {
        printHelp();
        return;
      }
    } else {
      printHelp();
      return;
    }
    int[] nBitsNeeded = new int[maxSumLength];
    for (int i=0; i<maxSumLength; i++) {
      int currentMaxSumValue = (i+1)*maxSymbol;
      int currentBits = 1;
      while ((1<<currentBits)<=currentMaxSumValue) {
        currentBits++;
      }
      nBitsNeeded[i] = currentBits;
    }
    int maxBits = nBitsNeeded[maxSumLength-1];
    Formula total = new Formula();
    int[][][] groups = new int[maxSumLength][nVals][maxBits];
    for (int i=0; i<nVals; i++) {
      for (int j=0; j<nBitsNeeded[0]; j++) {
        groups[0][i][j] = assign();
      }
    }
    for (int i=1; i<maxSumLength; i++) {
      for (int j=0; j<nVals-i; j++) {
        int carryVar = 0;
        for (int k=0; k<nBitsNeeded[i]; k++) {
          AddOut sum = add(groups[i-1][j][k],groups[0][j+i][k],carryVar);
          if (sum.varAssign!=null)
            total.clauses.addAll(sum.varAssign.clauses);
          groups[i][j][k] = sum.sumVar;
          carryVar = sum.carryVar;
        }
      }
    }
    for (int squareLen=1; squareLen<=maxSumLength; squareLen++) {
      for (int i=0; i<=nVals-2*squareLen; i++) {
        Formula f = checkInequality(groups[squareLen-1][i], groups[squareLen-1][i+squareLen], nBitsNeeded[squareLen-1]);
        total.clauses.addAll(f.clauses);
      }
    }
    
    if (mode==MAX_SYMBOL_MODE) {
      for (int i=0; i<nVals; i++) {
        Formula f = makeLessThanOrEqual(groups[0][i], maxSymbol, nBitsNeeded[0]);
        if (f!=null) {
          total.clauses.addAll(f.clauses);
        }
      }
    } else if (mode==VALUE_SET_MODE) {
      for (int i=0; i<nVals; i++) {
        Formula f = makeInValidSet(groups[0][i], valSet, nBitsNeeded[0]);
        if (f!=null) {
          total.clauses.addAll(f.clauses);
        }
      }
    }
    System.out.println("p cnf "+varCount+" "+total.clauses.size());
    System.out.println(total);
  }
  static int assign() {
    varCount++;
    return varCount;
  }

  static void printHelp() {
    System.out.println("Usage:\n BitAdderSat -m maxSymbol numVals [maxBlockLength]\n BitAdderSat -s v1,v2,v3,... numVals [maxBlockLength]");
  }
  static Formula checkInequality(int[] var1, int[] var2, int nBitsNeeded) {
    int nClauses = 4*nBitsNeeded+1;
    int[] dummyVars = new int[2*nBitsNeeded];
    for (int i=0; i<dummyVars.length; i++) {
      dummyVars[i] = assign();
    }
    Clause[] clauses = new Clause[nClauses];
    for (int i=0; i<nBitsNeeded; i++) {
      if (var1[i]==0 || var2[i]==0) {
        System.err.println("PANIC! 0 var in inequality check!");
      }
      clauses[4*i] = makeClause(-dummyVars[2*i], var1[i]);
      clauses[4*i+1] = makeClause(-dummyVars[2*i], -var2[i]);
      clauses[4*i+2] = makeClause(-dummyVars[2*i+1], -var1[i]);
      clauses[4*i+3] = makeClause(-dummyVars[2*i+1], var2[i]);
    }
    clauses[nClauses-1] = makeClause(dummyVars);
    return makeFormula(clauses);
  }

  /**
   * Given the variable indices for the value at some position, give a Formula
   * that ensures that the value is in the valid value set.
   * Assumes that the var[] array has as many indices as necessary for the largest
   * value in the valSet[] array.
   */
  static Formula makeInValidSet(int[] var, int[] valSet, int nBitsNeeded) {
    int[] dummyVars = new int[valSet.length];
    for (int i=0; i<dummyVars.length; i++) {
      dummyVars[i] = assign();
    }
    Clause[] clauses = new Clause[valSet.length*nBitsNeeded + 1];
    for (int i=0; i<valSet.length; i++) {
      int val = valSet[i];
      for (int j=0; j<nBitsNeeded; j++) {
        if ((val & (1<<j))!=0) {
          clauses[i*nBitsNeeded + j] = makeClause(-dummyVars[i], var[j]);
        } else {
          clauses[i*nBitsNeeded + j] = makeClause(-dummyVars[i], -var[j]);
        }
      }
    }
    clauses[clauses.length-1] = makeClause(dummyVars);
    return makeFormula(clauses);
  }

  /**
   * Takes the indices of the bits in some variable and a maximum value to produce
   * a Formula that ensures that the value is strictly less than max.
   * Assumes that the var[] has as many indices as necessary for the largest value
   * strictly smaller than max.
   */
  static Formula makeLessThanOrEqual(int[] var, int max, int nBitsNeeded) {
    if ((1<<nBitsNeeded)-1==max) {
      return null;
    }
    int nZeroes = 0;
    int maxCopy = max;
    while (maxCopy>0) {
      if (maxCopy%2==0) {
        nZeroes++;
      }
      maxCopy/=2;
    }
    int mask = 1<<(nBitsNeeded-1);
    int shift = nBitsNeeded-2;
    Clause[] clauses = new Clause[nZeroes];
    int cIndex = 0;
    int maskCount = 1;
    while (shift>=0) {
      while (shift>=0 && (max&(1<<shift))!=0) {
        mask += 1<<shift;
        maskCount++;
        shift--;
      }
      if (shift<0)
        continue;
      mask += 1<<shift;
      maskCount++;
      int badVarIndex = 0;
      int[] badVars = new int[maskCount];
      for (int i=0; i<nBitsNeeded; i++) {
        if ((mask&(1<<i))!=0) {
          badVars[badVarIndex] = -var[i];
          badVarIndex++;
        }
      }
      clauses[cIndex] = makeClause(badVars);
      cIndex++;
      mask -= 1<<shift;
      maskCount--;
      shift--;
    }
    return makeFormula(clauses);
  }

  static AddOut add(int var1, int var2, int inCarryVar) {
    int sumVar;
    int outCarryVar;
    int[] inVars = getNonZeroes(var1,var2,inCarryVar);
    int inVarsCount = inVars.length;
    Formula outFormula;
    if (inVarsCount==0) {
      System.err.println("PANIC! Summing all zeroes!");
      return null;
    }
    else if (inVarsCount==1) {
      sumVar = inVars[0];
      outCarryVar = 0;
      outFormula = null;
    } else if (inVarsCount==2) {
      sumVar = assign();
      outCarryVar = assign();
      Clause c1 = makeClause(outCarryVar, -inVars[0], -inVars[1]);
      Clause c2 = makeClause(-outCarryVar, inVars[0]);
      Clause c3 = makeClause(-outCarryVar, inVars[1]);

      Clause z1 = makeClause(sumVar, -inVars[0], inVars[1]);
      Clause z2 = makeClause(sumVar, inVars[0], -inVars[1]);
      Clause z3 = makeClause(-sumVar, inVars[0], inVars[1]);
      Clause z4 = makeClause(-sumVar, -inVars[0], -inVars[1]);
      outFormula = makeFormula(c1,c2,c3,z1,z2,z3,z4);
    } else {
      sumVar = assign();
      outCarryVar = assign();
      Clause c1 = makeClause(outCarryVar, -inVars[0], -inVars[1]);
      Clause c2 = makeClause(outCarryVar, -inVars[0], -inVars[2]);
      Clause c3 = makeClause(outCarryVar, -inVars[1], -inVars[2]);
      Clause c4 = makeClause(-outCarryVar, inVars[0], inVars[1]);
      Clause c5 = makeClause(-outCarryVar, inVars[0], inVars[2]);
      Clause c6 = makeClause(-outCarryVar, inVars[1], inVars[2]);

      Clause z1 = makeClause(sumVar, -inVars[0], inVars[1], inVars[2]);
      Clause z2 = makeClause(sumVar, inVars[0], -inVars[1], inVars[2]);
      Clause z3 = makeClause(sumVar, inVars[0], inVars[1], -inVars[2]);
      Clause z4 = makeClause(sumVar, -inVars[0], -inVars[1], -inVars[2]);
      Clause z5 = makeClause(-sumVar, inVars[0], inVars[1], inVars[2]);
      Clause z6 = makeClause(-sumVar, inVars[0], -inVars[1], -inVars[2]);
      Clause z7 = makeClause(-sumVar, -inVars[0], inVars[1], -inVars[2]);
      Clause z8 = makeClause(-sumVar, -inVars[0], -inVars[1], inVars[2]);
      outFormula = makeFormula(c1,c2,c3,c4,c5,c6,z1,z2,z3,z4,z5,z6,z7,z8);
    }

    return new AddOut(sumVar, outCarryVar, outFormula);
  }
  static int countNonZero(int ... vars) {
    int count = 0;
    for (int v:vars) {
      if (v!=0)
        count++;
    }
    return count;
  }
  static int[] getNonZeroes(int ... vars) {
    int[] nonZeroes = new int[countNonZero(vars)];
    int index = 0;
    for (int v:vars) {
      if (v!=0) {
        nonZeroes[index] = v;
        index++;
      }
    }
    return nonZeroes;
  }

  static Clause makeClause(int ... addVars) {
    Clause c = new Clause();
    for (int v:addVars) {
      c.vars.add(v);
    }
    return c;
  }
  static Formula makeFormula(Clause ... addClauses) {
    Formula f = new Formula();
    for (Clause c:addClauses) {
      f.clauses.add(c);
    }
    return f;
  }
}
class AddOut {
  int sumVar;
  int carryVar;
  Formula varAssign;
  public AddOut(int sv, int cv, Formula form) {
    sumVar = sv;
    carryVar = cv;
    varAssign = form;
  }
}
class Formula {
  ArrayList<Clause> clauses;
  public Formula() {
    clauses = new ArrayList<Clause>();
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
