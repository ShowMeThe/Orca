//
// Created by Ken on 2021/1/28.
//

#ifndef ORCA_CORE_UTIL_H
#define ORCA_CORE_UTIL_H

#include <string>
#include <sstream>


template<typename T>
std::string to_string(const T &n) {
    std::ostringstream stream;
    stream << n;
    return stream.str();
}

#endif //ORCA_CORE_UTIL_H
