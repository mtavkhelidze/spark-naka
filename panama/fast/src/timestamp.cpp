#include <fast/fast.h>

#include "parse_iso8601.h"

// Howard Hinnant's algorithm, branchless
inline auto to_epoch(const int y, const int m, const int d) -> int64_t {
    int year = y - static_cast<int>(m <= 2);
    int era = (year - (year < 0) * 399) / 400;
    int yoe = year - era * 400;
    int doy = (153 * (m + (m <= 2) * 12 - 3) + 2) / 5 + d - 1;
    int doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
    int64_t days = era * 146097LL + doe - 719468;
    return days * 86400LL;
}

auto fast::unix_timestamp(sized_bytes_t* bytes, int64_t* out) -> err_t {
    auto dates = fast::dates_t();
    dates.y.resize(bytes->n_rows);
    dates.m.resize(bytes->n_rows);
    dates.d.resize(bytes->n_rows);

    if (auto err = parse_iso8601(bytes, &dates); err != err_t::ok) return err;

    for (uint16_t i = 0; i < bytes->n_rows; ++i) {
        out[i] = to_epoch(dates.y[i], dates.m[i], dates.d[i]);
    }
    return err_t::ok;
}
