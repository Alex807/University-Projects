#include "elev.hpp"
#include "school.hpp"
#include <iostream>

// ==========================================
// BROKEN IMPLEMENTATION (for demonstration)
// ==========================================

// This class demonstrates what happens when we DON'T follow Items 10, 11, 12
class BrokenElev {
public:
    BrokenElev(const std::string& n = "", int v = 0, float m = 0.0f)
        : nume(n), varsta(v), medie(m) {}

    // BROKEN ITEM 10: Returns void instead of reference to *this
    void operator=(const BrokenElev& rhs) {
        nume = rhs.nume;
        varsta = rhs.varsta;
        medie = rhs.medie;
        // No return statement!
    }

    std::string getNume() const { return nume; }
    int getVarsta() const { return varsta; }
    float getMedie() const { return medie; }

private:
    std::string nume;
    int varsta;
    float medie;
};

// This class demonstrates broken self-assignment handling
class BrokenSchool {
public:
    BrokenSchool(const std::string& n, int cap)
        : nume(n), capacity(cap), count(0), data(new int[cap]) {}

    ~BrokenSchool() { delete[] data; }

    // BROKEN ITEM 11: No self-assignment check!
    BrokenSchool& operator=(const BrokenSchool& rhs) {
        std::cout << "  [BrokenSchool] Deleting old data...\n";
        delete[] data;  // DANGER: If this == &rhs, we just deleted rhs.data!

        std::cout << "  [BrokenSchool] Allocating new data...\n";
        capacity = rhs.capacity;
        count = rhs.count;
        nume = rhs.nume;

        std::cout << "  [BrokenSchool] Copying data from rhs...\n";
        data = new int[capacity];

        // This will read from DELETED memory if this == &rhs!
        for (int i = 0; i < count; ++i) {
            data[i] = rhs.data[i];  // UNDEFINED BEHAVIOR on self-assignment!
        }

        return *this;
    }

    void addData(int val) {
        if (count < capacity) {
            data[count++] = val;
        }
    }

    void print() const {
        std::cout << "    School: " << nume << ", data: [";
        for (int i = 0; i < count; ++i) {
            std::cout << data[i];
            if (i < count - 1) std::cout << ", ";
        }
        std::cout << "]\n";
    }

private:
    std::string nume;
    int capacity;
    int count;
    int* data;
};

// Base class for Item 12 demonstration
class BrokenBase {
public:
    BrokenBase(int x = 0) : base_value(x) {}

    BrokenBase& operator=(const BrokenBase& rhs) {
        base_value = rhs.base_value;
        return *this;
    }

    int getBaseValue() const { return base_value; }

private:
    int base_value;
};

// BROKEN ITEM 12: Doesn't copy base class parts!
class BrokenDerived : public BrokenBase {
public:
    BrokenDerived(int x = 0, int y = 0) 
        : BrokenBase(x), derived_value(y) {}

    // BROKEN: Doesn't call BrokenBase::operator=!
    BrokenDerived& operator=(const BrokenDerived& rhs) {
        std::cout << "  [BrokenDerived] Only copying derived_value...\n";
        // Missing: BrokenBase::operator=(rhs);
        derived_value = rhs.derived_value;
        return *this;
    }

    int getDerivedValue() const { return derived_value; }

private:
    int derived_value;
};

// ==========================================
// DEMONSTRATION TESTS
// ==========================================

void demonstrateItem10Broken() {
    std::cout << "\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
    std::cout << "BROKEN ITEM 10: operator= returns void\n";
    std::cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n";

    BrokenElev e1("Ana", 10, 9.5f);
    BrokenElev e2("Bob", 11, 8.5f);
    BrokenElev e3("Carl", 12, 7.5f);

    std::cout << "Attempting to chain assignments...\n";
    std::cout << "Code: e1 = e2 = e3;\n\n";

    // This line won't compile! Uncomment to see the error:
    // e1 = e2 = e3;

    std::cout << "❌ COMPILATION ERROR!\n";
    std::cout << "   Cannot chain assignments because operator= returns void\n";
    std::cout << "   Error: void value not ignored as it ought to be\n\n";

    std::cout << "Workaround (ugly and inconvenient):\n";
    e2 = e3;
    e1 = e2;
    std::cout << "✓ Must write: e2 = e3; e1 = e2;\n";
    std::cout << "  Result: e1 = " << e1.getNume() << "\n";
}

