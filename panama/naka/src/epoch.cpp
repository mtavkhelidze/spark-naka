#include <fast/fast.h>
#include <naka/naka.h>

using namespace fast;

auto epoch(varbyte_t* in, fixnum_t* out, size_t out_size) -> int32_t {
    if (auto err = unix_timestamp(in, out->data); err != err_t::ok) {
        return static_cast<int32_t>(err);
    }
    out->n_rows = in->n_rows;
    return out->n_rows;
}
