module reg_a(
  input clk, rst_b, c0, c3, c8, c6, q7,
  input [15:0] inbus,
  input [15:0] adder_input,
  output reg [15:0]out, outbus
);

  always @(posedge clk, negedge rst_b)
    if(!rst_b)  out <= 0;
    else if(c0) out <= inbus;
    else if(c3) out <= adder_input;
    else if(c6) out <= {out[14:0], q7}; //shiftare
      
  always @(*)
    outbus = (c8) ? out : 16'bz;
    
endmodule
// salveaza rezultatul impartirii
// si il scoate in outbus paralel atat catul cat si restul