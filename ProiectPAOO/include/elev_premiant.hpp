#pragma once

#include "elev.hpp"
#include <string>

// ITEM 12: Derived class demonstrating the importance of
// copying base class parts in copy constructor and assignment operator
class ElevPremiant : public Elev {
public:
    ElevPremiant(const std::string& nume, int varsta, float medie, 
                 const std::string& premiu, int numar_premii);
    ElevPremiant();
    ~ElevPremiant() = default;

    // ITEM 12: Copy constructor must call base class copy constructor
    ElevPremiant(const ElevPremiant& rhs);

    // ITEM 12: Assignment operator must call base class assignment operator
    // ITEM 10: Returns reference to *this
    // ITEM 11: Handles self-assignment
    ElevPremiant& operator=(const ElevPremiant& rhs);

    std::string getPremiu() const;
    int getNumarPremii() const;

private:
    std::string premiu;
    int numar_premii;
};