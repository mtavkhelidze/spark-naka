#ifndef __PARSE_ISO8601_H__
#define __PARSE_ISO8601_H__

#include <fast/fast.h>

#include <vector>

namespace fast {
struct dates_t {
    std::vector<int16_t> y;
    std::vector<int16_t> m;
    std::vector<int16_t> d;
};

auto parse_iso8601(sized_bytes_t* in, dates_t* out) -> err_t;

}  // namespace fast
#endif  // __PARSE_ISO8601_H__
