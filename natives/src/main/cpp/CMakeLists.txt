cmake_minimum_required(VERSION 3.16)
project(interactivity_natives LANGUAGES CXX)

# Require C++17 (standard for JoltPhysics)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Find Java JNI headers from JAVA_HOME
find_package(JNI REQUIRED)

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

# Define Shared Library
add_library(interactivity_natives SHARED
    src/test_jni.cpp
)

target_include_directories(interactivity_natives PRIVATE
    include
    ${JNI_INCLUDE_DIRS}
)

# Best practices for Minecraft mod JNI distribution:
# Statically link C/C++ runtime so end users don't need Visual C++ Redistributable installed!
if(MSVC)
    set_property(TARGET interactivity_natives PROPERTY
        MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
    # Disable prefix "lib" on Windows, output name: interactivity_natives.dll
    set_target_properties(interactivity_natives PROPERTIES
        PREFIX ""
        OUTPUT_NAME "interactivity_natives"
    )
elseif(MINGW OR (UNIX AND NOT APPLE))
    # Static libgcc / libstdc++ on MinGW / GCC for maximum portability
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
