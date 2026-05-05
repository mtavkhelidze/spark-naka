#ifndef __NAKAA_BATCH_H__
#define __NAKAA_BATCH_H__

#include <inttypes.h>

constexpr uint8_t TYPE_LONG = 1 << 0;
constexpr uint8_t TYPE_STR = 1 << 1;
constexpr inline auto is_long(uint8_t t) -> bool { return t & TYPE_LONG; }
constexpr inline auto is_str(uint8_t t) -> bool { return t & TYPE_STR; }

typedef struct {
    uint8_t type;
    uint8_t _pad1[3];
    int64_t* data;
    uint64_t _pad2[2];
} col_long_t;

typedef struct {
    uint8_t type;
    uint8_t _pad1[7];
    uint8_t* data;
    uint32_t* offs;
    uint8_t _pad2[8];
} col_str_t;

typedef struct {
    uint32_t n_cols;
    uint32_t n_rows;
    uint32_t cols[];
} batch_t;

constexpr inline int col_stride(uint32_t i) {
    return i * sizeof(col_long_t) / sizeof(uint32_t);
}

#define NAKA_FN(name) extern "C" auto name(batch_t* in, batch_t* out) -> int32_t

static_assert(
  sizeof(col_long_t) == sizeof(col_str_t),
  "col_long_t and col_str_t have different sizes"
);
static_assert(sizeof(batch_t) == 8, "header size isn't 8");
static_assert(sizeof(col_long_t) == 32, "col size isn't 32");
static_assert(sizeof(col_str_t) == 32, "col size isn't 32");

inline auto get_type(uint32_t col) -> uint8_t {
    return *reinterpret_cast<uint8_t*>(&col);
}

inline auto is_num_col(uint32_t p) -> bool { return is_long(get_type(p)); }
inline auto is_str_col(uint32_t p) -> bool { return is_str(get_type(p)); }

#endif  // __NAKAA_BATCH_H__
