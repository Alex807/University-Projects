module counter(
  input clk, rst_b, c0, c6,
  output reg [2:0] out
);

always @(posedge clk, negedge rst_b)
  if(!rst_b) out <= 0;
  else if(c0) out <= 0;
  else if(c6) out <= out + 1;
    
endmodule

// contorizeaza numarul de pasi efectuati