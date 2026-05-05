#ifndef __FAST_FAST_H__
#define __FAST_FAST_H__

#include <cstdint>
#include <vector>

namespace fast {

struct sized_bytes_t {
    uint16_t n_rows;
    uint8_t data[];
    inline auto bytes() -> uint8_t* {
        return data + (n_rows + 1) * sizeof(uint16_t);
    }
    inline auto offset(int32_t index) -> uint16_t& {
        return *reinterpret_cast<uint16_t*>(data + index * sizeof(uint16_t));
    }
    // length
    inline auto operator[](int32_t index) -> uint16_t {
        return this->offset(index + 1) - this->offset(index);
    }
    // bytes
    inline auto operator()(int32_t index) -> uint8_t* {
        return bytes() + this->offset(index);
    }
};

struct long_longs_t {
    // number of rows (data members)
    uint16_t n_rows;
    // I'll pad myself
    uint8_t _pad[sizeof(int64_t) - sizeof(uint16_t)];
    // data: continuous longs
    int64_t data[];
};

enum class err_t : int32_t {
    ok = 0,
    size_mismatch = -1,
    invalid_date = -2,
    null_input = -3,
};

auto unix_timestamp(sized_bytes_t* bytes, int64_t* out) -> err_t;

}  // namespace fast

#endif
