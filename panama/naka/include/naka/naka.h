#ifndef __NAKA_NAKA_H__
#define __NAKA_NAKA_H__
#pragma once
#include <fast/fast.h>

#include <cstdint>
#include <iostream>

using varbyte_t = fast::sized_bytes_t;
using fixnum_t = fast::long_longs_t;

#ifdef __cplusplus
extern "C" {
#endif  // __cplusplus

auto epoch(varbyte_t* in, fixnum_t* out, size_t out_size) -> int32_t;

#ifdef __cplusplus
}
#endif  // __cplusplus

#endif  // __NAKA_NAKA_H__
