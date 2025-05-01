#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h> //used for opendir and readdir
#include <sys/types.h> //used for EnrtryMetadata types used in struct
#include <sys/stat.h> //used for 'struct stat'
#include <unistd.h>
#include <sys/wait.h> //used for waitpid and wait 
#include <time.h>
#include <libgen.h>

#define MAX_PATH_LEN 1024 
#define SHELL_SCRIPT_NAME "CheckPossibleMalitiousContent.sh"  //before using this script, you need to give it execution permissions with 'chmod +x verify_for_malicious.sh'

int isDirectory(const char *dirPath) {
    struct stat st; 
    if (stat(dirPath, &st) == 0 && S_ISDIR(st.st_mode))
        return 1;
    return 0;
}

typedef struct {
    char name[MAX_PATH_LEN];
    time_t last_modified; 
    time_t last_acces;
    off_t size;
} EntryMetadata;

EntryMetadata getEntryMetadata(const char *dirPath) {
    EntryMetadata metadata;
    struct stat st;

    strcpy(metadata.name, basename((char *)dirPath)); //use only name of file/directory for naming snapshot file
    if (stat(dirPath, &st) == 0) {
        metadata.last_modified = st.st_mtime; 
        metadata.last_acces = st.st_atime;
        metadata.size = st.st_size;
    
    } else { //case when stat fails
        metadata.last_modified = -1; 
        metadata.last_acces = -1;
        metadata.size = -1;
    }
    return metadata;
}

char *createSnapshotDir(char *dirPath, const char *wantedName) {
    char *snapshotDirPath = NULL;
    if ((snapshotDirPath = calloc(1, MAX_PATH_LEN)) == NULL) {
        fprintf(stderr, "Error: Unable to allocate memory for %s path!\n", wantedName);
        return NULL;
    }

    int lastCharIndex = strlen(dirPath) - 1;
    if (dirPath[lastCharIndex] == '/') //remove last '/' if exists to have all paths in same format
        dirPath[lastCharIndex] = '\0';

    snprintf(snapshotDirPath, MAX_PATH_LEN, "%s/%s", dirPath, wantedName); //create path for snapshot directory

    if (!isDirectory(snapshotDirPath)) { //check if directory already exists
        if (mkdir(snapshotDirPath, 0777) != 0) { //create new directory with all permissions
            free(snapshotDirPath); //free memory allocated in case of error
            fprintf(stderr,"Error: Unable to create %s, possible directory already created for path: '%s'\n", wantedName, dirPath);
            return NULL;
        }
    }
    return snapshotDirPath;
} 

void checkFilePermissions(const char *filePath, char *ownerPermissions, char *groupPermissions, char *otherPermissions) {
    struct stat st;
    if (stat(filePath, &st) != 0) {
        fprintf(stderr, "Error: Unable to get file status for path: '%s'\n", filePath);
        return;
    }

    //owner permissions
    if (st.st_mode & S_IRUSR) *ownerPermissions = 'r'; else *ownerPermissions = '-';
    if (st.st_mode & S_IWUSR) *(ownerPermissions + 1) = 'w'; else *(ownerPermissions + 1) = '-';
    if (st.st_mode & S_IXUSR) *(ownerPermissions + 2) = 'x'; else *(ownerPermissions + 2) = '-';
    *(ownerPermissions + 3) = '\0';

    //group permissions
    if (st.st_mode & S_IRGRP) *groupPermissions = 'r'; else *groupPermissions = '-';
    if (st.st_mode & S_IWGRP) *(groupPermissions + 1) = 'w'; else *(groupPermissions + 1) = '-';
    if (st.st_mode & S_IXGRP) *(groupPermissions + 2) = 'x'; else *(groupPermissions + 2) = '-';
    *(groupPermissions + 3) = '\0';


    //others permissions
    if (st.st_mode & S_IROTH) *otherPermissions = 'r'; else *otherPermissions = '-';
    if (st.st_mode & S_IWOTH) *(otherPermissions + 1) = 'w'; else *(otherPermissions + 1) = '-';
    if (st.st_mode & S_IXOTH) *(otherPermissions + 2) = 'x'; else *(otherPermissions + 2) = '-';
    *(otherPermissions + 3) = '\0';
}  

