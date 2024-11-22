
module cache_memory (
    input clk,
    input reset,
    input read,
    input write,
    input [31:0] address,
    input [31:0] write_data,
    output reg [31:0] read_data,
    output reg hit,
    output reg miss,
    output reg evict,
    output reg [31:0] evict_data,
    output reg [31:0] evict_address
);
    parameter CACHE_SIZE = 32 * 1024; // 32 KB
    parameter BLOCK_SIZE = 64;        // 64 bytes
    parameter NUM_SETS = 128;         // 128 sets
    parameter ASSOC = 4;              // 4-way set associative
    parameter INDEX_BITS = $clog2(NUM_SETS);
    parameter OFFSET_BITS = $clog2(BLOCK_SIZE);
    parameter TAG_BITS = 32 - INDEX_BITS - OFFSET_BITS;

    // Cache data storage: Each set has 'ASSOC' number of ways, each way stores 'BLOCK_SIZE/4' number of 32-bit words
    reg [31:0] cache_data[NUM_SETS-1:0][ASSOC-1:0][BLOCK_SIZE/4-1:0]; 
    // Cache tag storage: Each set has 'ASSOC' number of ways
    reg [TAG_BITS-1:0] cache_tags[NUM_SETS-1:0][ASSOC-1:0];           
    // Valid bit storage: Each set has 'ASSOC' number of ways
    reg valid_bits[NUM_SETS-1:0][ASSOC-1:0];                         
    // Dirty bit storage: Each set has 'ASSOC' number of ways
    reg dirty_bits[NUM_SETS-1:0][ASSOC-1:0];                         

    // Extract tag, index, and offset from the address
    wire [TAG_BITS-1:0] tag = address[31:32-TAG_BITS];
    wire [INDEX_BITS-1:0] index = address[INDEX_BITS+OFFSET_BITS-1:OFFSET_BITS];
    wire [OFFSET_BITS-1:0] offset = address[OFFSET_BITS-1:0];

    integer way, i;  // Loop variables
    reg found, evict_needed;  // Flags to track cache hit and eviction requirement
    reg [1:0] lru_counter[NUM_SETS-1:0][ASSOC-1:0];  // LRU counters for replacement policy
    reg [1:0] lru_way;  // Way to be evicted based on LRU policy

    // Reset logic: Initialize all valid bits, dirty bits, and LRU counters
    always @(posedge clk or posedge reset) begin
        if (reset) begin
            for (i = 0; i < NUM_SETS; i = i + 1) begin
                for (way = 0; way < ASSOC; way = way + 1) begin
                    valid_bits[i][way] <= 0;
                    dirty_bits[i][way] <= 0;
                    lru_counter[i][way] <= way;
                end
            end
        end else begin
            if (read || write) begin
                found = 0;
                for (way = 0; way < ASSOC; way = way + 1) begin
                    if (valid_bits[index][way] && cache_tags[index][way] == tag) begin
                        found = 1;
                        if (read) begin
                            read_data <= cache_data[index][way][offset/4];
                        end else if (write) begin
                            cache_data[index][way][offset/4] <= write_data;
                            dirty_bits[index][way] <= 1;
                        end
                        lru_counter[index][way] <= 0;  // Update LRU counter for the accessed way
                        for (i = 0; i < ASSOC; i = i + 1) begin
                            if (i != way && lru_counter[index][i] < ASSOC - 1) begin
                                lru_counter[index][i] <= lru_counter[index][i] + 1;  // Update LRU counters for other ways
                            end
                        end
                        hit <= 1;
                        miss <= 0;
                        evict <= 0;
                        evict_data <= 0;
                        evict_address <= 0;
                    end
                end
                if (!found) begin
                    hit <= 0;
                    miss <= 1;
                    evict_needed = 1;
                    for (way = 0; way < ASSOC; way = way + 1) begin
                        if (!valid_bits[index][way]) begin
                            evict_needed = 0;
                            cache_tags[index][way] <= tag;
                            valid_bits[index][way] <= 1;
                            if (write) begin
                                cache_data[index][way][offset/4] <= write_data;
                                dirty_bits[index][way] <= 1;
                            end
                            lru_counter[index][way] <= 0;
                            for (i = 0; i < ASSOC; i = i + 1) begin
                                if (i != way && lru_counter[index][i] < ASSOC - 1) begin
                                    lru_counter[index][i] <= lru_counter[index][i] + 1;
                                end
                            end
                            evict <= 0;
                            evict_data <= 0;
                            evict_address <= 0;
                        end
                    end
                    if (evict_needed) begin
                        for (way = 0; way < ASSOC; way = way + 1) begin
                            if (lru_counter[index][way] == ASSOC - 1) begin
                                lru_way <= way;
                                evict <= 1;
                                evict_data <= cache_data[index][way][offset/4];
                                evict_address <= {cache_tags[index][way], index, offset};
                                cache_tags[index][way] <= tag;
                                if (write) begin
                                    cache_data[index][way][offset/4] <= write_data;
                                    dirty_bits[index][way] <= 1;
                                end
                                lru_counter[index][way] <= 0;
                                for (i = 0; i < ASSOC; i = i + 1) begin
                                    if (i != way && lru_counter[index][i] < ASSOC - 1) begin
                                        lru_counter[index][i] <= lru_counter[index][i] + 1;
                                    end
                                end
                            end
                        end
                    end
                end
            end
        end
    end
endmodule
