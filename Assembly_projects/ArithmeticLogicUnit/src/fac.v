module fac(
    input a, b, cin,
    output out, cout
);
    assign out = (a ^ b) ^ cin;
    assign cout = (a & b) ^ ((a ^ b) & cin);
endmodule