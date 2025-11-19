#include "elev.hpp"
#include "school.hpp"

#include <iostream>

// Returneaza o scoala nou infiintata; cand este atribuita declanseaza mutarea daca optimizerul nu aplica NRVO.
School deschideScoalaSatelit() {
    School scoala("Scoala Satelit Dimitrie Gusti", "Strada Lalelelor 12", 3);
    scoala.adaugareElev(Elev("Ana Pop", 12, 9.50f));
    scoala.adaugareElev(Elev("Mihai Ionescu", 13, 9.10f));
    scoala.adaugareElev(Elev("Ioana Stan", 12, 9.75f));
    return scoala;
}

// Primeste prin valoare pentru a genera o copie completa folosind copy constructorul.
void tiparesteSituatieInspectie(School scoala) {
    std::cout << "\n[Raport trimis inspectoratului]\n";
    scoala.printSchoala();
    scoala.printareElevi();
}

int main() {
    std::cout << "=== Biroul secretariatului - inceput de an scolar ===\n\n";

    School scoalaCartier("Scoala Gimnaziala Mircea Eliade", "Bulevardul Independentei 15", 5);
    std::cout << "Secretara introduce elevii noi in catalog...\n";
    scoalaCartier.adaugareElev(Elev("Ioana Marinescu", 10, 9.20f));
    scoalaCartier.adaugareElev(Elev("Teodor Barbu", 11, 8.75f));
    scoalaCartier.adaugareElev(Elev("Anca Dragnea", 10, 9.55f));

    std::cout << "\nSituatia actuala a scolii din cartier:\n";
    scoalaCartier.printSchoala();
    scoalaCartier.printareElevi();

    std::cout << "\nInspectoratul solicita organizarea unei scoli satelit pentru copiii din cartierul vecin.\n";
    School scoalaSatelit = deschideScoalaSatelit(); // move constructor daca nu intervine NRVO
    scoalaSatelit.printSchoala();
    scoalaSatelit.printareElevi();

    std::cout << "\nDirectorul cere o copie a registrului pentru arhiva fizica.\n";
    School copieRegistru(scoalaCartier);  // copy constructor
    copieRegistru.printSchoala();
    copieRegistru.printareElevi();

    tiparesteSituatieInspectie(scoalaCartier); // declanseaza o noua copiere

    std::cout << "\nZiua s-a incheiat. Aplicatia inchide evidenta si obiectele elibereaza memoria.\n";
    return 0;
}