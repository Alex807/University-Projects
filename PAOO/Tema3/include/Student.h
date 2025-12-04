#ifndef STUDENT_H
#define STUDENT_H

#include <string>
#include <iostream>

// Represents a heavy resource
class Student {
public:
    Student(const std::string& name);
    ~Student();

    // Copy constructor kept public for Deep Copy demo
    Student(const Student& other); 
    
    std::string getName() const { return m_name; }
    void study() const;

private:
    std::string m_name;
};

#endif
