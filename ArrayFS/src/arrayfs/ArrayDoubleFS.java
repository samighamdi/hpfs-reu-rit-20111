package arrayfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.UUID;

/**
 * Stores and manages a two-dimensional double array in a file.
 * 
 * 
 * @author AssistantX
 */
public class ArrayDoubleFS extends ArrayFS {
    
    private byte type;
    private int innerSize;
    private long size;
    private long position;
    private String filename;
    private RandomAccessFile fs;
    
    /**
     * Creates a two-dimensional double array of length 0 stored in a randomly named file with the specified inner array size
     * 
     * @param   innerSize   The size of all inner arrays in a two-dimensional array. This size cannot be changed after instantiation.
     */
    
    public ArrayDoubleFS(int innerSize) throws FileNotFoundException, IOException {
        this(UUID.randomUUID().toString().replace('-', '0'), innerSize);
    }
    
    /**
     * Creates a two-dimensional double array of length 0 stored in a file with the specified name and inner array size
     * 
     * @param   name    The name of the file
     * @param   innerSize   The size of all inner arrays in a two-dimensional array. This size cannot be changed after instantiation.
     */
    public ArrayDoubleFS(String name, int innerSize) throws FileNotFoundException, IOException {
        type = DOUBLE;
        size = 0;
        this.innerSize = innerSize;
        filename = name + ".adfs";
        new File(filename).delete();
        fs = new RandomAccessFile(filename, "rw");
        writeHeader();
    }
    
    /**
     * Creates a two-dimensional double array in a file with a randomly generated name from the given array. The size of the inner array cannot be changed and will be the same as the length of the first index.
     * 
     * @param   array   The array to build this file array from
     */
    public ArrayDoubleFS(final double[][] array) throws FileNotFoundException, IOException {
        this(UUID.randomUUID().toString().replace('-', '0'), array);
    }
    
    /**
     * Creates a two-dimensional double array in a file with the specified name from the given array. The size of the inner array cannot be changed and will be the same as the length of the first index.
     * 
     * @param    name    The name of the file
     * @param   array   The array to build this file array from
     */
    public ArrayDoubleFS(String name, final double[][] array) throws FileNotFoundException, IOException {
        type = DOUBLE;
        size = array.length;
        if (array[0] != null) {
            innerSize = array[0].length;
        }
        else {
            innerSize = 0;
        }
        filename = name + ".adfs";
        new File(filename).delete();
        fs = new RandomAccessFile(filename, "rw");
        writeHeader();
        writeData(array);
        seek(0);
    }
    
    /**
     * Opens a previously created ArrayDoubleFS file for reading and writing
     * 
     * @param    file    The file to open
     */
    public ArrayDoubleFS(File file) throws FileNotFoundException, IOException, ParseException {
        try {
            fs = new RandomAccessFile(file, "rw");
            type = fs.readByte();
            size = fs.readLong();
            innerSize = fs.readInt();
            fs.seek(HEADERSIZE);
            filename = file.getName();
        }
        catch (EOFException ex) {
            throw new ParseException("Improper File Format", 0);
        }
    }
    
    private void writeHeader() throws IOException {
        fs.seek(0);
        fs.writeByte(type);
        fs.writeLong(size);
        fs.writeInt(innerSize);
    }
    
    private void readHeader() throws IOException {
        fs.seek(0);
        type = fs.readByte();
        size = fs.readLong();
        innerSize = fs.readInt();
    }
    
    private long writeData(final double[][] array) throws IOException {
        return writeData(fs, array);
    }
    
