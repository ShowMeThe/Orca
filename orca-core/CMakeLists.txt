
cmake_minimum_required(VERSION 3.4.1)

add_library(
        core-client

        SHARED

        src/main/cpp/core-client.cpp
        src/main/cpp/core-enviroment.cpp
)

include_directories(src/main/cpp/include)

find_library(
              log-lib
              log )

target_link_libraries(
        core-client
                     ${log-lib} )