void demonstrateItem11Broken() {
    std::cout << "\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
    std::cout << "BROKEN ITEM 11: No self-assignment check\n";
    std::cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n";

    std::cout << "Creating BrokenSchool with data [1, 2, 3]...\n";
    BrokenSchool s("Test School", 5);
    s.addData(1);
    s.addData(2);
    s.addData(3);

    std::cout << "  Before self-assignment:\n";
    s.print();

    std::cout << "\n  Executing self-assignment: s = s\n";
    std::cout << "  Watch what happens...\n\n";

    s = s;  // DANGER: This causes undefined behavior!

    std::cout << "\n  After self-assignment:\n";
    s.print();

    std::cout << "\n⚠️  UNDEFINED BEHAVIOR OCCURRED!\n";
    std::cout << "   The data array was deleted, then we tried to copy from it!\n";
    std::cout << "   Result: Random garbage values or crash\n";
    std::cout << "   (may appear to work in debug builds but fail in production)\n";
}

void demonstrateItem12Broken() {
    std::cout << "\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
    std::cout << "BROKEN ITEM 12: Not copying base class parts\n";
    std::cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n";

    std::cout << "Creating objects:\n";
    BrokenDerived d1(100, 200);
    std::cout << "  d1: base_value = " << d1.getBaseValue() 
              << ", derived_value = " << d1.getDerivedValue() << "\n";

    BrokenDerived d2(999, 888);
    std::cout << "  d2: base_value = " << d2.getBaseValue() 
              << ", derived_value = " << d2.getDerivedValue() << "\n";

    std::cout << "\nExecuting: d2 = d1\n";
    d2 = d1;

    std::cout << "\nAfter assignment:\n";
    std::cout << "  d2: base_value = " << d2.getBaseValue() 
              << ", derived_value = " << d2.getDerivedValue() << "\n";

    std::cout << "\n❌ BUG DETECTED!\n";
    std::cout << "   Expected: base_value = 100, derived_value = 200\n";
    std::cout << "   Actual:   base_value = " << d2.getBaseValue() 
              << ", derived_value = " << d2.getDerivedValue() << "\n";
    std::cout << "   Problem: base_value was NOT copied (still has old value 999)!\n";
    std::cout << "   Cause: operator= didn't call BrokenBase::operator=(rhs)\n";
}

void showCorrectImplementations() {
    std::cout << "\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
    std::cout << "CORRECT IMPLEMENTATIONS (for comparison)\n";
    std::cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n";

    std::cout << "Item 10 - Correct:\n";
    std::cout << "  Elev& operator=(const Elev& rhs) {\n";
    std::cout << "      // ... copy members ...\n";
    std::cout << "      return *this;  // ✓ Returns reference\n";
    std::cout << "  }\n\n";

    std::cout << "Item 11 - Correct:\n";
    std::cout << "  School& operator=(const School& rhs) {\n";
    std::cout << "      if (this == &rhs) return *this;  // ✓ Self-assignment check\n";
    std::cout << "      // ... safe copying ...\n";
    std::cout << "      return *this;\n";
    std::cout << "  }\n\n";

    std::cout << "Item 12 - Correct:\n";
    std::cout << "  Derived& operator=(const Derived& rhs) {\n";
    std::cout << "      if (this == &rhs) return *this;\n";
    std::cout << "      Base::operator=(rhs);  // ✓ Call base class operator=\n";
    std::cout << "      // ... copy derived members ...\n";
    std::cout << "      return *this;\n";
    std::cout << "  }\n";
}

int main() {
    std::cout << "╔════════════════════════════════════════════════════════════╗\n";
    std::cout << "║  DEMONSTRATION: What Breaks Without Items 10, 11, 12     ║\n";
    std::cout << "║  These examples show ACTUAL BUGS that occur              ║\n";
    std::cout << "╚════════════════════════════════════════════════════════════╝\n";

    demonstrateItem10Broken();
    demonstrateItem11Broken();
    demonstrateItem12Broken();
    showCorrectImplementations();

    std::cout << "\n╔════════════════════════════════════════════════════════════╗\n";
    std::cout << "║  SUMMARY OF PROBLEMS                                      ║\n";
    std::cout << "╚════════════════════════════════════════════════════════════╝\n\n";

    std::cout << "Without Item 10:\n";
    std::cout << "  ❌ Assignment chaining doesn't compile\n";
    std::cout << "  ❌ Can't use class with STL algorithms\n";
    std::cout << "  ❌ Violates principle of least surprise\n\n";

    std::cout << "Without Item 11:\n";
    std::cout << "  ❌ Self-assignment causes undefined behavior\n";
    std::cout << "  ❌ Delete-then-copy-from-deleted-memory = crash\n";
    std::cout << "  ❌ Subtle aliasing bugs (a[i] = a[j] when i==j)\n\n";

    std::cout << "Without Item 12:\n";
    std::cout << "  ❌ Partial object copies (base class not copied)\n";
    std::cout << "  ❌ Silent bugs (compilers don't warn)\n";
    std::cout << "  ❌ Objects in inconsistent state\n\n";

    std::cout << "Run './test_items' to see the CORRECT implementations!\n\n";

    return 0;
}