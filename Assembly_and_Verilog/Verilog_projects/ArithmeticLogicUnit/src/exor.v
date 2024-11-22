module exor_w #(
  parameter size = 16 // numar biti ai cuvântului
)(
  input [size-1:0] numar,
  input select,
  output [size-1:0]exor
);

genvar i;

generate
 for (i = 0; i < size; i = i + 1) begin: un_text
	assign exor[i] = numar[i] ^ select; 
end
endgenerate

endmodule

  
