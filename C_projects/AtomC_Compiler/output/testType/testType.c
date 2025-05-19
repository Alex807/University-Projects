struct S{
	int n;
	char text[16];
	};
	
struct S a;
struct S v[10];

void f(char text[],int i,char ch){
	text[i]=ch; 
	3 = i;
	}

int h(int x,int y){
	if(x>0 && x<y){
		f(v[x].text,y,'#');
		return 1;
		}
	return 0; 
	}


// // Test file: type_analysis_tests.c

// // 1. Left-value and Right-value Tests
// void test_lval_rval() {
//     int x;
//     x = 1;      // Correct: x is lval
//     1 = x;      // Error: cannot assign to rval
//     3 = 4;      // Error: cannot assign to rval
//     x + 1 = 5;  // Error: expression result is not lval
// }

// // 2. Array Tests
// void test_arrays() {
//     int arr[10];
//     int i;
//     char ch;
    
//     ch[i] = 1;        // Error: only arrays can be indexed
//     arr[3.14] = 1;    // Error: index not convertible to int
//     arr["hello"] = 1; // Error: index not convertible to int
//     arr = 1;          // Error: cannot assign to array
//     arr++;            // Error: cannot use array in arithmetic
// }

// // 3. Structure Tests
// struct Point {
//     int x;
//     int y;
// };

// void test_structs() {
//     struct Point p;
//     int x;
    
//     x.y = 1;          // Error: field can only be selected from struct
//     p.z = 1;          // Error: undefined field 'z'
//     p = 1;            // Error: cannot convert int to struct
//     if(p) {}          // Error: struct cannot be used in condition
// }

// // 4. Function Call Tests
// void func(int x, double y) {}
// int getValue() { return 1; }

// void test_functions() {
//     int x;
    
//     func();           // Error: too few arguments
//     func(1, 2, 3);    // Error: too many arguments
//     func("hello", 1); // Error: argument type mismatch
//     x = func;         // Error: function can only be called
//     getValue = 1;     // Error: function can only be called
// }

// // 5. Type Conversion Tests
// void test_conversions() {
//     struct Point p;
//     int arr[10];
//     int i;
//     double d;
    
//     i = p;            // Error: cannot convert struct to int
//     p = i;            // Error: cannot convert int to struct
//     i = arr;          // Error: cannot convert array to scalar
//     arr = i;          // Error: cannot convert scalar to array
// }

// // 6. Operator Type Tests
// void test_operators() {
//     struct Point p1, p2;
//     int arr[10];
//     int i;
    
//     // Arithmetic operators
//     p1 + p2;          // Error: invalid operand type for +
//     arr + 1;          // Error: invalid operand type for +
    
//     // Logical operators
//     if(p1 && p2) {}   // Error: invalid operand type for &&
//     if(arr || i) {}   // Error: array cannot be used in logical operation
    
//     // Comparison operators
//     if(p1 > p2) {}    // Error: invalid operand type for >
//     if(arr == arr) {} // Error: invalid operand type for ==
// }

// // 7. Control Flow Type Tests
// void test_control_flow() {
//     struct Point p;
//     int arr[10];
    
//     if(p) {}          // Error: if condition must be scalar
//     while(arr) {}     // Error: while condition must be scalar
    
//     return p;         // Error: cannot return struct from void function
//     return arr;       // Error: cannot return array
// }

// // 8. Complex Expression Tests
// void test_complex_expressions() {
//     int i;
//     double d;
//     struct Point p;
//     int arr[10];
    
//     // Complex expressions
//     (p + 1)[i];       // Multiple errors: struct in arithmetic, indexing non-array
//     arr[p.x + d] = i && p.y;  // Error: logical op with struct member
//     i = arr[p];       // Error: struct as array index
// }

// // 9. String and Char Tests
// void test_strings() {
//     char str[10];
//     char ch;
    
//     ch[0] = 'a';      // Error: only arrays can be indexed
//     str = "hello";    // Error: cannot assign to array
//     str + "world";    // Error: invalid operand type for +
// }

// // 10. Constant Expression Tests
// void test_constants() {
//     const int c = 5;
//     int x;
    
//     c = 10;           // Error: cannot assign to constant
//     c++;              // Error: cannot modify constant
//     x = c + 1;        // OK: can use constant in expressions
// }
