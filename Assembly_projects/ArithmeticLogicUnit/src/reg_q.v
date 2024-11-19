module reg_q(
  input clk, rst_b, c1, c5, c6, c7, s,
  input [15:0] inbus, 
  output reg [15:0]out, outbus
);

  always @(posedge clk, negedge rst_b)
    if(!rst_b)  out <= 0;
    else if(c1) out <= {inbus[14:1], 1'b0};
    else if(c5) out <= {out[14:1], ~s};
    else if(c6) out <= {out[13:0], 1'b0};
      
  always @(*)
    outbus = (c7) ? out : 16'bz;
    
endmodule
// salveaza deimpartitul