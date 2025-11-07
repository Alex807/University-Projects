#include "Student.h"  // Note: Use quotes for local includes
#include <iostream>

Student::Student() : nume("Necunoscut"), varsta(0), note(nullptr), numarNote(0) {
    std::cout << "Constructor implicit apelat pentru " << nume << std::endl;
}

Student::Student(const std::string& n, int v) : nume(n), varsta(v), numarNote(0) {
    std::cout << "Constructor cu parametri apelat pentru " << nume << std::endl;
    // Alocare pe heap pentru note
    note = new double[10]; // maxim 10 note
}

Student::Student(const Student& other) : nume(other.nume), varsta(other.varsta), numarNote(other.numarNote) {
    std::cout << "Copy constructor apelat pentru " << nume << std::endl;
    // Deep copy pentru note
    if (other.note != nullptr) {
        note = new double[10];
        for (int i = 0; i < numarNote; i++) {
            note[i] = other.note[i];
        }
    } else {
        note = nullptr;
    }
}

Student::Student(Student&& other) noexcept : nume(std::move(other.nume)), varsta(other.varsta),
                                             note(other.note), numarNote(other.numarNote) {
    std::cout << "Move constructor apelat pentru " << nume << std::endl;
    // Transferăm ownership-ul
    other.note = nullptr;
    other.numarNote = 0;
}

Student::~Student() {
    std::cout << "Destructor apelat pentru " << nume << std::endl;
    delete[] note; // eliberare heap
}

Student& Student::operator=(const Student& other) {
    std::cout << "Copy assignment operator apelat pentru " << nume << std::endl;
    if (this != &other) { // verificare auto-assignment
        // Eliberăm memoria existentă
        delete[] note;

        // Copiem datele
        nume = other.nume;
        varsta = other.varsta;
        numarNote = other.numarNote;

        // Deep copy pentru note
        if (other.note != nullptr) {
            note = new double[10];
            for (int i = 0; i < numarNote; i++) {
                note[i] = other.note[i];
            }
        } else {
            note = nullptr;
        }
    }
    return *this;
}

Student& Student::operator=(Student&& other) noexcept {
    std::cout << "Move assignment operator apelat pentru " << nume << std::endl;
    if (this != &other) {
        // Eliberăm memoria existentă
        delete[] note;

        // Transferăm ownership-ul
        nume = std::move(other.nume);
        varsta = other.varsta;
        note = other.note;
        numarNote = other.numarNote;

        // Resetăm obiectul sursă
        other.note = nullptr;
        other.numarNote = 0;
    }
    return *this;
}

void Student::adaugaNota(double nota) {
    if (numarNote < 10 && note != nullptr) {
        note[numarNote++] = nota;
    }
}

void Student::afiseaza() const {
    std::cout << "Student: " << nume << ", Varsta: " << varsta;
    if (note != nullptr && numarNote > 0) {
        std::cout << ", Note: ";
        for (int i = 0; i < numarNote; i++) {
            std::cout << note[i] << " ";
        }
    }
    std::cout << std::endl;
}

Student Student::creeazaStudent(const std::string& nume, int varsta) {
    return Student(nume, varsta); // RVO/move
}