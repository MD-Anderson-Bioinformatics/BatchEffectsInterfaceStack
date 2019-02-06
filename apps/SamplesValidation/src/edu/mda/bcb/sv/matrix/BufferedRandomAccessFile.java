package edu.mda.bcb.matrix;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Description:
 * ------------
 * An extension of the RandomAccessFile which adds a buffer. Improves sequential file I/O
 * significantly. When doing frequent jumping around the file, performances approaches
 * base RandomAccessFile.
 * 
 * Notes:
 * ------
 *  - Based off post: https://www.javaworld.com/article/2077523/build-ci-sdlc/java-tip-26--how-to-improve-java-s-i-o-performance.html
 *  - Newline support is currently limited to newlines ending in '\n'. This covers
 *    '\n' and '\r\n', however extended newline recognition including a newline
 *    argument could be added.
 * 
 * @author cjacoby
 */
public class BufferedRandomAccessFile extends RandomAccessFile {
    
    private byte[] buffer;                                                      // The btye array used as buffer
    private int bufferEnd = 0;                                                  // The current buffer's length
    private int bufferPosition = 0;                                             // The current position in the buffer
    private long filePosition = 0;                                              // The current position in the file
    private static final int DEFUALT_BUFF_SIZE = 1 << 14; // 16,384             // Default buffer capacity
    private static final int DEFAULT_STRBUILD_SIZE = 256;                       // Default StringBuilder capacity in getNextLine
    
    public BufferedRandomAccessFile(String name, String mode) throws FileNotFoundException, IOException {
        this(name, mode, BufferedRandomAccessFile.DEFUALT_BUFF_SIZE);
    }
    
    public BufferedRandomAccessFile(String name, String mode, int bufferSize) throws FileNotFoundException, IOException {
        super(name, mode);
        this.invalidate();
        this.buffer = new byte[bufferSize];
    }
    
    /**
     * Return the next byte from the file. Will refill buffer as needed.
     * 
     * @return <byte>: The next byte, or -1 if reaches end of file.
     * @throws java.io.IOException
     */
    @Override
    public final int read() throws IOException {
        if (this.bufferIsEmpty()) {
            if (this.fillBuffer() < 0) {
                return -1;
            }
        } 
        if (this.bufferEnd == 0) {
            return -1;
        } else {
            return this.buffer[this.bufferPosition++];
        }
    }
    
    /**
     * Read characters into a caller-passed buffer. Returns the number of bytes read. 
     * 
     * @param buff <byte[]>: A caller's byte[] to read bytes into.
     * @param offset <int>: The index of buff to begin reading bytes to.
     * @param length <int>: The max number of bytes to read into buff.
     * @return bytesRead <int>: The number of bytes read to caller's buffer.
     * @throws java.io.IOException
     */
    @Override
    public int read(byte[] buff, int offset, int length) throws IOException {
        int leftover = this.bufferEnd - this.bufferPosition;
        if (length <= leftover) {
            System.arraycopy(this.buffer, this.bufferPosition, buff, offset, length);
            this.bufferPosition += length;
            return length;
        }
        for (int i = 0; i < length; i++) {
            int c = this.read();
            if (c != -1) {
                buff[offset + i] = (byte) c;
            } else {
                return i;
            }
        }
        return length;
    }
    
    /**
     * Check if the buffer is empty, which happens when buffer's position reaches
     * the buffer's end
     * 
     * @return <boolean>: Buffer is/isn't empty
     */
    private boolean bufferIsEmpty() {
        return this.bufferEnd - this.bufferPosition <= 0;
    }
    
    /**
     * Fill the buffer by calling the base RandomAccessFile's read.
     * 
     * @return <int>: The number of bytesRead, or -1 if end of file.
     */
    private int fillBuffer() throws IOException {
        int bytesRead = super.read(this.buffer, 0, this.buffer.length);
        if (bytesRead >= 0) {
            // If bytes were read, update filePosition and reset bufferPosition and bufferEnd
            this.filePosition += bytesRead;
            this.bufferEnd = bytesRead;
            this.bufferPosition = 0;
        }
        return bytesRead;
    }
    
    /**
     * Signal that the buffer no longer contains valid contents. This will trigger a
     * fillBuffer on the next execution of read-like method. This happens if the seek
     * method moves the filePosition outside the current buffer.
     */
    private void invalidate() throws IOException {
        this.bufferEnd = 0;
        this.bufferPosition = 0;
        this.filePosition = super.getFilePointer();
    }
    
    /**
     * Get the current file position, a.k.a. current byte offset in file.
     * 
     * @return <long>: The offset of current file position.
     * @throws java.io.IOException
     */
    @Override
    public long getFilePointer() throws IOException {
        return this.filePosition - this.bufferEnd + this.bufferPosition;
    }
    
    /**
     * Change current file position.
     * 
     * @param pos <long>: The new file position.
     * @throws java.io.IOException
     */
    @Override
    public void seek(long pos) throws IOException {
        int n = (int)(this.filePosition - pos);
        if (n >= 0 && n <= this.bufferEnd) {
            this.bufferPosition = this.bufferEnd - n;
        } else {
            super.seek(pos);
            this.invalidate();
        }
    }
    
    /**
     * Get the next line from file. Akin to BufferedReader.readLine.
     * 
     * @return <String>: The next line of the file, or null if end of file hit.
     * @throws java.io.IOException
     */
    public final String getNextLine() throws IOException {
        String line;
        if (this.bufferIsEmpty()) {
            if (this.fillBuffer() < 0) {
                return null;
            }
        }
        int lineEnd = -1;
        for (int i = this.bufferPosition; i < this.bufferEnd; i++) {
            if (this.buffer[i] == '\n') {
                lineEnd = i + 1;
                break;
            }
        }
        // Newline not in buffer. Keep reading until next newline, then return line.
        if (lineEnd < 0) {
            StringBuilder input = new StringBuilder(BufferedRandomAccessFile.DEFAULT_STRBUILD_SIZE);
            int c;
            while ((c = this.read()) != -1) {
                input.append((char) c);
                if (c == '\n') {
                    break;
                }
            }
            if (c == -1 && input.length() == 0) {
                return null;
            }
            return input.toString();
        }
        line = new String(this.buffer, 0, this.bufferPosition, lineEnd - this.bufferPosition);
        this.bufferPosition = lineEnd;
        return line;
    }
    
    public static void main(String args[]) throws IOException {
        String path = "C:\\Users\\cjacoby\\matrixSortTest\\matrix_data_.tsv";
        BufferedRandomAccessFile braf = new BufferedRandomAccessFile(path, "r", 8192);
        String line;
        int byteOffset = 0;
        while ((line = braf.getNextLine()) != null) {
            byteOffset += line.getBytes().length;
            System.out.println(byteOffset);
        }
        
    }
    
}
