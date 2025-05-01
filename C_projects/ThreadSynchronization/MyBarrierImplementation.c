#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <stdbool.h>
#include <unistd.h> // for sleep

#define THREAD_COUNT 6 
#define SPECIAL_CASE 2
int NUM_OF_ITERATIONS = 2;

// Family member names
const char *names[] = {"Alice", "Bob", "Chris", "Dave", "Mama Jones", "Papa Jones"}; 
const char *studySubject[] = {"english", "history", "biology", "physics"}; //used to simulate different subjects for kids when they go to school

typedef struct {
    pthread_mutex_t mutex; // use a mutex to protect barrier data(count and total) when a thread wants to read or write to it
    pthread_cond_t cond;   // condition variable is uded for threads to wait on until all threads have reached the barrier(thread is blocked with his current state and waits)

    int thread_count; // counts threads that have reached the barrier
    int thread_total; // total number of threads required to reach the barrier
} my_barrier_t;


// my_barriers for each family activities
my_barrier_t breakfast_barrier1, after_breakfast_barrier1, dinner_barrier1, after_dinner_barrier1, end_of_day_barrier1, math_barrier1, football_barrier1; 

// pthread_barriers for each family activities(have NOT a message to print, so time difference can be noticeble)
pthread_barrier_t breakfast_barrier2, after_breakfast_barrier2, dinner_barrier2, after_dinner_barrier2, end_of_day_barrier2, math_barrier2, football_barrier2; 


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
         while (barrier->thread_count != 0) { //used to avoid spurious wakeups
            pthread_cond_wait(&barrier->cond, &barrier->mutex);
        }
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

void MamaJones_myBarrier() {
    printf("Mama Jones is waking up.\n");
    sleep(1); //simulating waking up duration to group all family members together

    printf("Mama Jones is ready for breakfast.\n");
    my_barrier_wait(&breakfast_barrier1, "\n\t All family members are ready for breakfast !!\n\n");
    printf("Mama Jones is having breakfast.\n");
    my_barrier_wait(&after_breakfast_barrier1, "\n\t All family members finished breakfast !!\n\n");

    printf("Mama Jones starts WORK at this job.\n");
    
    printf("Mama Jones is READY FOR DINNER with the family.\n");
    my_barrier_wait(&dinner_barrier1, "\n\t All family members are ready for dinner !!\n\n");
    
    printf("Mama Jones is having dinner with the family.\n");
    my_barrier_wait(&after_dinner_barrier1, "\n\t All family members finished dinner !!\n\n");

    printf("Mama Jones goes to sleep.\n");
    my_barrier_wait(&end_of_day_barrier1, "\n\t END OF THE DAY \n\n");
}

void PapaJones_myBarrier() {
    printf("Papa Jones is waking up.\n");
    sleep(1);

    printf("Papa Jones is ready for breakfast.\n");
    my_barrier_wait(&breakfast_barrier1, "\n\t All family members are ready for breakfast !!\n\n");
    printf("Papa Jones is having breakfast.\n");
    my_barrier_wait(&after_breakfast_barrier1, "\n\t All family members finished breakfast !!\n\n");

    printf("Papa Jones starts WORK at this job.\n");
    
    printf("Papa Jones is READY FOR DINNER with the family.\n");
    my_barrier_wait(&dinner_barrier1, "\n\t All family members are ready for dinner !!\n\n");
    
    printf("Papa Jones is having dinner with the family.\n");
    my_barrier_wait(&after_dinner_barrier1, "\n\t All family members finished dinner !!\n\n");

    printf("Papa Jones goes to sleep.\n");
    my_barrier_wait(&end_of_day_barrier1, "\n\t END OF THE DAY \n\n");
}

