#include <string>

class Student {
private:
    std::string nume;
    int varsta;
    double* note; // pentru demonstrarea heap-ului
    int numarNote;

public:
    // Constructor implicit
    Student();

    // Constructor cu parametri
    Student(const std::string& n, int v);

    // Copy constructor
    Student(const Student& other);

    // Move constructor
    Student(Student&& other) noexcept;

    // Destructor
    ~Student();

    // Copy assignment operator
    Student& operator=(const Student& other);

    // Move assignment operator
    Student& operator=(Student&& other) noexcept;

    // Metode utilitare
    void adaugaNota(double nota);
    void afiseaza() const;

    // Factory function
    static Student creeazaStudent(const std::string& nume, int varsta);
};