#include "elev.hpp"
#include "elev_premiant.hpp"
#include "school.hpp"
#include <iostream>

void separator(const std::string& title) {
    std::cout << "\n========================================\n";
    std::cout << title << "\n";
    std::cout << "========================================\n";
}

// Test Item 10: Assignment operators return reference to *this
void testItem10() {
    separator("ITEM 10: Assignment Operators Return Reference to *this");

    std::cout << "\n--- Testing assignment chaining (only works if operator= returns *this) ---\n";
    Elev e1("Ana", 10, 9.5f);
    Elev e2("Bogdan", 11, 8.5f);
    Elev e3("Cristina", 12, 9.0f);
    Elev e4("Dan", 13, 8.0f);

    // This chaining ONLY works because operator= returns a reference to *this
    // If operator= returned void or a value, this wouldn't compile
    std::cout << "\nChaining assignments: e1 = e2 = e3 = e4\n";
    e1 = e2 = e3 = e4;

    std::cout << "\nAfter chaining, all should be 'Dan' with media 8.0:\n";
    std::cout << "e1: " << e1.getNume() << ", media " << e1.getMedie() << "\n";
    std::cout << "e2: " << e2.getNume() << ", media " << e2.getMedie() << "\n";
    std::cout << "e3: " << e3.getNume() << ", media " << e3.getMedie() << "\n";

    std::cout << "\n--- Testing compound assignment chaining ---\n";
    Elev e5("Elena", 10, 8.0f);

    // Compound assignment also returns *this, allowing chaining
    std::cout << "Before: " << e5.getNume() << " has media " << e5.getMedie() << "\n";
    (e5 += 0.5f) += 0.3f;  // This works because operator+= returns *this
    std::cout << "After e5 += 0.5 += 0.3: media is " << e5.getMedie() << "\n";

    std::cout << "\n✓ WITHOUT Item 10: These chainings wouldn't compile!\n";
}

// Test Item 11: Handle self-assignment
void testItem11() {
    separator("ITEM 11: Handle Assignment to Self");

    std::cout << "\n--- Testing self-assignment with simple object ---\n";
    Elev e1("Florin", 11, 9.2f);
    std::cout << "Before self-assignment: " << e1.getNume() << ", media " << e1.getMedie() << "\n";

    // Self-assignment - looks silly but can happen in real code
    e1 = e1;

    std::cout << "After self-assignment: " << e1.getNume() << ", media " << e1.getMedie() << "\n";
    std::cout << "✓ Object remains valid!\n";

    std::cout << "\n--- Testing self-assignment with resource-managing object ---\n";
    School s1("Liceul National", "Str. Libertatii 10", 5);
    s1.adaugareElev(Elev("George", 15, 9.5f));
    s1.adaugareElev(Elev("Horia", 16, 9.0f));

    std::cout << "\nBefore self-assignment:\n";
    s1.printSchoala();
    s1.printareElevi();

    // Self-assignment with dynamically allocated memory
    s1 = s1;

    std::cout << "\nAfter self-assignment:\n";
    s1.printSchoala();
    s1.printareElevi();

    std::cout << "\n✓ WITHOUT Item 11: Without self-assignment check,\n";
    std::cout << "  the School would delete its 'elevi' array,\n";
    std::cout << "  then try to copy from the deleted array!\n";
    std::cout << "  This causes UNDEFINED BEHAVIOR and likely crashes!\n";

    std::cout << "\n--- Testing aliasing (hidden self-assignment) ---\n";
    School* ps1 = &s1;
    School* ps2 = &s1;  // Both pointers point to same object

    std::cout << "ps1 and ps2 point to the same School object\n";
    *ps1 = *ps2;  // This is self-assignment!

    std::cout << "After aliased assignment:\n";
    s1.printSchoala();
    std::cout << "✓ Still works correctly!\n";
}