void Alice_myBarrier() {
    printf("Alice is waking up.\n");
    sleep(1);

    printf("Alice is ready for breakfast.\n");
    my_barrier_wait(&breakfast_barrier1, "\n\t All family members are ready for breakfast !!\n\n");
    printf("Alice is having breakfast.\n");
    my_barrier_wait(&after_breakfast_barrier1, "\n\t All family members finished breakfast !!\n\n");

    int i = rand() % 4; //pick a random subject to study at each iteration
    printf("Alice goes to SCHOOL at %s class.\n", studySubject[i]);

    printf("Alice is BACK HOME and ready to learn math with Bob.\n");
    my_barrier_wait(&math_barrier1, "");
    printf("Alice learns math with Bob.\n");

    printf("Alice is READY FOR DINNER with the family.\n");
    my_barrier_wait(&dinner_barrier1, "\n\t All family members are ready for dinner !!\n\n");
    
    printf("Alice is having dinner with the family.\n");
    my_barrier_wait(&after_dinner_barrier1, "\n\t All family members finished dinner !!\n\n");

    printf("Alice goes to sleep.\n");
    my_barrier_wait(&end_of_day_barrier1, "\n\t END OF THE DAY \n\n");
}

void Bob_myBarrier() {
    printf("Bob is waking up.\n");
    sleep(1);

    printf("Bob is ready for breakfast.\n");
    my_barrier_wait(&breakfast_barrier1, "\n\t All family members are ready for breakfast !!\n\n");
    printf("Bob is having breakfast.\n");
    my_barrier_wait(&after_breakfast_barrier1, "\n\t All family members finished breakfast !!\n\n");

    int i = rand() % 4;
    printf("Bob goes to SCHOOL at %s class.\n", studySubject[i]);

    printf("Bob is BACK HOME and ready to learn math with Alice.\n");
    my_barrier_wait(&math_barrier1, "");
    printf("Bob learns math with Alice.\n");

    printf("Bob is READY FOR DINNER with the family.\n");
    my_barrier_wait(&dinner_barrier1, "\n\t All family members are ready for dinner !!\n\n");
    
    printf("Bob is having dinner with the family.\n");
    my_barrier_wait(&after_dinner_barrier1, "\n\t All family members finished dinner !!\n\n");

    printf("Bob goes to sleep.\n");
    my_barrier_wait(&end_of_day_barrier1, "\n\t END OF THE DAY \n\n");
}

void Chris_myBarrier() {
    printf("Chris is waking up.\n");
    sleep(1);

    printf("Chris is ready for breakfast.\n");
    my_barrier_wait(&breakfast_barrier1, "\n\t All family members are ready for breakfast !!\n\n");
    printf("Chris is having breakfast.\n");
    my_barrier_wait(&after_breakfast_barrier1, "\n\t All family members finished breakfast !!\n\n");

    int i = rand() % 4;
    printf("Chris goes to SCHOOL at %s class.\n", studySubject[i]);

    printf("Chris is BACK HOME and ready to play football with Dave.\n");
    my_barrier_wait(&football_barrier1, "");
    printf("Chris plays football with Dave.\n");

    printf("Chris is READY FOR DINNER with the family.\n");
    my_barrier_wait(&dinner_barrier1, "\n\t All family members are ready for dinner !!\n\n");
    
    printf("Chris is having dinner with the family.\n");
    my_barrier_wait(&after_dinner_barrier1, "\n\t All family members finished dinner !!\n\n");

    printf("Chris goes to sleep.\n");
    my_barrier_wait(&end_of_day_barrier1, "\n\t END OF THE DAY \n\n");
}

void Dave_myBarrier() {
    printf("Dave is waking up.\n");
    sleep(1);

    printf("Dave is ready for breakfast.\n");
    my_barrier_wait(&breakfast_barrier1, "\n\t All family members are ready for breakfast !!\n\n");
    printf("Dave is having breakfast.\n");
    my_barrier_wait(&after_breakfast_barrier1, "\n\t All family members finished breakfast !!\n\n");

    int i = rand() % 4;
    printf("Dave goes to SCHOOL at %s class.\n", studySubject[i]);

    printf("Dave is BACK HOME and ready to play football with Chris.\n");
    my_barrier_wait(&football_barrier1, "");
    printf("Dave plays football with Chris.\n");

    printf("Dave is READY FOR DINNER with the family.\n");
    my_barrier_wait(&dinner_barrier1, "\n\t All family members are ready for dinner !!\n\n");
    
    printf("Dave is having dinner with the family.\n");
    my_barrier_wait(&after_dinner_barrier1, "\n\t All family members finished dinner !!\n\n");

    printf("Dave goes to sleep.\n");
    my_barrier_wait(&end_of_day_barrier1, "\n\t END OF THE DAY \n\n");
}


