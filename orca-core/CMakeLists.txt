
cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_CONFIGURATION_TYPES Release)
add_library(
        core-client

        SHARED

        src/main/cpp/core-client.cpp
        src/main/cpp/core-environment.cpp
        src/main/cpp/core-encryption.cpp
        src/main/cpp/core-come-true.cpp
)

include_directories(src/main/cpp/include)

find_library(
              log-lib
              log )

target_link_libraries(
        core-client
                     ${log-lib} )