    private long writeData(RandomAccessFile file, double[][] array) throws IOException {
        int added = array.length;
        for (int i = 0; i < array.length; i++) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(bytes);
            if (array[i].length == innerSize) {
                for (int j = 0; j < innerSize; j++) {
                    stream.writeDouble(array[i][j]);
                }
            }
            else {
                added--;
            }
            file.write(bytes.toByteArray());
            stream.close();
        }
        return added;
    }
    
    private long writeData(RandomAccessFile source, RandomAccessFile destination) throws IOException, ParseException {
        source.seek(0);
        byte type = source.readByte();
        if (type == this.type) {
            long size = source.readLong();
            long innerSize = source.readInt();
            copyData(source, destination, 0, size);
            return size;
        }
        else {
            throw new ParseException("Improper Array Type", 0);
        }
    }
    
    private void copyData(RandomAccessFile source, RandomAccessFile destination, long start, long end) throws IOException {
        source.seek(HEADERSIZE + type * innerSize * start);
        for (long i = 0; i < end - start; i += Math.min(BUFFERSIZE, end - start - i)) {
            byte[] temp = new byte[type * (int)(innerSize * Math.min(BUFFERSIZE, end - start - i))];
            source.readFully(temp);
            destination.write(temp);
        }
    }
    
    private void copyDataTo(RandomAccessFile destination, long start, long end) throws IOException {
        fs.seek(HEADERSIZE + type * innerSize * start);
        for (long i = 0; i < end - start; i += Math.min(BUFFERSIZE, end - start - i)) {
            byte[] temp = new byte[type * (int)(innerSize * Math.min(BUFFERSIZE, end - start - i))];
            fs.readFully(temp);
            destination.write(temp);
        }
    }
    
    private void copyDataFrom(RandomAccessFile source, long start, long end) throws IOException {
        source.seek(HEADERSIZE + type * innerSize * start);
        for (long i = 0; i < end - start; i += Math.min(BUFFERSIZE, end - start - i)) {
            byte[] temp = new byte[type * (int)(innerSize * Math.min(BUFFERSIZE, end - start - i))];
            source.readFully(temp);
            fs.write(temp);
        }
    }
    
    /**
     * Merges the cotents of the given array to the end of this array file
     * 
     * @param   array   The array to merge to the end of this array file
     */
    public void append(final double[][] array) throws IOException {
        fs.seek(HEADERSIZE + type * innerSize * size);
        size += writeData(array);
        writeHeader();
    }
    
    /**
     * Overwrites the portion of the array file beginning at the index specified with the array inserted
     * 
     * @param   array   The array to overwrite the portion of this file with
     * @param   start   The index of the array file to start
     */
    public void overwite(final double[][] array, long start) throws IOException {
        if (start >= 0 && start + array.length < size) {
            seek(start);
            writeData(array);
        }
        else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    
    /**
     * Merges the contents of the previously created ArrayDoubleFS file to the end of this array file.
     * 
     * @param  data   The previously created ArrayDoubleFS file.
     */
    public void append(File data) throws IOException, ParseException {
        try {
            RandomAccessFile source = new RandomAccessFile(data, "r");
            fs.seek(HEADERSIZE + type * innerSize * size);
            size += writeData(source, fs);
            writeHeader();
        }
        catch (EOFException ex) {
            throw new ParseException("Improper File Format", 0);
        }
    }
    
    /**
     * Inserts the contents of the given array into the current index of this array file
     * 
     * @param   array   The array to insert into this array file
     */
    public void insert(final double[][] array) throws IOException {
        RandomAccessFile tempFS = new RandomAccessFile("temp_" + filename, "rw");
        tempFS.seek(HEADERSIZE);
        copyDataTo(tempFS, 0, position);
        tempFS.seek(HEADERSIZE + type * innerSize * position);
        long insertSize = writeData(tempFS, array);
        copyDataTo(tempFS, position, size);
        fs.close();
        fs = null;
        tempFS.close();
        tempFS = null;
        new File(filename).delete();
        new File("temp_" + filename).renameTo(new File(filename));
        fs = new RandomAccessFile(filename, "rw");
        size = size + insertSize;
        writeHeader();
        seek(position);
    }
    
    
    /**
     * Inserts the contents of the previously created ArrayDoubleFS file into the current index of this array file
     * 
     * @param   array   The array to insert into this array file
     */
    public void insert(File data) throws IOException, ParseException {
        try {
            RandomAccessFile tempFS = new RandomAccessFile("temp_" + filename, "rw");
            RandomAccessFile source = new RandomAccessFile(data, "r");
            tempFS.seek(HEADERSIZE);
            copyDataTo(tempFS, 0, position);
            tempFS.seek(HEADERSIZE + type * innerSize * position);
            long insertSize = writeData(source, tempFS);
            copyDataTo(tempFS, position, size);
            fs.close();
            fs = null;
            tempFS.close();
            tempFS = null;
            new File(filename).delete();
            new File("temp_" + filename).renameTo(new File(filename));
            fs = new RandomAccessFile(filename, "rw");
            size = size + insertSize;
            writeHeader();
            seek(position);
        }
        catch (EOFException ex) {
            throw new ParseException("Improper File Format", 0);
        }
    }
    /**
     * Removes the specified index range from the array file
     * 
     * @param   start   The starting index of the content to remove
     * @param   num The number of indices to remove.
     */
    public void remove(long start, long num) throws IOException, ArrayIndexOutOfBoundsException {
        if (start >= 0 && start + num < size) {
            RandomAccessFile tempFS = new RandomAccessFile("temp_" + filename, "rw");
            tempFS.seek(HEADERSIZE);
            copyDataTo(tempFS, 0, start);
            tempFS.seek(HEADERSIZE + type * innerSize * start);
            copyDataTo(tempFS, start + num, size);
            fs.close();
            fs = null;
            tempFS.close();
            tempFS = null;
            new File(filename).delete();
            new File("temp_" + filename).renameTo(new File(filename));
            fs = new RandomAccessFile(filename, "rw");
            size = size - num;
            writeHeader();
            seek(start);
        }
        else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /**
     * Change the current index pointer of the array file to the specified position
     * 
     * @param   position    The index to place the index pointer
     */
    public void seek(long position) throws ArrayIndexOutOfBoundsException, IOException {
        if (position >= 0 && position < size) {
            fs.seek(HEADERSIZE + position * type * innerSize);
            this.position = position;
        }
        else {
            throw new ArrayIndexOutOfBoundsException(Long.toString(position));
        }
    }
    
    /*
     * Returns the length of the outer array
     * 
     * @return      The length of the outer array
     */
    public long size() {
        return size;
    }
    
    /*
     * Returns the length of the inner arrays
     * 
     * @return      The length of the inner arrays
     */
    public int innerSize() {
        return innerSize;
    }
    
    /*
     * Returns a copy of two-dimensional array stored by this file
     * 
     * @return      A copy of the two-dimension array stored by this file
     */
    public double[][] getArray() throws IOException {
        double[][] array = new double[(int)size][(int)innerSize];
        fs.seek(HEADERSIZE);
        for (int a = 0; a < array.length; a += (int)Math.min(BUFFERSIZE, array.length - a)) {
            byte[] temp = new byte[type * (int)innerSize * (int)Math.min(BUFFERSIZE, array.length - a)];
            fs.readFully(temp);
            ByteBuffer bytes = ByteBuffer.wrap(temp);
            for (int i = a; i < a + (int)Math.min(BUFFERSIZE, array.length - a); i++) {
                for (int j = 0; j < array[i].length; j++) {
                    array[i][j] = bytes.getDouble();
                }
            }
        }
        
        position = 0;
        return array;
    }
    
    /*
     * Returns a single segment of the two-dimensional array stored by this file at the current index pointer position. The index pointer moves to the next index.
     * 
     * @return      A single segment of the two-dimensional array stored by this file at the current index pointer position
     */
    public double[][] getNext() throws IOException, ArrayIndexOutOfBoundsException {
        if (position < size) {
            double [][] array = new double[1][(int)innerSize];
            byte[] temp = new byte[type * (int)innerSize];
            fs.readFully(temp);
            ByteBuffer bytes = ByteBuffer.wrap(temp);
            for (int j = 0; j < array[0].length; j++) {
                array[0][j] = bytes.getDouble();
            }
            position++;
            return array;
        }
        else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /*
     * Returns a specified number of indices from the two-dimensional array stored by this file at the current index pointer position. The index pointer moves to the next index.
     *
     * @param   num The number of indices to return from the array file
     * @return      A segment of the two-dimensional array stored by this file at the current index pointer position with the requested size.
     */
    public double[][] getNext(int num) throws IOException {
        if (position + num <= size) {
            double [][] array = new double[(int)num][(int)innerSize];
            for (int a = 0; a < num; a += (int)Math.min(BUFFERSIZE, num - a)) {
                byte[] temp = new byte[type * (int)innerSize * (int)Math.min(BUFFERSIZE, num - a)];
                fs.readFully(temp);
                ByteBuffer bytes = ByteBuffer.wrap(temp);
                for (int i = a; i < a + (int)Math.min(BUFFERSIZE, num - a); i++) {
                    for (int j = 0; j < array[i].length; j++) {
                        array[i][j] = bytes.getDouble();
                    }
                }
                position++;
            }
            return array;
        }
        else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /*
     * Checks if there are more indices that can be returned before the end of this file
     *
     * @return      True if more indices can be returned before the end of this file.
     */
    public boolean hasNext() {
        return (position < size)? true : false;
    }
    
    /*
     * Resets the index pointer of this file to position 0
     */
    public void reset() throws IOException, ArrayIndexOutOfBoundsException {
        seek(0);
    }
    
    /*
     * Closes the file from further operations
     */
    public void close() throws IOException {
        fs.close();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException, FileNotFoundException, IOException {
    }
}