void *family_schedule_myBarrier(void* arg) { 
    const char *threadName = (const char *)arg; 

    for (int day = 1; day <= NUM_OF_ITERATIONS; day++) {
        if (strcmp(threadName, "Mama Jones") == 0) { 
            MamaJones_myBarrier();
        
        } else if (strcmp(threadName, "Papa Jones") == 0) { 
            PapaJones_myBarrier();
        
        } else if (strcmp(threadName, "Alice") == 0) { 
            Alice_myBarrier();
        
        } else if (strcmp(threadName, "Bob") == 0) { 
            Bob_myBarrier();
        
        } else if (strcmp(threadName, "Chris") == 0) { 
            Chris_myBarrier();
        
        } else if (strcmp(threadName, "Dave") == 0) {
            Dave_myBarrier();
        
        } else {
            printf("Invalid thread name!\n"); 
            exit(1); //exit the program if the thread name is invalid
        } 
    }
    return NULL;
}

void MamaJones_pthread() {
    printf("Mama Jones is waking up.\n");
    sleep(1); //simulating waking up duration to group all family members together

    printf("Mama Jones is ready for breakfast.\n");
    pthread_barrier_wait(&breakfast_barrier2);
    printf("Mama Jones is having breakfast.\n");
    pthread_barrier_wait(&after_breakfast_barrier2);

    printf("Mama Jones starts WORK at this job.\n");
    
    printf("Mama Jones is READY FOR DINNER with the family.\n");
    pthread_barrier_wait(&dinner_barrier2);
    
    printf("Mama Jones is having dinner with the family.\n");
    pthread_barrier_wait(&after_dinner_barrier2);

    printf("Mama Jones goes to sleep.\n");
    pthread_barrier_wait(&end_of_day_barrier2);
}

void PapaJones_pthread() {
    printf("Papa Jones is waking up.\n");
    sleep(1);

    printf("Papa Jones is ready for breakfast.\n");
    pthread_barrier_wait(&breakfast_barrier2);
    printf("Papa Jones is having breakfast.\n");
    pthread_barrier_wait(&after_breakfast_barrier2);

    printf("Papa Jones starts WORK at this job.\n");
    
    printf("Papa Jones is READY FOR DINNER with the family.\n");
    pthread_barrier_wait(&dinner_barrier2);
    
    printf("Papa Jones is having dinner with the family.\n");
    pthread_barrier_wait(&after_dinner_barrier2);

    printf("Papa Jones goes to sleep.\n");
    pthread_barrier_wait(&end_of_day_barrier2);
}

void Alice_pthread() {
    printf("Alice is waking up.\n");
    sleep(1);

    printf("Alice is ready for breakfast.\n");
    pthread_barrier_wait(&breakfast_barrier2);
    printf("Alice is having breakfast.\n");
    pthread_barrier_wait(&after_breakfast_barrier2);

    int i = rand() % 4; //pick a random subject to study at each iteration
    printf("Alice goes to SCHOOL at %s class.\n", studySubject[i]);

    printf("Alice is BACK HOME and ready to learn math with Bob.\n");
    pthread_barrier_wait(&math_barrier2);
    printf("Alice learns math with Bob.\n");

    printf("Alice is READY FOR DINNER with the family.\n");
    pthread_barrier_wait(&dinner_barrier2);
    
    printf("Alice is having dinner with the family.\n");
    pthread_barrier_wait(&after_dinner_barrier2);

    printf("Alice goes to sleep.\n");
    pthread_barrier_wait(&end_of_day_barrier2);
}

void Bob_pthread() {
    printf("Bob is waking up.\n");
    sleep(1);

    printf("Bob is ready for breakfast.\n");
    pthread_barrier_wait(&breakfast_barrier2);
    printf("Bob is having breakfast.\n");
    pthread_barrier_wait(&after_breakfast_barrier2);

    int i = rand() % 4;
    printf("Bob goes to SCHOOL at %s class.\n", studySubject[i]);

    printf("Bob is BACK HOME and ready to learn math with Alice.\n");
    pthread_barrier_wait(&math_barrier2);
    printf("Bob learns math with Alice.\n");

    printf("Bob is READY FOR DINNER with the family.\n");
    pthread_barrier_wait(&dinner_barrier2);
    
    printf("Bob is having dinner with the family.\n");
    pthread_barrier_wait(&after_dinner_barrier2);

    printf("Bob goes to sleep.\n");
    pthread_barrier_wait(&end_of_day_barrier2);
}

