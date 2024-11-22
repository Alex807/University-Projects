module nonRD(
    input clk, rst_b, 
    input [15:0] inbus,
    output [15:0] outbus
);
    wire[15:0] reg_a, reg_m, reg_q;
    wire[16:0] adder_output;
    wire[2:0] cnt;
    wire reg_s;
    wire c0, c1, c2, c3, c4, c5, c6, c7, c8;

    control_unit inst7(.clk(clk),
                    .rst_b(rst_b),
                    .s(reg_s),
                    .is_count_7(cnt == 3'b111),
                    .c0(c0),
                    .c1(c1),
                    .c2(c2),
                    .c3(c3),
                    .c4(c4),
                    .c5(c5),
                    .c6(c6),
                    .c7(c7),
                    .c8(c8));

    reg_m inst0(.clk(clk),
            .rst_b(rst_b),
            .c2(c2),
            .inbus(inbus),
            .out(reg_m));

    reg_q inst1(.clk(clk),
            .rst_b(rst_b),
            .c1(c1),
            .c5(c5),
            .c6(c6),
            .c7(c7),
            .s(~s),
            .inbus(inbus[15:0]),
            .out(reg_q),
            .outbus(outbus));

    reg_a inst2(.clk(clk),
            .rst_b(rst_b),
            .c0(c0),
            .c3(c3),
            .c6(c6),
            .c8(c8),
            .q7(reg_q[7]),
            .inbus(inbus),
            .adder_input(adder_output[15:0]),
            .out(reg_a),
            .outbus(outbus));

    reg_s inst3(.clk(clk),
                .rst_b(rst_b),
                .adder_input(adder_output[16]),
                .a7(reg_a[15]),
                .c0(c0),
                .c3(c3),
                .c6(c6),
                .out(reg_s));

    counter inst4(.clk(clk),
                .rst_b(rst_b),
                .c0(c0),
                .c6(c6),
                .out(cnt));

    parallel_adder inst6(.cin(c4),
                         .a({reg_s, reg_a}),
                         .b({reg_m[15], reg_m} ^ {17{c4}}),
                         .out_add(adder_output));

endmodule

module nonRD_tb;
    reg clk, rst_b;
    reg [15:0] inbus;
    wire [15:0] outbus;

    nonRD dut(.clk(clk),
                 .rst_b(rst_b),
                 .inbus(inbus),
                 .outbus(outbus));
                     
    localparam CLOCK_CYCLES = 160, CLOCK_PERIOD = 100;
    localparam RST_PULSE = 25;

    initial begin 
        clk = 0;
        repeat(CLOCK_CYCLES * 2)
            #(CLOCK_PERIOD / 2) clk = ~clk;
    end

    initial begin 
        rst_b = 0;
        #(RST_PULSE) rst_b = 1;
    end

    initial begin 
          inbus = 16'b0; //prima jumatate deimpartit
          #(200)

          inbus = 16'b0000_0000_0001_0000; //a doua jumatate => deimpartit 
          #(200)
          inbus = 16'b0000_0000_0000_0010; //divisor 
          #(200)
 
          inbus = 16'bz;
          #(1200) 
          inbus = 16'bz;
    end
endmodule 