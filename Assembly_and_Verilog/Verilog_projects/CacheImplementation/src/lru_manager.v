module lru_manager (
    input clk,
    input reset,
    input access,
    input [($clog2(NUM_SETS))-1:0] index,
    output reg [1:0] lru_way
);
    parameter NUM_SETS = 128;
    parameter ASSOC = 4;
    localparam INDEX_BITS = $clog2(NUM_SETS);

    reg [1:0] lru_counter[NUM_SETS-1:0][ASSOC-1:0];

    integer way;

    // Reset and update LRU counters
    always @(posedge clk or posedge reset) begin : LRU_UPDATE
        integer i, j;
        if (reset) begin
            for (i = 0; i < NUM_SETS; i = i + 1) begin
                for (j = 0; j < ASSOC; j = j + 1) begin
                    lru_counter[i][j] <= j;
                end
            end
        end else if (access) begin
            for (way = 0; way < ASSOC; way = way + 1) begin
                if (lru_counter[index][way] == 0) begin
                    lru_way = way;
                end
            end
            for (way = 0; way < ASSOC; way = way + 1) begin
                if (lru_counter[index][way] > 0) begin
                    lru_counter[index][way] = lru_counter[index][way] - 1;
                end
            end
            lru_counter[index][lru_way] = ASSOC - 1;
        end
    end
endmodule

