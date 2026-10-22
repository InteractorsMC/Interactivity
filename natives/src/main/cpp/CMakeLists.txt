cmake_minimum_required(VERSION 3.16)
project(interactivity_natives LANGUAGES CXX)

# Require C++17 (standard for JoltPhysics)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Find Java JNI headers from JAVA_HOME
find_package(JNI REQUIRED)

# Fallback if project opened outside of gradle
if(NOT JOLT_INCLUDE_DIR)
    set(JOLT_INCLUDE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../build/jolt/include")
endif()
if(NOT JOLT_LIB_DIR)
    set(JOLT_LIB_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../build/jolt/lib")
endif()

# Detect Target Platform Directory
if(WIN32)
    if(CMAKE_SYSTEM_PROCESSOR MATCHES "ARM64|aarch64")
        set(PLATFORM_DIR "windows-aarch64")
    else()
        set(PLATFORM_DIR "windows-x86_64")
    endif()
elseif(APPLE)
    if(CMAKE_SYSTEM_PROCESSOR MATCHES "ARM64|arm64|aarch64")
        set(PLATFORM_DIR "macos-aarch64")
    else()
        set(PLATFORM_DIR "macos-x86_64")
    endif()
elseif(UNIX)
    if(CMAKE_SYSTEM_PROCESSOR MATCHES "ARM64|aarch64")
        set(PLATFORM_DIR "linux-aarch64")
    else()
        set(PLATFORM_DIR "linux-x86_64")
    endif()
endif()

message(STATUS "Configuring Interactivity Natives for target: ${PLATFORM_DIR}")

add_library(joltjni SHARED IMPORTED)
if(WIN32)
    set_target_properties(joltjni PROPERTIES
        IMPORTED_LOCATION "${JOLT_LIB_DIR}/joltjni.dll"
        IMPORTED_IMPLIB "${JOLT_LIB_DIR}/joltjni.lib"
        INTERFACE_INCLUDE_DIRECTORIES "${JOLT_INCLUDE_DIR}"
    )
elseif(APPLE)
    set_target_properties(joltjni PROPERTIES
        IMPORTED_LOCATION "${JOLT_LIB_DIR}/libjoltjni.dylib"
        INTERFACE_INCLUDE_DIRECTORIES "${JOLT_INCLUDE_DIR}"
    )
else()
    set_target_properties(joltjni PROPERTIES
        IMPORTED_LOCATION "${JOLT_LIB_DIR}/libjoltjni.so"
        INTERFACE_INCLUDE_DIRECTORIES "${JOLT_INCLUDE_DIR}"
    )
endif()

# Define Shared Library
add_library(interactivity_natives SHARED
    src/test_jni.cpp
    src/physics_sync.cpp
)

target_include_directories(interactivity_natives PRIVATE
    include
    ${JOLT_INCLUDE_DIR}
    ${JNI_INCLUDE_DIRS}
)

target_compile_definitions(interactivity_natives PRIVATE
    JPH_DOUBLE_PRECISION
    JPH_SHARED_LIBRARY
    JPH_OBJECT_STREAM
    JPH_USE_CPU_COMPUTE
    $<$<CONFIG:Release>:JPH_NO_DEBUG>
)

target_link_libraries(interactivity_natives PRIVATE
    joltjni
)

set_target_properties(interactivity_natives PROPERTIES
    OUTPUT_NAME "interactivity_natives"
)

if(MSVC)
    target_compile_options(interactivity_natives PRIVATE /GR- /EHs- /W4)
    set_target_properties(interactivity_natives PROPERTIES
        PREFIX ""
        MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>"
    )
elseif(MINGW)
    set_target_properties(interactivity_natives PROPERTIES
        PREFIX ""
    )
    target_compile_options(interactivity_natives PRIVATE -fno-rtti -fno-exceptions -Wall -Wextra)
    target_link_options(interactivity_natives PRIVATE -static-libgcc -static-libstdc++)
elseif(APPLE)
    set_target_properties(interactivity_natives PROPERTIES
        PREFIX "lib"
    )
else()
    set_target_properties(interactivity_natives PROPERTIES
        PREFIX "lib"
    )
    target_compile_options(interactivity_natives PRIVATE -fno-rtti -fno-exceptions -Wall -Wextra)
    target_link_options(interactivity_natives PRIVATE -static-libgcc -static-libstdc++)
endif()

# Destination directory in Java resources (now located at ../resources/natives/${PLATFORM_DIR})
set(RESOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../resources/natives/${PLATFORM_DIR}")

# Post-build copy to automatically place the binary inside Java resources for Gradle jar packaging
add_custom_command(TARGET interactivity_natives POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E make_directory "${RESOURCE_DIR}"
    COMMAND ${CMAKE_COMMAND} -E copy_if_different
        "$<TARGET_FILE:interactivity_natives>"
        "${RESOURCE_DIR}/$<TARGET_FILE_NAME:interactivity_natives>"
    COMMENT "Copying compiled native library to Java resources: ${RESOURCE_DIR}"
)