int fileExistsIn_IzolateDir(const char *fileName, const char *directoryPath) {
    if (fileName == NULL || directoryPath == NULL) {
        fprintf(stderr, "Error: File name or directory path is NULL!\n");
        return 0;
    }

    char filePath[MAX_PATH_LEN];
    snprintf(filePath, MAX_PATH_LEN, "%s/%s", directoryPath, fileName);

    if ( (chmod(filePath, 0444)) == -1) { //set initial to readable to can check if file exists
        fprintf(stderr, "Error: Unable to change permissions to readable for suspect file: '%s' in izolate directory\n", filePath);
    } 

    FILE *file = fopen(filePath, "r");
    if (file != NULL) {
        fclose(file); 
        if ( (chmod(filePath, 0000)) == -1) {
            fprintf(stderr, "Error: Unable to set old permissions for suspect file: '%s' in izolate directory\n", filePath);
        }
        return 1; //case when file exists
    } 

    if ( (chmod(filePath, 0000)) == -1) {
        fprintf(stderr, "Error: Unable to set old permissions for suspect file: '%s' in izolate directory\n", filePath);
    }
    return 0; //case when file does not exist
}

void moveFileTo_IsolatedDir(const char *filePath, const char *izolateDirPath) {
    if (filePath == NULL || izolateDirPath == NULL) {
        fprintf(stderr, "Error: File path/isolated_directory path is NULL!\n");
        return;
    }

    char *filename = basename((char *)filePath); 
    char destPath[MAX_PATH_LEN]; 
    snprintf(destPath, MAX_PATH_LEN, "%s/%s", izolateDirPath, filename);

    if (fileExistsIn_IzolateDir(filename, izolateDirPath)) { 
        srand(time(NULL)); //if we have already a file with same name,
        char randomStr[2]; //we generate a random number to create a new unique name
        randomStr[0] = '0' + rand() % 10; 
        randomStr[1] = '\0';

        int size = strlen(filename) + 4; //used 'size + 4' to add space for characters: '(', ')', '\0' and random number
        char uniqName[size]; 
        uniqName[0] = '\0'; 
        strcat(uniqName, "("); 
        strcat(uniqName, randomStr);
        strcat(uniqName, ")");
        strcat(uniqName, filename); 

        snprintf(destPath, MAX_PATH_LEN, "%s/%s", izolateDirPath, uniqName);
    } 

    if (rename(filePath, destPath) != 0) { //move file to isolated directory
        fprintf(stderr, "Error: Failed to move file '%s' to isolated directory: '%s'.\n", filePath, izolateDirPath);
    } else {
        printf("File '%s' moved to isolated directory: '%s'\n", filePath, izolateDirPath);
    }
}

void printFilePermissions(const char *filePath) { 
    if (filePath == NULL) {
        fprintf(stderr, "Error: Recieved file path in 'printFilePermissions' function is NULL!\n");
        return;
    }
    char ownerPermissions[4], groupPermissions[4], otherPermissions[4];
    checkFilePermissions(filePath, ownerPermissions, groupPermissions, otherPermissions);

    printf("\nFile permissions for file path: '%s':\n", filePath);
    printf("Owner: %s\n", ownerPermissions);
    printf("Group: %s\n", groupPermissions);
    printf("Others: %s\n\n", otherPermissions);
}

