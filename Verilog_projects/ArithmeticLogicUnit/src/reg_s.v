module reg_s(
  input a7, c0, c3, c6, clk, rst_b, adder_input,
  output reg out
);

always @(posedge clk, negedge rst_b)
  if (!rst_b) out <= 0;
  else if (c0) out <= 0;
  else if (c3) out <= adder_input;
  else if (c6) out <= a7;
    
endmodule

// salveaza un singur bit si anume bitul de semn 