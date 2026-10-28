/*
 * Copyright (c) 2026 timtaran
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org.
 */
package org.clwn.interactivity.core.physics.persistence.storage;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Reads and writes {@code r.<x>.<z>.imcphys} files. One file holds the payloads of 32x32 chunks.
 * <pre>
 * magic        4 B   "IMCP"
 * version      u16   1
 * compression  u8    0 = none, 1 = zlib
 * section (compressed as a whole):
 *   entryCount varint
 *   entries    entryCount x { localIndex u16 = (z &amp; 31) &lt;&lt; 5 | (x &amp; 31), length varint }
 *   payloads   concatenated, in entry order
 * </pre>
 */
public final class PhysicsRegionFile {
    public static final int CHUNKS_PER_REGION = 32 * 32;

    private static final byte[] MAGIC = {'I', 'M', 'C', 'P'};
    private static final int VERSION = 1;
    private static final int COMPRESSION_NONE = 0;
    private static final int COMPRESSION_ZLIB = 1;
    private static final int HEADER_SIZE = MAGIC.length + 3;

    private PhysicsRegionFile() {
    }

    public static int localIndex(int chunkX, int chunkZ) {
        return (chunkZ & 31) << 5 | (chunkX & 31);
    }

    /**
     * @return the payload of every chunk of the region, {@code null} entries for chunks without one
     * @throws IOException if the file can not be read or is corrupt
     */
    public static byte[][] read(Path path) throws IOException {
        byte[][] chunks = new byte[CHUNKS_PER_REGION][];
        if (!Files.exists(path)) {
            return chunks;
        }

        ByteBuffer file = ByteBuffer.wrap(Files.readAllBytes(path));
        if (file.remaining() < HEADER_SIZE) {
            throw new IOException("File is too short");
        }

        byte[] magic = new byte[MAGIC.length];
        file.get(magic);
        if (!Arrays.equals(magic, MAGIC)) {
            throw new IOException("Not a physics region file");
        }

        int version = Short.toUnsignedInt(file.getShort());
        if (version != VERSION) {
            throw new IOException("Unsupported version " + version);
        }

        int compression = Byte.toUnsignedInt(file.get());
        byte[] section = new byte[file.remaining()];
        file.get(section);

        switch (compression) {
            case COMPRESSION_NONE -> {}
            case COMPRESSION_ZLIB -> section = inflate(section);
            default -> throw new IOException("Unsupported compression " + compression);
        }

        try {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(section));

            int entryCount = buf.readVarInt();
            if (entryCount < 0 || entryCount > CHUNKS_PER_REGION) {
                throw new IOException("Invalid entry count " + entryCount);
            }

            int[] indices = new int[entryCount];
            int[] lengths = new int[entryCount];
            for (int i = 0; i < entryCount; i++) {
                indices[i] = buf.readUnsignedShort();
                lengths[i] = buf.readVarInt();
                if (indices[i] >= CHUNKS_PER_REGION || lengths[i] < 0 || lengths[i] > buf.readableBytes()) {
                    throw new IOException("Invalid entry " + i);
                }
            }

            for (int i = 0; i < entryCount; i++) {
                byte[] payload = new byte[lengths[i]];
                buf.readBytes(payload);
                chunks[indices[i]] = payload;
            }

            if (buf.isReadable()) {
                throw new IOException(buf.readableBytes() + " unexpected bytes at the end");
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Section is truncated", e);
        }

        return chunks;
    }

    /**
     * Writes the region, or deletes the file if no chunk has a payload.
     */
    public static void write(Path path, byte[][] chunks) throws IOException {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        int entryCount = 0;
        for (byte[] payload : chunks) {
            if (payload != null) {
                entryCount++;
            }
        }

        if (entryCount == 0) {
            Files.deleteIfExists(path);
            return;
        }

        buf.writeVarInt(entryCount);
        for (int i = 0; i < chunks.length; i++) {
            if (chunks[i] != null) {
                buf.writeShort(i);
                buf.writeVarInt(chunks[i].length);
            }
        }
        for (byte[] payload : chunks) {
            if (payload != null) {
                buf.writeBytes(payload);
            }
        }

        byte[] section = new byte[buf.readableBytes()];
        buf.readBytes(section);
        byte[] compressed = deflate(section);

        ByteBuffer file = ByteBuffer.allocate(HEADER_SIZE + compressed.length);
        file.put(MAGIC);
        file.putShort((short) VERSION);
        file.put((byte) COMPRESSION_ZLIB);
        file.put(compressed);

        Files.createDirectories(path.getParent());
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Files.write(temporary, file.array());
        try {
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static byte[] deflate(byte[] data) {
        Deflater deflater = new Deflater();
        try {
            deflater.setInput(data);
            deflater.finish();

            ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(64, data.length / 4));
            byte[] chunk = new byte[8192];
            while (!deflater.finished()) {
                out.write(chunk, 0, deflater.deflate(chunk));
            }
            return out.toByteArray();
        } finally {
            deflater.end();
        }
    }

    private static byte[] inflate(byte[] data) throws IOException {
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(data);

            ByteArrayOutputStream out = new ByteArrayOutputStream(data.length * 4);
            byte[] chunk = new byte[8192];
            while (!inflater.finished()) {
                int length = inflater.inflate(chunk);
                if (length == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                    throw new IOException("Compressed section is truncated");
                }
                out.write(chunk, 0, length);
            }
            return out.toByteArray();
        } catch (DataFormatException e) {
            throw new IOException("Compressed section is corrupt", e);
        } finally {
            inflater.end();
        }
    }
}
