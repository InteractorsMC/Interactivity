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

}
