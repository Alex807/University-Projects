#pragma once

#include <cstddef>
#include <string>
#include "elev.hpp"

#ifndef SCHOOL_MAX_SIZE
#define SCHOOL_MAX_SIZE 100
#endif

class School{
    public:
        explicit School(const std::string& nume, const std::string& adresa, std::size_t capacity = SCHOOL_MAX_SIZE);
        School(const School& other);
        School(School&& other) noexcept;
        ~School();

        // ITEM 10 & 11: Assignment operator that returns reference to *this
        // and handles self-assignment properly
        School& operator=(const School& rhs);

        // Move assignment operator (also follows Item 10)
        School& operator=(School&& rhs) noexcept;

        bool adaugareElev(const Elev& elev); 
        void printareElevi() const;
        void printSchoala() const;

        std::string getNume() const;
        std::string getAdresa() const;
        std::size_t getNumarElevi() const;
        std::size_t getCapacity() const;

    private:
        std::size_t numar_elevi;
        std::size_t capacity;
        std::string nume;
        std::string adresa;
        Elev* elevi;

};