void createSnapshot(char *mainDirPath, int *totalUnsafeFiles, const char *entryName, char *snapshotDirPath, char *izolateDirPath, int depth) {
    if (snapshotDirPath == NULL || mainDirPath == NULL) {
        fprintf(stderr, "Error: Received snapshot_directory/main_directory path in 'createSnapshot' is NULL!\n");
        return;
    }
    
    if (depth < 0)
        return;  //base case for recursion

    char snapshotPath[MAX_PATH_LEN];
    snprintf(snapshotPath, MAX_PATH_LEN, "%s/%s_snapshot.txt", snapshotDirPath, entryName); //create file for each entry in snapshot dir

    FILE *snapshotFile; // open that file with previous created path
    if ((snapshotFile = fopen(snapshotPath, "w")) == NULL) {
        fprintf(stderr, "Error: Unable to create snapshot file for tracked directory: '%s'!\n", mainDirPath);
        return;
    }
    fprintf(snapshotFile, "Snapshot of directory: %s\n\n", mainDirPath); //head_line for each snapshot file

    DIR *dir = opendir(mainDirPath); //open tracked dir
    if (dir == NULL) {
        fprintf(stderr, "Error: Unable to open tracked directory: '%s'\n", mainDirPath);
        fclose(snapshotFile);
        return;
    }

    struct dirent *entry;
    int pipefd[2];
    if (pipe(pipefd) == -1) {
        fprintf(stderr, "Error: Pipe creation failed.\n");
        return;
    }

    int unsafeFiles = 0; // Contor pentru numărul de fișiere nesigure găsite în directorul curent
    while ((entry = readdir(dir)) != NULL) { 
        unsafeFiles = 0; //reset number of unsafe files for each iteration

        if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0)
            continue;

        char entryPath[MAX_PATH_LEN]; //create path for each instance of main tracked dir
        snprintf(entryPath, MAX_PATH_LEN, "%s/%s", mainDirPath, entry->d_name);

        if (strcmp(entryPath, snapshotDirPath) == 0) //if snapshot dir is placed in tracked dir, we ignore it
            continue;

        if (!isDirectory(entryPath)) {
            char ownerPermissions[4], groupPermissions[4], otherPermissions[4]; //find permissions for each entry
            checkFilePermissions(entryPath, ownerPermissions, groupPermissions, otherPermissions);

            if (strcmp(ownerPermissions, "---") == 0 && strcmp(groupPermissions, "---") == 0 && strcmp(otherPermissions, "---") == 0) {//file hasn't any permmissions, we check it
                pid_t PID = fork();  //create a child process to run shell script for verify this suspect file

                if (PID == -1) {
                    fprintf(stderr, "Error: Fork failed for input path: '%s'\n", entryPath);

                } else if (PID == 0) { //child process created successfully
                    char scriptPath[MAX_PATH_LEN];
                    snprintf(scriptPath, MAX_PATH_LEN, "./%s", SHELL_SCRIPT_NAME); //create path to execute shell script

                    if ((chmod(entryPath, 0444)) == -1) {  //give file temporar permission just enough shell can verify his content
                        fprintf(stderr, "Error: Unable to change permissions to readable for suspect file: '%s'\n", entryPath);
                        exit(1);  // Terminate child process with error code 1
                    }

                    if ((chmod(scriptPath, 0777)) == -1) { //ensure that shell script has all permissions to execute
                        fprintf(stderr, "Error: Unable to set all permissions for shell script: '%s'\n", scriptPath);
                        exit(1); // Terminate child process with error code 1
                    }

                    close(pipefd[0]); // close reading head of pipe
                    // redirect stdout to writing head of pipe to can collect all data for ended process
                    dup2(pipefd[1], STDOUT_FILENO);

                    // create a command to execute shell script and contains entry path as argument-shell scripts need entire path to evaluate file status  
                    char runScriptCommand[2 * MAX_PATH_LEN];
                    snprintf(runScriptCommand, 2* MAX_PATH_LEN, "%s %s", scriptPath, entryPath);

                    FILE *scriptOutput = popen(runScriptCommand, "r"); //with popen we do 3 actions: 1. open script path as any file; 2. execute script; 3. store output of script in this file
                    if (scriptOutput == NULL) {
                        fprintf(stderr, "Error: Unable to execute shell script for suspect file: '%s'. Possible case: shell script may not have execution permission.\n", entryPath);
                        exit(1); // Terminate child process with error code 1
                    }
                    
                    char buffer[1024]; //auxiliar string to can write infos in pipe
                    size_t bytesRead;
                    while ((bytesRead = fread(buffer, 1, sizeof(buffer), scriptOutput)) > 0) { //read in buffer shell output
                        if (write(pipefd[1], buffer, bytesRead) == -1) { //write the result of script in pipe
                            fprintf(stderr, "Error: Failed to write script output to pipe for suspect file: '%s'\n", entryPath);
                            exit(1); // Terminate child process with error code 1
                        }
                    }

                    pclose(scriptOutput); // close the file opened with popen
                    close(pipefd[1]); // close writing head of pipe after we are done to write

                    exit(0); // close child process 0 errors
               
                } else { //parent process
                    close(pipefd[1]); // close writing head of pipe to can read safe

                    char buffer[MAX_PATH_LEN]; //result of suspect file examination, comunicate by pipe
                    int bytesRead = read(pipefd[0], buffer, sizeof(buffer)); //read from pipe
                   
                    if (bytesRead > 0) { //check if we read something 
                        buffer[bytesRead] = '\0'; 
                        if (strstr(buffer, entryPath) != NULL) { //for malicious files script output is his file path, so we move file to izolate dir
                            if ((chmod(entryPath, 0000)) == -1) { //set file to old permissions
                                fprintf(stderr, "Error: Unable to set file permissions to 000 for suspect file: '%s'\n", entryPath);
                                exit(1); // Terminate child process with error code 1
                            }
                            moveFileTo_IsolatedDir(entryPath, izolateDirPath);  
                            unsafeFiles++; // update number of unsafe files
                        }
                    }
                    close(pipefd[0]); // close reading head of pipe for parent process
                }
            }
        }
        if (unsafeFiles) { 
            *totalUnsafeFiles = unsafeFiles; // update number of all potential unsafe files
             continue; //if we have unsafe files in tracked directory, we move them and ignore them for snapshot file 
        }

        EntryMetadata metadata = getEntryMetadata(entryPath); //collect data for entry to can print infos in snapshot file
        char last_modified[MAX_PATH_LEN], last_acces[MAX_PATH_LEN];

        strftime(last_modified, sizeof(last_modified), "%m-%d-%Y %H:%M:%S", localtime(&metadata.last_modified)); //formatated info 
        strftime(last_acces, sizeof(last_acces), "%m-%d-%Y %H:%M:%S", localtime(&metadata.last_acces)); //formatated info

        fprintf(snapshotFile, "Name: %s,\t\tLast Modified: %s,\t\tLast Acces: %s,\t\tSize: %.2lf KB\n",
                metadata.name, last_modified, last_acces, (double)metadata.size / 1024); //write in snapshot file

        if (isDirectory(entryPath) && strcmp(entryPath, izolateDirPath) != 0) { //case when izolated dir is placed in tracked dir, we ignore it
            char *entryName = basename((char *)entryPath);
            createSnapshot(entryPath, totalUnsafeFiles, entryName, snapshotDirPath, izolateDirPath, depth - 1);
        }
    }
    printf("Snapshot for '%s' was created successfully! \n", entryName);

    closedir(dir);
    fclose(snapshotFile);

}

