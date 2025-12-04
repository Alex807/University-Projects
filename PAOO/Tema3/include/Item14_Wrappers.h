#ifndef ITEM14_WRAPPERS_H
#define ITEM14_WRAPPERS_H

#include "Student.h"
#include <memory>
#include <iostream>

// STRATEGY 1: Prohibit Copying
class ScopedLockStudent {
public:
    explicit ScopedLockStudent(Student* s) : studentPtr(s) {
        std::cout << "[ScopedLock] Locking resource for " << s->getName() << std::endl;
    }
    
    ~ScopedLockStudent() {
        std::cout << "[ScopedLock] Releasing resource for " << studentPtr->getName() << std::endl;
        delete studentPtr;
    }

    // Delete copy operations
    ScopedLockStudent(const ScopedLockStudent&) = delete;
    ScopedLockStudent& operator=(const ScopedLockStudent&) = delete;

private:
    Student* studentPtr;
};

// STRATEGY 2: Reference Counting
class SharedStudentWrapper {
public:
    explicit SharedStudentWrapper(std::string name) 
        : studentPtr(std::make_shared<Student>(name)) {}

    void printRefCount() const {
        std::cout << "[SharedWrapper] Ref count for " << studentPtr->getName() 
                  << ": " << studentPtr.use_count() << std::endl;
    }

private:
    std::shared_ptr<Student> studentPtr;
};

// STRATEGY 3: Deep Copy
class DeepCopyStudentWrapper {
public:
    explicit DeepCopyStudentWrapper(const std::string& name)
        : studentPtr(new Student(name)) {}

    ~DeepCopyStudentWrapper() {
        delete studentPtr;
    }

    DeepCopyStudentWrapper(const DeepCopyStudentWrapper& rhs) {
        std::cout << "[DeepCopyWrapper] Copying... Allocating NEW resource." << std::endl;
        studentPtr = new Student(rhs.studentPtr->getName() + "_Copy");
    }

    DeepCopyStudentWrapper& operator=(const DeepCopyStudentWrapper& rhs) {
        if (this == &rhs) return *this;
        std::cout << "[DeepCopyWrapper] Assigning... Allocating NEW resource." << std::endl;
        
        Student* temp = new Student(rhs.studentPtr->getName() + "_Copy");
        delete studentPtr;
        studentPtr = temp;
        return *this;
    }

private:
    Student* studentPtr;
};

#endif
