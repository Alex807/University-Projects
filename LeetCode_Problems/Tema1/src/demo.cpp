#include "Student.h"  // Include the header
#include <iostream>

void demonstreazaConcepte() {
    std::cout << "\n=== 1. Constructor implicit ===" << std::endl;
    Student s1; // constructor implicit

    std::cout << "\n=== 2. Încapsulare (membri privați accesați prin metode publice) ===" << std::endl;
    Student s2("Ion Popescu", 20);
    s2.adaugaNota(9.5);
    s2.adaugaNota(8.7);
    s2.afiseaza();

    std::cout << "\n=== 3. Inițializarea membrilor din constructor ===" << std::endl;
    Student s3("Maria Ionescu", 19);
    s3.afiseaza();

    std::cout << "\n=== 4. Eliberarea heap-ului în destructor ===" << std::endl;
    {
        Student s4("Temp Student", 21);
        s4.adaugaNota(10.0);
        // Destructorul va fi apelat automat la ieșirea din scope
    }
    std::cout << "Obiectul temporar a fost distrus" << std::endl;

    std::cout << "\n=== 5. Copy constructor ===" << std::endl;
    Student s5 = s2; // copy constructor
    s5.afiseaza();

    std::cout << "\n=== 6. Move constructor ===" << std::endl;
    Student s6 = Student::creeazaStudent("Ana Georgescu", 22); // move constructor
    s6.adaugaNota(9.8);
    s6.afiseaza();

    std::cout << "\n=== Copy assignment operator ===" << std::endl;
    Student s7("Mihai Dumitrescu", 23);
    s7 = s2; // copy assignment
    s7.afiseaza();

    std::cout << "\n=== Move assignment operator ===" << std::endl;
    s7 = Student::creeazaStudent("Elena Vasilescu", 24); // move assignment
    s7.afiseaza();
}