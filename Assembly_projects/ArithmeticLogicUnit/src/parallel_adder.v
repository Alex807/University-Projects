module parallel_adder (
    input cin,
    input [16:0]a, b,
    output [16:0]out_add
);
    wire w0, w1, w2, w3, w4, w5, w6, w7, w8;
    wire w9, w10, w11, w12, w13, w14, w15;
    wire [16:0]out;

    fac fac0(.a(a[0]), .b(b[0]), .cin(cin), .out(out[0]), .cout(w0));
    fac fac1(.a(a[1]), .b(b[1]), .cin(w0), .out(out[1]), .cout(w1));
    fac fac2(.a(a[2]), .b(b[2]), .cin(w1), .out(out[2]), .cout(w2));
    fac fac3(.a(a[3]), .b(b[3]), .cin(w2), .out(out[3]), .cout(w3));
    fac fac4(.a(a[4]), .b(b[4]), .cin(w3), .out(out[4]), .cout(w4));
    fac fac5(.a(a[5]), .b(b[5]), .cin(w4), .out(out[5]), .cout(w5));
    fac fac6(.a(a[6]), .b(b[6]), .cin(w5), .out(out[6]), .cout(w6));
    fac fac7(.a(a[7]), .b(b[7]), .cin(w6), .out(out[7]), .cout(w7));

    fac fac8(.a(a[8]), .b(b[8]), .cin(w7), .out(out[8]), .cout(w8));
    fac fac9(.a(a[9]), .b(b[9]), .cin(w8), .out(out[9]), .cout(w9));
    fac fac10(.a(a[10]), .b(b[10]), .cin(w9), .out(out[10]), .cout(w10));
    fac fac11(.a(a[11]), .b(b[11]), .cin(w10), .out(out[11]), .cout(w11));
    fac fac12(.a(a[12]), .b(b[12]), .cin(w11), .out(out[12]), .cout(w12));
    fac fac13(.a(a[13]), .b(b[13]), .cin(w12), .out(out[13]), .cout(w13));
    fac fac14(.a(a[14]), .b(b[14]), .cin(w13), .out(out[14]), .cout(w14));
    fac fac15(.a(a[15]), .b(b[15]), .cin(w14), .out(out[15]), .cout(w15));

    fac fac16(.a(a[16]), .b(b[16]), .cin(w15), .out(out[16]), .cout());

    assign out_add = out;
endmodule
// face adunare paralela, bit cu bit

module parallel_adder_tb;
    // Inputs
    reg cin;
    reg [16:0] a, b;

    // Outputs
    wire [16:0] out_add;

    // Instantiate the parallel adder module
    parallel_adder dut (
        .cin(cin),
        .a(a),
        .b(b),
        .out_add(out_add)
    );

    // Test stimulus
    initial begin
        // Initialize inputs
        cin = 0;
        a = 17'b00000000_000000001;  //Introducem numerele pentru adunare
        b = 17'b00000000_000000011;

        // Apply inputs and display outputs
        #1500;
        $display("\n\nInput: Numarul_1 = %d, Numarul_2 = %d", a, b);
        $display("Output: SUMA = %d\n\n", out_add);


    end

endmodule