void Chris_pthread() {
    printf("Chris is waking up.\n");
    sleep(1);

    printf("Chris is ready for breakfast.\n");
    pthread_barrier_wait(&breakfast_barrier2);
    printf("Chris is having breakfast.\n");
    pthread_barrier_wait(&after_breakfast_barrier2);

    int i = rand() % 4;
    printf("Chris goes to SCHOOL at %s class.\n", studySubject[i]);

    printf("Chris is BACK HOME and ready to play football with Dave.\n");
    pthread_barrier_wait(&football_barrier2);
    printf("Chris plays football with Dave.\n");

    printf("Chris is READY FOR DINNER with the family.\n");
    pthread_barrier_wait(&dinner_barrier2);
    
    printf("Chris is having dinner with the family.\n");
    pthread_barrier_wait(&after_dinner_barrier2);

    printf("Chris goes to sleep.\n");
    pthread_barrier_wait(&end_of_day_barrier2);
}

void Dave_pthread() {
    printf("Dave is waking up.\n");
    sleep(1);

    printf("Dave is ready for breakfast.\n");
    pthread_barrier_wait(&breakfast_barrier2);
    printf("Dave is having breakfast.\n");
    pthread_barrier_wait(&after_breakfast_barrier2);

    int i = rand() % 4;
    printf("Dave goes to SCHOOL at %s class.\n", studySubject[i]);

    printf("Dave is BACK HOME and ready to play football with Chris.\n");
    pthread_barrier_wait(&football_barrier2);
    printf("Dave plays football with Chris.\n");

    printf("Dave is READY FOR DINNER with the family.\n");
    pthread_barrier_wait(&dinner_barrier2);
    
    printf("Dave is having dinner with the family.\n");
    pthread_barrier_wait(&after_dinner_barrier2);

    printf("Dave goes to sleep.\n");
    pthread_barrier_wait(&end_of_day_barrier2);
}

void *family_schedule_pthread(void* arg) { 
    const char *threadName = (const char *)arg; 

    for (int day = 1; day <= NUM_OF_ITERATIONS; day++) { 
        if (strcmp(threadName, "Mama Jones") == 0) { 
            MamaJones_pthread();
        
        } else if (strcmp(threadName, "Papa Jones") == 0) { 
            PapaJones_pthread();
        
        } else if (strcmp(threadName, "Alice") == 0) { 
            Alice_pthread();
        
        } else if (strcmp(threadName, "Bob") == 0) { 
            Bob_pthread();
        
        } else if (strcmp(threadName, "Chris") == 0) { 
            Chris_pthread();
        
        } else if (strcmp(threadName, "Dave") == 0) {
            Dave_pthread();
        
        } else {
            printf("Invalid thread name!\n"); 
            exit(1); //exit the program if the thread name is invalid
        } 

    }
    return NULL;
}


void initializate_All_MyBarriers() { //mutex initialization is done inside 'my_barrier_init' function
    my_barrier_init(&breakfast_barrier1, THREAD_COUNT); 
    my_barrier_init(&after_breakfast_barrier1, THREAD_COUNT ); //to make sure that everyone works after all family members have had breakfast
    my_barrier_init(&dinner_barrier1, THREAD_COUNT); 
    my_barrier_init(&after_dinner_barrier1, THREAD_COUNT); 
    my_barrier_init(&end_of_day_barrier1, THREAD_COUNT);
    my_barrier_init(&math_barrier1, SPECIAL_CASE);   // Only Alice and Bob
    my_barrier_init(&football_barrier1, SPECIAL_CASE); // Only Chris and Dave 
}

void destroy_All_MyBarriers() { //mutex and condition variable destruction is done inside 'my_barrier_destroy' function
    my_barrier_destroy(&breakfast_barrier1); 
    my_barrier_destroy(&after_breakfast_barrier1);
    my_barrier_destroy(&dinner_barrier1); 
    my_barrier_destroy(&after_dinner_barrier1);
    my_barrier_destroy(&end_of_day_barrier1);
    my_barrier_destroy(&math_barrier1);
    my_barrier_destroy(&football_barrier1);
}

