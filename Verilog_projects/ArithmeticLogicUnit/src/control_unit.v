module control_unit(
    input clk, rst_b, s, is_count_7,
    output c0, c1, c2, c3, c4, c5, c6, c7, c8
);
    localparam S0 = 0, S1 = 1, S2 = 2, S3 = 3, S4 = 4, S5 = 5;
    localparam S6 = 6, S7 = 7, S8 = 8, S9 = 9, S10 = 10, S11 = 11, S12 = 12, S13 = 13, S14 = 14;
    reg[3:0]state, state_next;
    
    always @(*) begin 
      case(state) 
        S0: state_next = S1;
        S1: state_next = S2;
        S2: state_next = S3;
        S3: state_next = S4;
        S4: if (s) state_next = S6;
            else state_next = S5;
        S5: state_next = S7;
        S6: state_next = S7;
        S7: state_next = S8;
        S8: if(is_count_7) state_next = S10;
          else state_next = S9;
        S9: state_next = S4;
        S10: if(s) state_next = S11;
            else state_next = S12;
        S11: state_next = S12;
        S12: state_next = S13;
        S13: state_next = S14;
      endcase
    end
    
    always @(posedge clk, negedge rst_b)
      if(!rst_b) state <= S0;
      else state <= state_next;
        
    assign c0 = (state == S1);
    assign c1 = (state == S2);
    assign c2 = (state == S3);
    assign c3 = (state == S5 || state == S6 || state == S11);
    assign c4 = (state == S5);
    assign c5 = (state == S7);
    assign c6 = (state == S9);
    assign c7 = (state == S12);
    assign c8 = (state == S13);
        
endmodule