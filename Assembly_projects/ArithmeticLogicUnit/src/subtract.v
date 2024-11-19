module subtract( 
	input[15:0] a, b,  
	output[15:0] result 
); 

wire[15:0] outputExor;

exor_w #(16) resultExor (
	.numar(b),   
	.select(1), 
	.exor(outputExor)
);

parallel_adder dut (
        .cin(1),
        .a(a),
        .b(outputExor),
        .out_add(result)
); 
endmodule 

module subtract_tb;
    // Inputs
    reg [15:0] a, b;

    // Outputs
    wire [15:0] subtractResult;

    // Instantiate the substract module
    subtract dut( 
	.a(a), 
	.b(b), 
	.result(subtractResult) 
	);

    // Test stimulus
    initial begin
        // Initialize inputs
        a = 16'b00000000_00001001;  //Introducem numerele pentru scadere
        b = 16'b00000000_00000010;

        // Apply inputs and display outputs
        #1500;
        $display("\n\nInput: Numarul_1 = %d, Numarul_2 = %d", a, b);
        $display("Output: DIFERENTA = %d\n\n", subtractResult);

    end
endmodule 