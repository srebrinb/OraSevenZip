package bg.bobs.SevenZip;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import java.nio.ByteBuffer;

public class outStreamBLOB extends OutputStream {
    private ByteArrayOutputStream stream;
    static public final int STREAM_SEEK_SET = 0;
    static public final int STREAM_SEEK_CUR = 1;
    int initialBufferCapacity = 65536;
    java.nio.ByteBuffer buf = ByteBuffer.allocate(initialBufferCapacity);
    int bufferSize = 0;
    long size = 0;
    private long pos = 0;
    RandomAccessFile ostream = null;
    OutputStream outStream = null;
    byte[] startHeader;

    public outStreamBLOB(OutputStream outBuffer) {
        outStream = outBuffer;
        stream = new ByteArrayOutputStream();
    }

    public outStreamBLOB(String filename,
                         String mode) throws FileNotFoundException {
        ostream = new java.io.RandomAccessFile(filename, mode);
        stream = new ByteArrayOutputStream();

    }

    public outStreamBLOB(File file, String mode) throws java.io.IOException {
        ostream = new java.io.RandomAccessFile(file, mode);
        stream = new ByteArrayOutputStream();
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (bufferSize > 0) {
            stream.write(buf.array(), 0, bufferSize);
            buf.clear();
            bufferSize = 0;
        }
        stream.write(b);
        size += b.length;
        pos += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (bufferSize > 0) {
            stream.write(buf.array(), 0, bufferSize);
            buf.clear();
            bufferSize = 0;
        }
        stream.write(b, off, len);
        size += len;
        pos += len;
    }

    @Override
    public void write(int b) throws IOException {
        if (bufferSize < initialBufferCapacity) {
            buf.put((byte)b);
            bufferSize++;
        } else {
            stream.write(buf.array());
            buf.clear();
            bufferSize = 0;
            buf.put((byte)b);
            bufferSize++;
        }

        size++;
        pos++;
    }

    public long seek(long offset, int seekOrigin) throws java.io.IOException {
        if (bufferSize > 0) {
            stream.write(buf.array(), 0, bufferSize);
            buf.clear();
            bufferSize = 0;
        }
        // stream.reset();
        if (seekOrigin == STREAM_SEEK_CUR) {
            pos += offset;
        } else {
            pos = offset;
        }

        size = 0;

        return pos;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setStartHeader(byte[] inB) throws IOException {
        startHeader = inB;
    }

    public void applayStartHeader(byte[] inB) throws IOException {
        if (bufferSize > 0) {
            stream.write(buf.array());
            buf.clear();
            bufferSize = 0;
        }
        startHeader = null;
        byte[] tmp = stream.toByteArray();
        for (int i = 0; i < inB.length; i++) {
            int p = (int)pos + i;
            tmp[p] = inB[i];
        }
        pos += tmp.length;
        stream = new ByteArrayOutputStream(tmp.length);
        stream.write(tmp, 0, tmp.length);
    }

    @Override
    public void close() throws IOException {
        if (bufferSize > 0) {
            stream.write(buf.array());
            buf.clear();
            bufferSize = 0;
        }
        stream.close();
        byte[] toSave = stream.toByteArray();
        if (ostream != null) {
            if (startHeader != null) {
                ostream.write(toSave, 0, 8);
                ostream.write(startHeader);
                ostream.write(toSave, 8 + 24, toSave.length - 32);
            } else {
                ostream.write(toSave, 0, toSave.length);
            }
            ostream.close();
        } else {
            if (startHeader != null) {
                outStream.write(toSave, 0, 8);
                outStream.write(startHeader);
                outStream.write(toSave, 8 + 24, toSave.length - 32);
            } else {
                outStream.write(toSave, 0, toSave.length);
            }
            outStream.close();
        }

    }
}
