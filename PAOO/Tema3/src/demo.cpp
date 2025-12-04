#include "demo.h"
#include "Student.h"
#include "Item14_Wrappers.h"
#include <iostream>
#include <memory>

void printHeader(const std::string& title) {
    std::cout << "\n========================================\n";
    std::cout << " " << title << "\n";
    std::cout << "========================================\n";
}

void runItem13Demo() {
    printHeader("ITEM 13: RAII (Resource Acquisition Is Initialization)");

    std::cout << "1. MANUAL MANAGEMENT (The Dangerous Way)\n";
    std::cout << "   Simulating a raw pointer usage...\n";
    {
        Student* rawStudent = new Student("Raw_Bob");
        rawStudent->study();
        delete rawStudent; 
        std::cout << "   Manual delete called.\n";
    }

    std::cout << "\n2. RAII MANAGEMENT (std::unique_ptr)\n";
    std::cout << "   Entering scope...\n";
    {
        std::unique_ptr<Student> smartStudent = std::make_unique<Student>("Smart_Alice");
        smartStudent->study();
        std::cout << "   Exiting scope now... (Expect destructor)\n";
    } 
    std::cout << "   Scope exited. Resource released automatically.\n";
}

void runItem14Demo() {
    printHeader("ITEM 14: Copying Behavior Strategies");

    std::cout << "\n--- Strategy 1: Prohibit Copying (e.g., Locks) ---\n";
    {
        Student* s = new Student("Lock_Resource");
        ScopedLockStudent lock1(s);
        std::cout << "   Copying is explicitly deleted in class definition.\n";
    } 

    std::cout << "\n--- Strategy 2: Reference Counting (shared_ptr) ---\n";
    {
        SharedStudentWrapper w1("Shared_Charlie");
        w1.printRefCount();
        {
            std::cout << "   Creating copy w2 from w1...\n";
            SharedStudentWrapper w2 = w1; 
            w1.printRefCount();
            w2.printRefCount();
            std::cout << "   Destroying w2...\n";
        }
        std::cout << "   w2 destroyed. Back to w1.\n";
        w1.printRefCount();
    } 

    std::cout << "\n--- Strategy 3: Deep Copy ---\n";
    {
        DeepCopyStudentWrapper d1("Deep_Dave");
        std::cout << "   Creating copy d2 from d1...\n";
        DeepCopyStudentWrapper d2 = d1; 
        std::cout << "   d1 and d2 manage separate resources.\n";
    } 
}
