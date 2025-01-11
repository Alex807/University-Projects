module multiply_BRadix4 (
    input [15:0] multiplicand,
    input [15:0] multiplier,
    output reg [31:0] product
);

reg [15:0] a;
reg [32:0] s;

always @(*) begin
    a = multiplicand;
    s = {16'b0,multiplier,1'b0};

    repeat(16) begin
        if (s[1:0] == 2'b01) begin
            s = s + {a,17'b0}; //mut a cu 8 pt suma cu biti prod

        end else if (s[1:0] == 2'b10) begin
            s = s - {a,17'b0};
        end

        // Shiftam la dreapta folosind shift arithmetic pentru a pastra semnul
        s = {s[32], s} >> 1;
    end
	product = s[32:1]; //trunchiem un bit adica cel adaugat mai sus
end

endmodule

module multiply_BRadix4_tb;
    reg [15:0] multiplicand, multiplier;

    wire [31:0] product;

    multiply_BRadix4 dut (
        .multiplicand(multiplicand),
        .multiplier(multiplier),
        .product(product)
    );

    initial begin //Introducere date 
        multiplicand = 16'b00000000_00000010; 
        multiplier   = 16'b00000000_00000011;

        #1500;

        $display("\n\nInput: Multiplicand = %d,  Multiplier = %d ", multiplicand, multiplier);
        $display("PRODUS = %d\n\n", product);        
    end
endmodule 