Problem description

In this assignment you will provide several implementations of matrix multiplication algorithms, both serial and parallel versions and analyze their performance. This assignment continues the discussion about matrix  multiplication started in Lecture DenseMatrix.pdf .

First, explore several options for different implementations of square matrix multiplications, resulting from the possible permutations in the order in which multiplication loops i, j, k are performed. 

Implement matrix multiplication variants corresponding to all 6 permutations of (i,j,k): i-j-k, i-k-j, j-i-k, j-k-i, k-i-j, k-j-i. The variants corresponding to i-j-k and i-k-j  have been discussed in Lecture and their implementation given in omp_matrix_mult.c.   Additional reading: why-loops-do-matter-a-story-of-matrix-multiplication

For all 6 algorithms, implement serial version and parallel version using OpenMP (first two variants are already given in omp_matrix_mult.c).   Determine which version has the best serial time and which version the best parallel time.

Implement also the blocked matrix multiplication algorithm as suggested in Lecture DenseMatrix.pdf . Implement the blocked algorithm in serial and parallel version using OpenMP. 

Take into account also the case when  the size of the matrix is not evenly divisible to the block size. 

Perform experiments to determine which is a good block size for your computer. 

Additional reading: Anatomy of High-Performance Many-Threaded Matrix Multiplication.

For all algorithms, work with square matrixes N*N, where size N varies between 1000 and 3000. 

The matrix values can be generated randomly. 

For each algorithm version, validate its result by implementing an automatic comparison with the result produced by the classical i-j-k algorithm version.