int main(int argc, char *argv[]) {
    if (argc > 12) {
        fprintf(stderr, "Invalid number of arguments! Program can receive MAX 10 directory paths!\n");
        return 1;
    }

    char* outputDirPath = NULL;  
    char* izolateDirPath = NULL;
    int indexOutputDir = -1; //save indexes to ignore aruments recieved as output dir or izolated dir
    int indexIzolateDir = -1;

    for (int i=1; i < argc; i++) { //search needed directors
        if (strcmp(argv[i], "-o") == 0) { 
            if(isDirectory(argv[i+1])) {
                outputDirPath = argv[i+1]; 
                indexOutputDir = i+1; 
            }
        } else if (strcmp(argv[i], "-s") == 0) { 
            if (isDirectory(argv[i+1])) { 
                izolateDirPath = argv[i+1]; 
                indexIzolateDir = i+1;
            }
        }
    }
    if (outputDirPath == NULL) { 
        fprintf(stderr, "Error: Output directory path is NOT provided or is NOT valid!\n");
        return 1;
    } else if (izolateDirPath == NULL) { 
        fprintf(stderr, "Error: Isolated_space directory path is NOT provided or is NOT valid!\n");
        return 1;
    }

    char* snapshotDirPath = createSnapshotDir(outputDirPath, "Snapshots_Folder"); 

    int maxProcesses = argc - 5;  //we can create ax that because we ignore: './executable'; '-o'; '-s'; 'izolate_dir'; 'output_dir'
    int actualCreatedProcesses = 0; //can be less(used as size of pids_array)
    pid_t childProcesses_Pids[maxProcesses];  

    int* unsafeFiles_EachProcess; //save all unsafe files counts for each process
    if ((unsafeFiles_EachProcess = calloc(maxProcesses, sizeof(int))) == NULL) { 
        fprintf(stderr, "Error: Unable to allocate memory for unsafe files count for each process!\n");
        return 1;
    }

     
    int pipefd[2]; //create pipe for communicate unsafeFiles founded in each process
    if (pipe(pipefd) == -1) {
        fprintf(stderr, "Error: Pipe creation failed.\n");
        return 1;
    }

    for (int i=1; i < argc; i++) { 
        if (isDirectory(argv[i]) && i != indexOutputDir && i != indexIzolateDir) {
            pid_t PID = fork(); //create a new process for each valid recieved argument
            int unsafeFiles = 0;  //counts for each directory
            
            if (PID == -1) {
                fprintf(stderr, "Error: Fork failed for input path: '%s'\n", argv[i]);
            
            } else if (PID == 0) { 
                char *entryName = basename((char *)argv[i]); 
                createSnapshot(argv[i], &unsafeFiles, entryName, snapshotDirPath, izolateDirPath, 1); 
                close(pipefd[0]); // Închideți capătul de citire al pipe-ului în procesul copil
                write(pipefd[1], &unsafeFiles, sizeof(int)); // Scrieți numărul de fișiere nesigure în pipe
                close(pipefd[1]); // Închideți capătul de scriere al pipe-ului în procesul copil
                exit(0); // Terminăm procesul copil cu succes
            } 
            
            childProcesses_Pids[actualCreatedProcesses] = PID;  
            actualCreatedProcesses++;
            
        } else if (!isDirectory(argv[i]) && strcmp(argv[i], "-o") != 0 && strcmp(argv[i], "-s")) { 
            fprintf(stderr, "Error: Directory with path: '%s' does NOT exist!\n", argv[i]);
        }
    }

    int totalUnsafeFiles = 0; //count all finded unsafe files for all processes
    close(pipefd[1]); // Închideți capătul de scriere al pipe-ului în procesul părinte
    for (int i = 0; i < actualCreatedProcesses; i++) {
        int status;
        pid_t endedProcess_Pid = waitpid(childProcesses_Pids[i], &status, 0); 

        read(pipefd[0], &unsafeFiles_EachProcess[i], sizeof(int)); // Citiți numărul de fișiere nesigure din pipe pt fiecare proces

        if (WIFEXITED(status)) {
            printf("Child Process %d with PID %d finished with CODE %d and %d unsafe files.\n\n", i+1, endedProcess_Pid, WEXITSTATUS(status), unsafeFiles_EachProcess[i]);
            totalUnsafeFiles += unsafeFiles_EachProcess[i]; // Adăugăm numărul de fișiere nesigure returnat de acest proces la totalul general
        } else {
            printf("Child Process %d with PID %d abnormally finished!\n\n", i+1, endedProcess_Pid); 
            exit(1); //finish child process abnormally
        }
    }
    close(pipefd[0]); // Închideți capătul de citire al pipe-ului în procesul părinte

    printf("Total number of unsafe files found: %d\n", totalUnsafeFiles);

    free(unsafeFiles_EachProcess);
    free(snapshotDirPath); 
    return 0;
}