void initializate_All_PthreadBarriers() { 
    pthread_barrier_init(&breakfast_barrier2, NULL, THREAD_COUNT); 
    pthread_barrier_init(&after_breakfast_barrier2, NULL, THREAD_COUNT);  
    pthread_barrier_init(&dinner_barrier2, NULL, THREAD_COUNT);
    pthread_barrier_init(&after_dinner_barrier2, NULL, THREAD_COUNT );
    pthread_barrier_init(&end_of_day_barrier2, NULL, THREAD_COUNT); 
    pthread_barrier_init(&math_barrier2, NULL, SPECIAL_CASE);
    pthread_barrier_init(&football_barrier2, NULL, SPECIAL_CASE); 
}

void destroy_All_PthreadBarriers() {
    pthread_barrier_destroy(&breakfast_barrier2); 
    pthread_barrier_destroy(&after_breakfast_barrier2);
    pthread_barrier_destroy(&dinner_barrier2); 
    pthread_barrier_destroy(&after_dinner_barrier2);
    pthread_barrier_destroy(&end_of_day_barrier2);
    pthread_barrier_destroy(&math_barrier2);
    pthread_barrier_destroy(&football_barrier2); 
}

void printBanchmarkData(double* resultedTimeValues, int index) { 
    printf("\nBanckmark result: \n"); 
    int thread_count_value = 2;
    for (int i = 0; i < index; i++) { 
        printf("%d) for NUM_OF_ITERATION = %d diffrence between my_barier and pthread_barrier is %f seconds\n", i, thread_count_value, resultedTimeValues[i]);
        thread_count_value += 2;
    }
}

int main() {
    printf("\n \t\t PTHREAD_BARRIER IMPLEMENTATION\n\n");

    // initialize all needed barriers
    initializate_All_PthreadBarriers();

    //variables to measure time difference between 'pthread_barrier' and 'my_barrier'
    struct timespec start2, end2;

    clock_gettime(CLOCK_MONOTONIC, &start2);

    // Create threads for each family member
    pthread_t threads2[THREAD_COUNT ];
    for (int i = 0; i < THREAD_COUNT; i++) {
        pthread_create(&threads2[i], NULL, family_schedule_pthread, (void *)names[i]); 
    }

    // Wait for all threads to finish
    for (int i = 0; i < THREAD_COUNT; i++) {
        pthread_join(threads2[i], NULL);
    }

    clock_gettime(CLOCK_MONOTONIC, &end2); //very important is to measure AFTER all threads have finished their work
    double pthread_barrier_runtime = (end2.tv_sec - start2.tv_sec) + (end2.tv_nsec - start2.tv_nsec) / 1000000000.0;

    // Destroy barriers
    destroy_All_PthreadBarriers();
//-----------------------------------------------------------------------------BELOW IS VERSION WITH (my_barrier)------------------------------------------------------------------------------------------------
    printf("\n\n \t\t MY_BARRIER IMPLEMENTATION\n\n");
    
    // initialize all needed my_barriers 
    initializate_All_MyBarriers();

    //Measure time for 'my_barrier' implementation
    struct timespec start1, end1;

    clock_gettime(CLOCK_MONOTONIC, &start1);

    // Create threads for each family member
    pthread_t threads1[THREAD_COUNT];
    for (int i = 0; i < THREAD_COUNT; i++) {
        pthread_create(&threads1[i], NULL, family_schedule_myBarrier, (void *)names[i]);
    }

    // Wait for all threads to finish
    for (int i = 0; i < THREAD_COUNT; i++) {
        pthread_join(threads1[i], NULL);
    }

    clock_gettime(CLOCK_MONOTONIC, &end1); //very important is to measure AFTER all threads have finished their work
    double my_barrier_runtime = (end1.tv_sec - start1.tv_sec) + (end1.tv_nsec - start1.tv_nsec) / 1000000000.0;

    // Destroy barriers 
    destroy_All_MyBarriers(); 

    //calculate time difference between 'pthread_barrier' and 'my_barrier'
    printf("For %d day/s difference between 'my_barrier' and 'pthread_barrier' is %f seconds \n", NUM_OF_ITERATIONS, pthread_barrier_runtime - my_barrier_runtime);

    return 0;
}
