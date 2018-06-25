/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

#pragma once

class DexField;
class DexMethod;

#include "boost/variant.hpp"

#include <functional>

namespace JarLoaderUtil {
uint32_t read32(uint8_t*& buffer);
uint32_t read16(uint8_t*& buffer);
};

using attribute_hook_t =
    std::function<void(boost::variant<DexField*, DexMethod*> field_or_method,
                       const char* attribute_name,
                       uint8_t* attribute_pointer)>;

bool load_jar_file(const char* location,
                   Scope* classes = nullptr,
                   attribute_hook_t = nullptr);

bool load_class_file(const std::string& filename, Scope* classes = nullptr);
