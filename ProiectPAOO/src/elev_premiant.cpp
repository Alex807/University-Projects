#include "elev_premiant.hpp"
#include <iostream>

ElevPremiant::ElevPremiant() 
    : Elev(), premiu(""), numar_premii(0) {
}

ElevPremiant::ElevPremiant(const std::string& nume, int varsta, float medie,
                           const std::string& premiu, int numar_premii)
    : Elev(nume, varsta, medie), // Initialize base class
      premiu(premiu),
      numar_premii(numar_premii) {
    std::cout << "[ElevPremiant Constructor] Creating: " << nume << "\n";
}

// ITEM 12: Copy constructor MUST invoke base class copy constructor
// Otherwise, base class parts will be default-initialized!
ElevPremiant::ElevPremiant(const ElevPremiant& rhs)
    : Elev(rhs),  // CRITICAL: Call base class copy constructor!
      premiu(rhs.premiu),
      numar_premii(rhs.numar_premii) {
    std::cout << "[ElevPremiant Copy Constructor] Copying: " << rhs.getNume() << "\n";
}

// ITEM 12: Assignment operator MUST call base class assignment operator
// ITEM 10: Returns reference to *this for chaining
// ITEM 11: Handles self-assignment
ElevPremiant& ElevPremiant::operator=(const ElevPremiant& rhs) {
    std::cout << "[ElevPremiant Assignment] Assigning " << rhs.getNume() 
              << " to " << getNume() << "\n";

    // ITEM 11: Handle self-assignment
    if (this == &rhs) {
        std::cout << "[ElevPremiant Assignment] Self-assignment detected!\n";
        return *this;
    }

    // ITEM 12: CRITICAL - Call base class assignment operator!
    // Without this, base class members (nume, varsta, medie) won't be copied!
    Elev::operator=(rhs);

    // Copy derived class members
    premiu = rhs.premiu;
    numar_premii = rhs.numar_premii;

    // ITEM 10: Return reference to *this
    return *this;
}

std::string ElevPremiant::getPremiu() const {
    return premiu;
}

int ElevPremiant::getNumarPremii() const {
    return numar_premii;
}