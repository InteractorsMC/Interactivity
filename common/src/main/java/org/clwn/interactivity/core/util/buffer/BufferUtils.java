package org.clwn.interactivity.core.util.buffer;

import java.nio.*;

public class BufferUtils {
    /**
     * Copies a range of elements from one direct {@link DoubleBuffer} to another.
     * <p>
     * Sets positions and limits to transfer precisely the requested amount of elements,
     * resetting buffer states upon completion to avoid side effects.
     *
     * @param src    The source buffer.
     * @param dest   The destination buffer.
     * @param length The number of elements to copy.
     */
    public static void copyDoubleBuffer(DoubleBuffer src, DoubleBuffer dest, int length) {
        src.position(0).limit(length);
        dest.position(0).limit(length);
        dest.put(src);
        src.clear();
        dest.clear();
    }

    /**
     * Copies a range of elements from one direct {@link FloatBuffer} to another.
     * <p>
     * Sets positions and limits to transfer precisely the requested amount of elements,
     * resetting buffer states upon completion to avoid side effects.
     *
     * @param src    The source buffer.
     * @param dest   The destination buffer.
     * @param length The number of elements to copy.
     */
    public static void copyFloatBuffer(FloatBuffer src, FloatBuffer dest, int length) {
        src.position(0).limit(length);
        dest.position(0).limit(length);
        dest.put(src);
        src.clear();
        dest.clear();
    }

    /**
     * Copies a range of elements from one direct {@link ByteBuffer} to another.
     * <p>
     * Sets positions and limits to transfer precisely the requested amount of elements,
     * resetting buffer states upon completion to avoid side effects.
     *
     * @param src    The source buffer.
     * @param dest   The destination buffer.
     * @param length The number of elements to copy.
     */
    public static void copyByteBuffer(ByteBuffer src, ByteBuffer dest, int length) {
        src.position(0).limit(length);
        dest.position(0).limit(length);
        dest.put(src);
        src.clear();
        dest.clear();
    }

    /**
     * Copies a range of elements from one direct {@link LongBuffer} to another.
     * <p>
     * Sets positions and limits to transfer precisely the requested amount of elements,
     * resetting buffer states upon completion to avoid side effects.
     *
     * @param src    The source buffer.
     * @param dest   The destination buffer.
     * @param length The number of elements to copy.
     */
    public static void copyLongBuffer(LongBuffer src, LongBuffer dest, int length) {
        src.position(0).limit(length);
        dest.position(0).limit(length);
        dest.put(src);
        src.clear();
        dest.clear();
    }

    /**
     * Copies a range of elements from one direct {@link IntBuffer} to another.
     * <p>
     * Sets positions and limits to transfer precisely the requested amount of elements,
     * resetting buffer states upon completion to avoid side effects.
     *
     * @param src    The source buffer.
     * @param dest   The destination buffer.
     * @param length The number of elements to copy.
     */
    public static void copyIntBuffer(IntBuffer src, IntBuffer dest, int length) {
        src.position(0).limit(length);
        dest.position(0).limit(length);
        dest.put(src);
        src.clear();
        dest.clear();
    }

    /**
     * Copies a range of elements between two direct {@link DoubleBuffer}s at the given offsets.
     * <p>
     * Uses the absolute bulk {@code put}, so positions and limits of both buffers are neither read nor changed.
     *
     * @param src       The source buffer.
     * @param srcOffset Index of the first element to read.
     * @param dest      The destination buffer.
     * @param destOffset Index of the first element to write.
     * @param length    The number of elements to copy.
     */
    public static void copyDoubleBuffer(DoubleBuffer src, int srcOffset, DoubleBuffer dest, int destOffset, int length) {
        dest.put(destOffset, src, srcOffset, length);
    }

    /**
     * Copies a range of elements between two direct {@link FloatBuffer}s at the given offsets.
     * <p>
     * Uses the absolute bulk {@code put}, so positions and limits of both buffers are neither read nor changed.
     *
     * @param src       The source buffer.
     * @param srcOffset Index of the first element to read.
     * @param dest      The destination buffer.
     * @param destOffset Index of the first element to write.
     * @param length    The number of elements to copy.
     */
    public static void copyFloatBuffer(FloatBuffer src, int srcOffset, FloatBuffer dest, int destOffset, int length) {
        dest.put(destOffset, src, srcOffset, length);
    }

    /**
     * Copies a range of elements between two direct {@link ByteBuffer}s at the given offsets.
     * <p>
     * Uses the absolute bulk {@code put}, so positions and limits of both buffers are neither read nor changed.
     *
     * @param src       The source buffer.
     * @param srcOffset Index of the first element to read.
     * @param dest      The destination buffer.
     * @param destOffset Index of the first element to write.
     * @param length    The number of elements to copy.
     */
    public static void copyByteBuffer(ByteBuffer src, int srcOffset, ByteBuffer dest, int destOffset, int length) {
        dest.put(destOffset, src, srcOffset, length);
    }

    /**
     * Copies a range of elements between two direct {@link LongBuffer}s at the given offsets.
     * <p>
     * Uses the absolute bulk {@code put}, so positions and limits of both buffers are neither read nor changed.
     *
     * @param src       The source buffer.
     * @param srcOffset Index of the first element to read.
     * @param dest      The destination buffer.
     * @param destOffset Index of the first element to write.
     * @param length    The number of elements to copy.
     */
    public static void copyLongBuffer(LongBuffer src, int srcOffset, LongBuffer dest, int destOffset, int length) {
        dest.put(destOffset, src, srcOffset, length);
    }

    /**
     * Copies a range of elements between two direct {@link IntBuffer}s at the given offsets.
     * <p>
     * Uses the absolute bulk {@code put}, so positions and limits of both buffers are neither read nor changed.
     *
     * @param src       The source buffer.
     * @param srcOffset Index of the first element to read.
     * @param dest      The destination buffer.
     * @param destOffset Index of the first element to write.
     * @param length    The number of elements to copy.
     */
    public static void copyIntBuffer(IntBuffer src, int srcOffset, IntBuffer dest, int destOffset, int length) {
        dest.put(destOffset, src, srcOffset, length);
    }
}
