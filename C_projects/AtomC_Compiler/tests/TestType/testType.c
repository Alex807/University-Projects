struct S{
	int n;
	char text[16];
	};
	
struct S a;
struct S v[10];

void f(char text[],int i,char ch){
	text[i]=ch; 
}

int h(int x,int y){
	if(x>0 && x<y){
		f(v[x].text,y,'#');
		return 1;
		}
	return 0; 
}

// // Test 1: Verifies that IF condition must be scalar
// // Expected: Error - if condition must be scalar
// struct S { int x; };
// void f() {
//     struct S s;
//     if(s) {}
// }

// // Test 2: Verifies that IF condition accepts valid scalar value
// // Expected: Valid
// void f() {
//     int x = 1;
//     if(x) {}
// }

// // Test 3: Verifies that WHILE condition must be scalar
// // Expected: Error - while condition must be scalar
// struct S { int x; };
// void f() {
//     struct S s;
//     while(s) {}
// }

// // Test 4: Verifies that WHILE condition accepts valid scalar value
// // Expected: Valid
// void f() {
//     int x = 1;
//     while(x) {}
// }

// // Test 5: Verifies that void functions cannot return value
// // Expected: Error - void function cannot return value
// void f() {
//     return 5;
// }

// // Test 6: Verifies that non-void functions must return value
// // Expected: Error - non-void function must return value
// int f() {
//     return;
// }

// // Test 7: Verifies return type conversion rules
// // Expected: Error - cannot convert double to int
// int f() {
//     return 1.5;
// }

// // Test 8: Verifies valid return in void function
// // Expected: Valid
// void f() {
//     return;
// }

// // Test 9: Verifies assignment requires left-value
// // Expected: Error - destination must be left-value
// void f() {
//     5 = 3;
// }

// // Test 10: Verifies assignment to constant is forbidden
// // Expected: Error - destination cannot be constant
// void f() {
//     const int x = 5;
//     x = 3;
// }

// // Test 11: Verifies assignment operands must be scalar
// // Expected: Error - assignment operands must be scalar
// struct S { int x; };
// void f() {
//     struct S s1, s2;
//     s1 = s2;
// }

// // Test 12: Verifies assignment type compatibility
// // Expected: Error - cannot convert struct to int
// void f() {
//     int x;
//     struct S s;
//     x = s;
// }

// // Test 13: Verifies OR operation requires scalar operands
// // Expected: Error - invalid operand type for ||
// struct S { int x; };
// void f() {
//     struct S s1, s2;
//     s1 || s2;
// }

// // Test 14: Verifies AND operation requires scalar operands
// // Expected: Error - invalid operand type for &&
// struct S { int x; };
// void f() {
//     struct S s1, s2;
//     s1 && s2;
// }

// // Test 15: Verifies valid logical operations
// // Expected: Valid
// void f() {
//     int x = 1, y = 0;
//     x || y;
//     x && y;
// }

// // Test 16: Verifies equality comparison requires scalar operands
// // Expected: Error - invalid operand type for ==
// struct S { int x; };
// void f() {
//     struct S s1, s2;
//     s1 == s2;
// }

// // Test 17: Verifies relational comparison requires scalar operands
// // Expected: Error - invalid operand type for <
// struct S { int x; };
// void f() {
//     struct S s1, s2;
//     s1 < s2;
// }

// // Test 18: Verifies addition requires scalar operands
// // Expected: Error - invalid operand type for +
// struct S { int x; };
// void f() {
//     struct S s1, s2;
//     s1 + s2;
// }

// // Test 19: Verifies multiplication requires scalar operands
// // Expected: Error - invalid operand type for *
// struct S { int x; };
// void f() {
//     struct S s1, s2;
//     s1 * s2;
// }

// // Test 20: Verifies struct cannot be cast
// // Expected: Error - cannot convert a struct
// struct S { int x; };
// void f() {
//     struct S s;
//     (int)s;
// }

// // Test 21: Verifies cannot cast to struct type
// // Expected: Error - cannot convert to struct type
// struct S { int x; };
// void f() {
//     int x;
//     (struct S)x;
// }

// // Test 22: Verifies array to scalar cast restrictions
// // Expected: Error - array can only be converted to another array
// void f() {
//     int arr[10];
//     (int)arr;
// }

// // Test 23: Verifies unary minus requires scalar operand
// // Expected: Error - unary - must have scalar operand
// struct S { int x; };
// void f() {
//     struct S s;
//     -s;
// }

// // Test 24: Verifies logical NOT requires scalar operand
// // Expected: Error - ! must have scalar operand
// struct S { int x; };
// void f() {
//     struct S s;
//     !s;
// }

// // Test 25: Verifies only arrays can be indexed
// // Expected: Error - only arrays can be indexed
// void f() {
//     int x;
//     x[0];
// }

// // Test 26: Verifies array index must be convertible to int
// // Expected: Error - index not convertible to int
// void f() {
//     int arr[10];
//     struct S s;
//     arr[s];
// }

// // Test 27: Verifies field access requires struct
// // Expected: Error - field can only be selected from struct
// void f() {
//     int x;
//     x.field;
// }

// // Test 28: Verifies field must exist in struct
// // Expected: Error - field y doesn't exist in struct S
// struct S { int x; };
// void f() {
//     struct S s;
//     s.y;
// }

// // Test 29: Verifies only functions can be called
// // Expected: Error - only functions can be called
// void f() {
//     int x;
//     x();
// }

// // Test 30: Verifies functions must be called
// // Expected: Error - function can only be called
// void g() {}
// void f() {
//     g;
// }

// // Test 31: Verifies function call argument count (too many)
// // Expected: Error - too many arguments
// void g(int x) {}
// void f() {
//     g(1, 2);
// }

// // Test 32: Verifies function call argument count (too few)
// // Expected: Error - too few arguments
// void g(int x, int y) {}
// void f() {
//     g(1);
// }

// // Test 33: Verifies function argument type compatibility
// // Expected: Error - cannot convert argument type
// void g(int x) {}
// void f() {
//     struct S s;
//     g(s);
// }

// // Test 34: Verifies valid function call
// // Expected: Valid
// void g(int x) {}
// void f() {
//     g(1);
// }