// Test Item 12: Copy all parts of an object
void testItem12() {
    separator("ITEM 12: Copy All Parts of an Object");

    std::cout << "\n--- Creating ElevPremiant objects ---\n";
    ElevPremiant ep1("Ion Popescu", 14, 9.8f, "Premiul I Olimpiada Matematica", 3);

    std::cout << "\nOriginal ElevPremiant:\n";
    std::cout << "  Nume: " << ep1.getNume() << "\n";
    std::cout << "  Varsta: " << ep1.getVarsta() << "\n";
    std::cout << "  Medie: " << ep1.getMedie() << "\n";
    std::cout << "  Premiu: " << ep1.getPremiu() << "\n";
    std::cout << "  Numar premii: " << ep1.getNumarPremii() << "\n";

    std::cout << "\n--- Testing copy constructor ---\n";
    ElevPremiant ep2(ep1);

    std::cout << "\nCopied ElevPremiant (via copy constructor):\n";
    std::cout << "  Nume: " << ep2.getNume() << "\n";
    std::cout << "  Varsta: " << ep2.getVarsta() << "\n";
    std::cout << "  Medie: " << ep2.getMedie() << "\n";
    std::cout << "  Premiu: " << ep2.getPremiu() << "\n";
    std::cout << "  Numar premii: " << ep2.getNumarPremii() << "\n";

    std::cout << "\n✓ WITHOUT Item 12: If copy constructor doesn't call Elev(rhs),\n";
    std::cout << "  base class members would be DEFAULT-INITIALIZED!\n";
    std::cout << "  Result: nume='', varsta=0, medie=0.0 (WRONG!)\n";

    std::cout << "\n--- Testing assignment operator ---\n";
    ElevPremiant ep3("Maria Ionescu", 15, 9.5f, "Premiul II Olimpiada Fizica", 2);

    std::cout << "\nBefore assignment:\n";
    std::cout << "  ep3: " << ep3.getNume() << ", medie " << ep3.getMedie() 
              << ", " << ep3.getPremiu() << "\n";

    std::cout << "\nAssigning ep1 to ep3...\n";
    ep3 = ep1;

    std::cout << "\nAfter assignment:\n";
    std::cout << "  Nume: " << ep3.getNume() << "\n";
    std::cout << "  Varsta: " << ep3.getVarsta() << "\n";
    std::cout << "  Medie: " << ep3.getMedie() << "\n";
    std::cout << "  Premiu: " << ep3.getPremiu() << "\n";
    std::cout << "  Numar premii: " << ep3.getNumarPremii() << "\n";

    std::cout << "\n✓ WITHOUT Item 12: If operator= doesn't call Elev::operator=(rhs),\n";
    std::cout << "  base class members would NOT be copied!\n";
    std::cout << "  Result: nume, varsta, medie would keep OLD values (WRONG!)\n";

    std::cout << "\n--- Testing self-assignment in derived class ---\n";
    ep3 = ep3;
    std::cout << "After self-assignment, ep3 still valid:\n";
    std::cout << "  " << ep3.getNume() << ", " << ep3.getPremiu() << "\n";
}

// Demonstrate what happens without proper implementation
void demonstrateProblems() {
    separator("DEMONSTRATING PROBLEMS WITHOUT THESE ITEMS");

    std::cout << "\n--- PROBLEM 1: Without Item 10 (not returning *this) ---\n";
    std::cout << "If operator= returned void:\n";
    std::cout << "  - Chaining assignments (a = b = c) wouldn't compile\n";
    std::cout << "  - Standard library containers expect this convention\n";
    std::cout << "  - Code would be inconsistent with built-in types\n";

    std::cout << "\n--- PROBLEM 2: Without Item 11 (no self-assignment check) ---\n";
    std::cout << "For School class with dynamic memory:\n";
    std::cout << "  1. s = s would execute: delete[] elevi\n";
    std::cout << "  2. Then try: elevi = new Elev[rhs.capacity]\n";
    std::cout << "  3. Then copy: elevi[i] = rhs.elevi[i]\n";
    std::cout << "  BUT rhs.elevi was ALREADY DELETED in step 1!\n";
    std::cout << "  RESULT: Reading from deleted memory = CRASH or corruption\n";

    std::cout << "\n--- PROBLEM 3: Without Item 12 (not copying base parts) ---\n";
    std::cout << "For ElevPremiant derived from Elev:\n";
    std::cout << "  Without calling Elev::operator=(rhs):\n";
    std::cout << "    - Only premiu and numar_premii get copied\n";
    std::cout << "    - nume, varsta, medie_intrare KEEP OLD VALUES\n";
    std::cout << "  Without calling Elev(rhs) in copy constructor:\n";
    std::cout << "    - Base class gets default-initialized\n";
    std::cout << "    - nume='', varsta=0, medie=0.0\n";
    std::cout << "  RESULT: Partial/incorrect object state!\n";
}

int main() {
    std::cout << "╔═══════════════════════════════════════════════════════════╗\n";
    std::cout << "║  Testing Effective C++ Items 10, 11, and 12             ║\n";
    std::cout << "║  Demonstrating why they are CRITICAL for correctness    ║\n";
    std::cout << "╚═══════════════════════════════════════════════════════════╝\n";

    testItem10();
    testItem11();
    testItem12();
    demonstrateProblems();

    separator("SUMMARY");
    std::cout << "\n✓ Item 10: Assignment operators return *this\n";
    std::cout << "  - Enables chaining: a = b = c\n";
    std::cout << "  - Follows convention of built-in types\n";
    std::cout << "  - Required by standard library\n";

    std::cout << "\n✓ Item 11: Handle self-assignment in operator=\n";
    std::cout << "  - Prevents deleting resources before copying\n";
    std::cout << "  - Handles aliasing (a[i] = a[j] when i == j)\n";
    std::cout << "  - Exception-safe implementations are often self-assignment-safe\n";

    std::cout << "\n✓ Item 12: Copy all parts of an object\n";
    std::cout << "  - Copy ALL data members in copying functions\n";
    std::cout << "  - Call base class copying functions in derived classes\n";
    std::cout << "  - Forgetting causes partial copies and bugs\n";

    std::cout << "\n";
    return 0;
}