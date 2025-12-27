cmake_minimum_required(VERSION 3.22.1)

project("scamdetectorapp")

add_library(
        scamdetectorapp
        SHARED
        native-lib.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        scamdetectorapp
        ${log-lib})
