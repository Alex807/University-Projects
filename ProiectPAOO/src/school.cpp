#include "school.hpp"

#include <iostream>
#include <utility>

School::School(const std::string& nume, const std::string& adresa, std::size_t capacity)
    : numar_elevi(0),
      capacity(capacity && capacity <= SCHOOL_MAX_SIZE ? capacity : SCHOOL_MAX_SIZE),
      nume(nume),
      adresa(adresa),
      elevi(new Elev[this->capacity]) {
    std::cout << "[Constructor] Creating School: " << nume << "\n";
}

// Deep copy constructor to prevent double-free and dangling pointers.
School::School(const School& other)
    : numar_elevi(other.numar_elevi),
      capacity(other.capacity),
      nume(other.nume),
      adresa(other.adresa),
      elevi(new Elev[capacity]) {
    std::cout << "[Copy Constructor] Copying School: " << other.nume << "\n";
    for (std::size_t i = 0; i < numar_elevi; ++i) {
        elevi[i] = other.elevi[i];
    }
}

//move 
School::School(School&& other) noexcept
    : numar_elevi(other.numar_elevi),
      capacity(other.capacity),
      nume(std::move(other.nume)),
      adresa(std::move(other.adresa)),
      elevi(other.elevi) {
    std::cout << "[Move Constructor] Moving School\n";
    other.numar_elevi = 0;
    other.capacity = 0;
    other.elevi = nullptr;
}

School::~School() { 
    std::cout << "[Destructor] Destroying School: " << nume << "\n";
    delete[] elevi; 
}

// ITEM 10 & 11: Copy assignment operator with self-assignment handling
// This demonstrates the "copy and swap" technique for exception safety
School& School::operator=(const School& rhs) {
    std::cout << "[Copy Assignment] Assigning " << rhs.nume << " to " << nume << "\n";

    // ITEM 11: Handle self-assignment using copy-and-swap idiom
    // This is exception-safe and self-assignment-safe
    if (this == &rhs) {
        std::cout << "[Copy Assignment] Self-assignment detected!\n";
        return *this;
    }

    // ITEM 11: Alternative approach - make a copy first (exception-safe)
    // Save the original pointer
    Elev* pOrig = elevi;

    // Allocate new memory and copy data
    elevi = new Elev[rhs.capacity];

    // Copy all the data
    for (std::size_t i = 0; i < rhs.numar_elevi; ++i) {
        elevi[i] = rhs.elevi[i];
    }

    // Delete old memory only after successful copy
    delete[] pOrig;

    // Copy other members
    numar_elevi = rhs.numar_elevi;
    capacity = rhs.capacity;
    nume = rhs.nume;
    adresa = rhs.adresa;

    // ITEM 10: Return reference to *this to allow chaining
    return *this;
}

// Move assignment operator (also follows Item 10)
School& School::operator=(School&& rhs) noexcept {
    std::cout << "[Move Assignment] Moving School\n";

    // ITEM 11: Self-assignment check (though unlikely with move)
    if (this == &rhs) {
        return *this;
    }

    // Clean up existing resource
    delete[] elevi;

    // Transfer ownership
    numar_elevi = rhs.numar_elevi;
    capacity = rhs.capacity;
    nume = std::move(rhs.nume);
    adresa = std::move(rhs.adresa);
    elevi = rhs.elevi;

    // Leave rhs in valid state
    rhs.numar_elevi = 0;
    rhs.capacity = 0;
    rhs.elevi = nullptr;

    // ITEM 10: Return reference to *this
    return *this;
}

bool School::adaugareElev(const Elev& elev){
    if (numar_elevi >= capacity) {
        std::cout << "Scoala nu mai are locuri (" << capacity << ")\n";
        return false;
    }

    elevi[numar_elevi] = elev;
    ++numar_elevi;
    return true;
}

void School::printareElevi() const {
    if (numar_elevi == 0) {
        std::cout << "Nu exista elevi inregistrati inca.\n";
        return;
    }

    for (std::size_t i = 0; i < this->numar_elevi; ++i) {
        std::cout << "Elev " << i + 1 << ": " << elevi[i].getNume()
                  << ", varsta " << elevi[i].getVarsta()
                  << ", media " << elevi[i].getMedie() << '\n';
    }
}

void School::printSchoala() const {
    std::cout << "Scoala " << this->getNume()
              << " situata la adresa " << this->getAdresa()
              << " (capacitate " << capacity << ", elevi inscrisi " << numar_elevi << ")\n";
}

std::string School::getNume() const { return nume; }

std::string School::getAdresa() const { return adresa; }

std::size_t School::getNumarElevi() const {
    return numar_elevi;
}

std::size_t School::getCapacity() const {
    return capacity;
}