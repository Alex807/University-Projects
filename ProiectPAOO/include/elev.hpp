#pragma once 

#include <string>

class Elev {
    public:
        Elev(const std::string& nume, int varsta, float medie_intrare);
        Elev();
        ~Elev() = default;

        // ITEM 10: Assignment operator returns reference to *this
        // ITEM 11: Handles self-assignment
        Elev& operator=(const Elev& rhs);

        // ITEM 10: Compound assignment operators also return reference to *this
        Elev& operator+=(float bonus);

        std::string getNume() const;
        int getVarsta() const;
        float getMedie() const;

        void setMedie(float medie);

    private:
        std::string nume;
        int varsta;
        float medie_intrare;
};