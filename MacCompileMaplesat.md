# Compiling maplesat on MacOS

To compile maplesat, mapleCOMSPS_LRB, and mapleCOMSPS_CHB found at https://sites.google.com/a/gsd.uwaterloo.ca/maplesat/maplesat on a machine running Mac OS a number of changes need to be made. The changes to fix issues with minisat are given in the following 3 git commit records. Each gives a few lines that need to be changed to allow compilation on Mac:

https://github.com/u-u-h/minisat/commit/e768238f8ecbbeb88342ec0332682ca8413a88f9
https://github.com/niklasso/minisat/commit/9bd874980a7e5d65cecaba4edeb7127a41050ed1.patch?full_index=1
https://github.com/niklasso/minisat/commit/cfae87323839064832c8b3608bf595548dd1a1f3.patch?full_index=1

The modified System.cc file is included in this repositoty (changes corresponding to the two latter git commit records above).
I also had to make the following change to the core/Solver.h file for MapleCOMSPS_CHB and for MapleCOMPSPS_LRB:

Add the following lines to core/Solver.h:
````
#ifdef __APPLE__
  #define fwrite_unlocked fwrite
  #define fflush_unlocked fflush
#endif
````
Note that minisat can be compiled and installed using the Homebrew package manager on Mac OS. The instructions described above are what the Homebrew formula does to install minisat.
