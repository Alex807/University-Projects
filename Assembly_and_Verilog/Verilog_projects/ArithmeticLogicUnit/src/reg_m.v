module reg_m (
    input clk, rst_b, c2,
    input [15:0] inbus,
    output reg [15:0] out
);
    always @(posedge clk, negedge rst_b)
        if(!rst_b)  out <= 0;
        else if(c2) out <= inbus;
endmodule
// salveaz impartitorul