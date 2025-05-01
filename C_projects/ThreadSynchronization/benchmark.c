#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <sys/time.h>

#define NUMTHREADS 6  
#define REPEATS 1000     
#define TIMES_RUN_BENCHMARKS 20

double pthread_barrier_runtime = 0.0; //use global variables to store the time for each benchmark run
double my_barrier_runtime = 0.0; 

// simulate work load for each thread
void do_work(int thread_id) {
    float sum = 0.0f;
    for (int i = 0; i < 100; i++) {
        sum += (float)(i * thread_id);
    }
}

typedef struct {
    pthread_mutex_t mutex; // use a mutex to protect barrier data(count and total) when a thread wants to read or write to it
    pthread_cond_t cond;   // condition variable is uded for threads to wait on until all threads have reached the barrier(thread is blocked with his current state and waits)

    int thread_count; // counts threads that have reached the barrier
    int thread_total; // total number of threads required to reach the barrier
} my_barrier_t;

// declare the barriers to can use them in thread_function 
pthread_barrier_t pthread_barrier;
my_barrier_t my_barrier;

//refactoring lots of if's necessary to ensure we implement in right way 'my_barrier'
int check_return(int checkValue) {
    if (checkValue != 0) {
        return checkValue;
    }
    return 0;
}

int my_barrier_init(my_barrier_t *barrier, int count) { 
    barrier->thread_total = count; // set the total number of threads to wait for
    barrier->thread_count = 0;     // initially, no thread has reached the barrier

    // Initialize mutex and condition, returning error if needed
    int checkValue = check_return(pthread_mutex_init(&barrier->mutex, NULL));
    if (checkValue != 0) return checkValue;

    checkValue = check_return(pthread_cond_init(&barrier->cond, NULL));
    if (checkValue != 0) return checkValue;

    return 0; // return 0 if initialization was successful
}

int my_barrier_wait(my_barrier_t *barrier, char* displayMessage) { // included "displayMessage" parameter to have a better output readability and highlight when all threads have reached the barrier
    int checkValue = check_return(pthread_mutex_lock(&barrier->mutex)); // use lock mutex to have exclusive access to 'thread_count' and 'thread_total'
    if (checkValue != 0) return checkValue;
    
    barrier->thread_count++; // increment the count for each thread reaching the barrier

    if (barrier->thread_count == barrier->thread_total) { // if all threads have reached the barrier
        printf("%s", displayMessage); //print only when all threads have reached the barrier, for better output readability
        
        checkValue = check_return(pthread_cond_broadcast(&barrier->cond)); // wake up all threads
        if (checkValue != 0) return checkValue; 
        barrier->thread_count = 0; // reset count just after all threads have been woken up successfully
    
    } else {  
        // wait until all threads have reached the barrier
        pthread_cond_wait(&barrier->cond, &barrier->mutex);
    }
    checkValue = check_return(pthread_mutex_unlock(&barrier->mutex)); // unlock mutex after leaving barrier
    if (checkValue != 0) return checkValue;

    return 0; // return 0 if the operation was successful
}

int my_barrier_destroy(my_barrier_t *barrier) { // destroy all components of the created barrier
    int checkValue = check_return(pthread_mutex_destroy(&barrier->mutex));  
    if (checkValue != 0) return checkValue;
   
    checkValue = check_return(pthread_cond_destroy(&barrier->cond));  
    if (checkValue != 0) return checkValue;
        
    return 0; // return 0 if destruction was successful
}

void *thread_function_pthread(void* arg) {
    for (int i = 0; i < REPEATS; i++) {
        do_work(i); // simulate work load for each thread
        pthread_barrier_wait(&pthread_barrier);
    }
    return NULL;
}

void *thread_function_myBarrier(void* arg) {
    for (int i = 0; i < REPEATS; i++) {
        do_work(i); // simulate work load for each thread
        my_barrier_wait(&my_barrier, "");
    }
    return NULL;
}

// benckmark function to can measure the time for 'pthread_barrier'
void benchmark_pthread() {
    pthread_t threads[NUMTHREADS];
    
    pthread_barrier_init(&pthread_barrier, NULL, NUMTHREADS);

    struct timespec start, end;
    clock_gettime(CLOCK_MONOTONIC, &start);
    
    // create threads
    for (int i = 0; i < NUMTHREADS; i++) {
        pthread_create(&threads[i], NULL, thread_function_pthread, NULL); //barrier is passed as a global variable
    }

    //wait for all threads to finish
    for (int i = 0; i < NUMTHREADS; i++) {
        pthread_join(threads[i], NULL);
    }

    clock_gettime(CLOCK_MONOTONIC, &end); //very important is to measure AFTER all threads have finished their work
    pthread_barrier_runtime += (end.tv_sec - start.tv_sec) + (end.tv_nsec - start.tv_nsec) / 1000000000.0;

    pthread_barrier_destroy(&pthread_barrier);
}

void benchmark_my_barrier() {
    pthread_t threads[NUMTHREADS];
    
    my_barrier_init(&my_barrier, NUMTHREADS);

    struct timespec start, end;
    clock_gettime(CLOCK_MONOTONIC, &start);
    
    for (int i = 0; i < NUMTHREADS; i++) {
        pthread_create(&threads[i], NULL, thread_function_myBarrier, NULL);
    }

    for (int i = 0; i < NUMTHREADS; i++) {
        pthread_join(threads[i], NULL);
    }

    clock_gettime(CLOCK_MONOTONIC, &end); //very important is to measure AFTER all threads have finished their work
    my_barrier_runtime += (end.tv_sec - start.tv_sec) + (end.tv_nsec - start.tv_nsec) / 1000000000.0;

    my_barrier_destroy(&my_barrier);
}

int main() {
    for (int i = 0; i < TIMES_RUN_BENCHMARKS; i++) {
        printf("Benchmark run %d ...\n", i + 1);
        benchmark_pthread();
        benchmark_my_barrier();
    }
    printf ("Average time difference after %d iterations between 'my_barrier' and 'pthread_barrier' is %f seconds\n", 
                TIMES_RUN_BENCHMARKS, (my_barrier_runtime - pthread_barrier_runtime) / TIMES_RUN_BENCHMARKS);
    return 0;
}