#include "Student.h"

Student::Student(const std::string& name) : m_name(name) {
    std::cout << "  [Resource] Student '" << m_name << "' acquired (Constructor)." << std::endl;
}

Student::~Student() {
    std::cout << "  [Resource] Student '" << m_name << "' released (Destructor)." << std::endl;
}

Student::Student(const Student& other) : m_name(other.m_name) {
    std::cout << "  [Resource] Student '" << m_name << "' copied." << std::endl;
}

void Student::study() const {
    std::cout << "  [Action] " << m_name << " is studying." << std::endl;
}
