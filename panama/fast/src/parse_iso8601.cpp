#include "parse_iso8601.h"

struct offsets_t {
    int y, m, d;
};

inline auto detect_offsets(const uint8_t* p, int len) -> offsets_t {
    // yyyy-mm-dd canonical fallback
    if (len == 10 && (p[4] == '-' || p[4] == '/')) return {0, 5, 8};
    // dd/mm/yyyy or dd-mm-yyyy or dd/mm/yy
    return {6, 3, 0};
}

inline auto day(const uint8_t* p, int d) -> int {
    return ((p[d] - '0') * 10) + (p[d + 1] - '0');
}
inline auto month(const uint8_t* p, int m) -> int {
    return ((p[m] - '0') * 10) + (p[m + 1] - '0');
}

inline auto year(const uint8_t* p, int y) -> int {
    return ((p[y] - '0') * 1000)
         + ((p[y + 1] - '0') * 100)
         + ((p[y + 2] - '0') * 10)
         + (p[y + 3] - '0');
}

inline auto year2(const uint8_t* p, int y) -> int {
    return ((p[y] - '0') * 10) + (p[y + 1] - '0') + 1900;
}

auto fast::parse_iso8601(fast::sized_bytes_t* in, fast::dates_t* out) -> err_t {
    if (in == nullptr || out == nullptr) return err_t::null_input;
    if (in->n_rows != out->y.size()) return err_t::size_mismatch;

    for (uint16_t i = 0; i < in->n_rows; ++i) {
        const auto* ptr = (*in)(i);
        const auto len = (*in)[i];
        auto [y, m, d] = detect_offsets(ptr, len);
        out->y[i] = len == 8 ? year2(ptr, y) : year(ptr, y);
        out->m[i] = month(ptr, m);
        out->d[i] = day(ptr, d);
    }
    return err_t::ok;
}
