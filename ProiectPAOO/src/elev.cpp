#include "elev.hpp"
#include <iostream>

// Default constructor keeps members in a valid, minimal state.
Elev::Elev() : nume(""), varsta(0), medie_intrare(0.0f) {}

Elev::Elev(const std::string& nume, int varsta, float medie_intrare)
    : nume(nume), varsta(varsta), medie_intrare(medie_intrare) {}

// ITEM 10 & 11: Assignment operator that returns reference to *this
// and handles self-assignment
Elev& Elev::operator=(const Elev& rhs) {
    // ITEM 11: Identity test to handle self-assignment
    if (this == &rhs) {
        std::cout << "[DEBUG] Self-assignment detected for Elev: " << nume << "\n";
        return *this;
    }

    std::cout << "[DEBUG] Assigning " << rhs.nume << " to " << nume << "\n";

    // Copy all data members
    nume = rhs.nume;
    varsta = rhs.varsta;
    medie_intrare = rhs.medie_intrare;

    // ITEM 10: Return reference to *this to allow chaining
    return *this;
}

// ITEM 10: Compound assignment operator also returns reference to *this
Elev& Elev::operator+=(float bonus) {
    medie_intrare += bonus;
    if (medie_intrare > 10.0f) medie_intrare = 10.0f;
    return *this; // ITEM 10: return reference to *this
}

int Elev::getVarsta() const { return this->varsta; }

float Elev::getMedie() const { return this->medie_intrare; }

std::string Elev::getNume() const { return this->nume; }

void Elev::setMedie(float medie) { this->medie_intrare = medie; }