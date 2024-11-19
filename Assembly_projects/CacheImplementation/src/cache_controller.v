module cache_controller (
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
    // Cache configuration parameters
    localparam LINE_SIZE = 16; // Cache line size in bytes
    localparam CACHE_SIZE = 1024; // Total cache size in bytes
    localparam ASSOCIATIVITY = 4; // Cache associativity

    // Calculating the number of bits for index, offset, and tag
    localparam OFFSET_BITS = $clog2(LINE_SIZE);
    localparam INDEX_BITS = $clog2(CACHE_SIZE / (LINE_SIZE * ASSOCIATIVITY));
    localparam TAG_BITS = 32 - INDEX_BITS - OFFSET_BITS;

    // Address decomposition
    wire [TAG_BITS-1:0] tag = address[31:32-TAG_BITS];
    wire [INDEX_BITS-1:0] index = address[OFFSET_BITS+INDEX_BITS-1:OFFSET_BITS];
    wire [OFFSET_BITS-1:0] offset = address[OFFSET_BITS-1:0];

    // Internal signals
    wire [31:0] cache_read_data;
    wire cache_hit, cache_miss, cache_evict;
    wire [31:0] cache_evict_data;
    wire [31:0] cache_evict_address;

    // Instantiate cache memory and LRU manager
    cache_memory u_cache_memory (
        .clk(clk),
        .reset(reset),
        .read(read),
        .write(write),
        .address(address),
        .write_data(write_data),
        .read_data(cache_read_data),
        .hit(cache_hit),
        .miss(cache_miss),
        .evict(cache_evict),
        .evict_data(cache_evict_data),
        .evict_address(cache_evict_address)
    );

    lru_manager u_lru_manager (
        .clk(clk),
        .reset(reset),
        .access(read || write),
        .index(index),
        .lru_way(lru_way)
    );

    // FSM state definitions using localparam
    localparam IDLE        = 3'b000;
    localparam READ_HIT    = 3'b001;
    localparam READ_MISS   = 3'b010;
    localparam WRITE_HIT   = 3'b011;
    localparam WRITE_MISS  = 3'b100;
    localparam EVICT       = 3'b101;
    localparam ALLOCATE    = 3'b110;
    localparam WRITE_BACK  = 3'b111;

    reg [2:0] current_state, next_state;

    // FSM logic
    always @(posedge clk or posedge reset) begin
        if (reset) begin
            current_state <= IDLE;
        end else begin
            current_state <= next_state;
        end
    end

    always @(*) begin
        // Default values
        next_state = current_state;
        hit = 0;
        miss = 0;
        evict = 0;

        case (current_state)
            IDLE: begin
                if (read || write) begin
                    if (cache_hit) begin
                        if (read) next_state = READ_HIT;
                        else next_state = WRITE_HIT;
                    end else begin
                        if (read) next_state = READ_MISS;
                        else next_state = WRITE_MISS;
                    end
                end
            end
            READ_HIT: begin
                hit = 1;
                read_data = cache_read_data;
                next_state = IDLE;
            end
            WRITE_HIT: begin
                hit = 1;
                next_state = IDLE;
            end
            READ_MISS: begin
                miss = 1;
                if (cache_evict) next_state = EVICT;
                else next_state = ALLOCATE;
            end
            WRITE_MISS: begin
                miss = 1;
                if (cache_evict) next_state = EVICT;
                else next_state = ALLOCATE;
            end
            EVICT: begin
                evict = 1;
                evict_data = cache_evict_data;
                evict_address = cache_evict_address;
                next_state = WRITE_BACK;
            end
            WRITE_BACK: begin
                next_state = ALLOCATE;
            end
            ALLOCATE: begin
                next_state = IDLE;
            end
        endcase
    end
endmodule

module cache_controller_tb;

    // Declare testbench signals
    reg clk;                  // Clock signal
    reg reset;                // Reset signal
    reg read;                 // Read request signal
    reg write;                // Write request signal
    reg [31:0] address;       // Address for read/write
    reg [31:0] write_data;    // Data to write to cache
    wire [31:0] read_data;    // Data read from cache
    wire hit;                 // Cache hit signal
    wire miss;                // Cache miss signal
    wire evict;               // Eviction signal
    wire [31:0] evict_data;   // Data being evicted
    wire [31:0] evict_address;// Address of the data being evicted

    // Instantiate the cache controller
    cache_controller uut (
        .clk(clk),
        .reset(reset),
        .read(read),
        .write(write),
        .address(address),
        .write_data(write_data),
        .read_data(read_data),
        .hit(hit),
        .miss(miss),
        .evict(evict),
        .evict_data(evict_data),
        .evict_address(evict_address)
    );

    // Clock generation: Toggle clock every 5 time units
    always #5 clk = ~clk;

    // Task to perform a read operation
    task do_read;
        input [31:0] addr; // Address to read from
        begin
            @(posedge clk); // Wait for the positive edge of the clock
            read = 1;       // Set read signal
            write = 0;      // Clear write signal
            address = addr; // Set address
            @(posedge clk); // Wait for the next positive edge of the clock
            read = 0;       // Clear read signal
        end
    endtask

    // Task to perform a write operation
    task do_write;
        input [31:0] addr; // Address to write to
        input [31:0] data; // Data to write
        begin
            @(posedge clk); // Wait for the positive edge of the clock
            read = 0;       // Clear read signal
            write = 1;      // Set write signal
            address = addr; // Set address
            write_data = data; // Set data to write
            @(posedge clk); // Wait for the next positive edge of the clock
            write = 0;      // Clear write signal
        end
    endtask

    initial begin
        // Initialize signals
        clk = 0;          // Initialize clock to 0
        reset = 1;        // Assert reset signal
        read = 0;         // Clear read signal
        write = 0;        // Clear write signal
        address = 0;      // Initialize address to 0
        write_data = 0;   // Initialize write data to 0

        // Reset the system
        @(posedge clk);   // Wait for the positive edge of the clock
        reset = 0;        // Deassert reset signal

        // Perform a series of read and write operations
        $display("Starting simulation...");

        // Write data to various addresses
        do_write(32'h00000000, 32'hAAAAAAAA); // Write 0xAAAAAAAA to address 0
        do_write(32'h00000010, 32'hBBBBBBBB); // Write 0xBBBBBBBB to address 16
        do_write(32'h00000020, 32'hCCCCCCCC); // Write 0xCCCCCCCC to address 32
        do_write(32'h00000030, 32'hDDDDDDDD); // Write 0xDDDDDDDD to address 48

        // Read data from the same addresses
        do_read(32'h00000000); // Read from address 0
        if (read_data != 32'hAAAAAAAA) $display("Read Error at address 0"); // Verify data
        do_read(32'h00000010); // Read from address 16
        if (read_data != 32'hBBBBBBBB) $display("Read Error at address 16"); // Verify data
        do_read(32'h00000020); // Read from address 32
        if (read_data != 32'hCCCCCCCC) $display("Read Error at address 32"); // Verify data
        do_read(32'h00000030); // Read from address 48
        if (read_data != 32'hDDDDDDDD) $display("Read Error at address 48"); // Verify data

        // Test for cache eviction
        // Fill the cache to force eviction
        do_write(32'h00000040, 32'hEEEEEEEE); // Write 0xEEEEEEEE to address 64
        do_write(32'h00000050, 32'hFFFFFFFF); // Write 0xFFFFFFFF to address 80
        do_write(32'h00000060, 32'h11111111); // Write 0x11111111 to address 96
        do_write(32'h00000070, 32'h22222222); // Write 0x22222222 to address 112

        // Access a new address to cause eviction
        do_write(32'h00000080, 32'h33333333); // Write 0x33333333 to address 128

        // Check eviction happened correctly
        if (!evict) $display("Eviction did not occur as expected");
        else $display("Eviction occurred for address: %h", evict_address);

        $display("Simulation completed.");
        $finish; // End simulation
    end
endmodule
