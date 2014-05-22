import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * File system
 *
 * @author Dmytro Troshchuk
 * @version 1.00 17.05.14
 */
public class FileSystem {
    /** File where saved file system */
    private RandomAccessFile file;
    /** File system memory */
    private int memory;
    /** Free memory */
    private int freeMemory;
    /** Super block */
    private SuperBlock superBlock;
    /** Bit map of blocks */
    private BitMap blocksBitMap[];
    /** Bit map of inodes */
    private BitMap inodesBitMap[];
    /** Table of inodes */
    private HashMap<Integer, Inode> openedFiles;

    /**
     * Constructor create new file system
     *
     * @param filepath file where constructor will save virtual hard drive
     * @param memory   memory
     */
    public FileSystem(String filepath, int memory) {
        this.memory = memory;
        superBlock = new SuperBlock();

        int blocksCount = superBlock.BLOCKS_COUNT;
        int blockSize = superBlock.BLOCK_SIZE;
        int inodesCount = superBlock.INODES_COUNT;
        int inodeSize = superBlock.INODE_SIZE;
        int linkSize = superBlock.LINK_SIZE;
        int bitCount = blockSize * 8;

        int blocksBitmapsCount = (int) Math.ceil((1. * blocksCount) / bitCount);
        superBlock.BLOCKS_BITMAPS_COUNT = blocksBitmapsCount;

        int inodesBitmapsCount = (int) Math.ceil((1. * inodesCount) / bitCount);
        superBlock.INODES_BITMAPS_COUNT = inodesBitmapsCount;

        int inodesTable = (inodesCount * inodeSize) / blockSize;
        int firstDataBlock = (1 + blocksBitmapsCount + inodesBitmapsCount + inodesTable);
        superBlock.FIRST_DATA_BLOCK = firstDataBlock;

        superBlock.FREE_BLOCKS_COUNT = blocksCount - firstDataBlock;
        superBlock.FREE_INODES_COUNT = superBlock.INODES_COUNT;
        superBlock.DATA_BLOCKS_COUNT = blocksCount - firstDataBlock;

        Block.setBlockSize(blockSize);
        BitMap.setBlockSize(blockSize);

        blocksBitMap = new BitMap[blocksBitmapsCount];
        for (int i = 0; i < blocksBitmapsCount; i++) {
            blocksBitMap[i] = new BitMap();
        }

        // Root use first block
        blocksBitMap[0].setBits(0, (byte) 1);

        inodesBitMap = new BitMap[inodesBitmapsCount];
        for (int i = 0; i < inodesBitmapsCount; i++) {
            inodesBitMap[i] = new BitMap();
        }

        // Root use first inode
        inodesBitMap[0].setBits(0, (byte) 1);

        // Root use
        superBlock.FREE_BLOCKS_COUNT--;
        superBlock.FREE_INODES_COUNT--;

        // Write all information of created fs to hard drive
        try {
            file = new RandomAccessFile(filepath, "rw");

            // Write no more than 1 Mb in 1 time
            int toWrite = memory;
            while (toWrite > 1024) {
                file.write(new byte[1024]);
                toWrite -= 1;
            }
            file.write(new byte[toWrite * 1024]);

            file.seek(0);
            file.write(superBlock.toBytes());

            for (int i = 0; i < blocksBitmapsCount; i++) {
                file.write(blocksBitMap[i].getBits());
            }

            for (int i = 0; i < inodesBitmapsCount; i++) {
                file.write(inodesBitMap[i].getBits());
            }

            // Write first data block for root
            for (int i = 0; i < inodesTable; i++) {
                ByteBuffer bb = ByteBuffer.allocate(blockSize);
                int length = blockSize / inodeSize;
                for (int j = 0; j < length; j++) {
                    Inode inode = new Inode();
                    if (j == 0 && i == 0) { // Root use first inode
                        inode.fileType = 1;
                        inode.size = 0;
                        inode.links = 2;
                        inode.numberOfBlocks[0] = 0; // Root use first block
                    }
                    bb.put(inode.toByte());
                }
                file.write(bb.array());
            }

            // Write . and .. dirs to root
            int length = blockSize / linkSize;
            Link[] links = new Link[length];
            for (int i = 0; i < length; i++) {
                links[i] = new Link();
            }

            links[0].set(0, "."); // Cur dir
            links[1].set(0, ".."); // Parent dir

            Block block = new Block(links);
            file.write(block.getBytes());

            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Empty constructor
     */
    public FileSystem() {

    }

    /**
     * Mount file system
     *
     * @param filepath file where saved virtual hard drive
     */
    public boolean mountFileSystem(String filepath) {
        try {
            file = new RandomAccessFile(filepath, "rw");
            superBlock = new SuperBlock();

            byte[] bytes = new byte[superBlock.BLOCK_SIZE];
            // Load super block
            file.read(bytes);
            superBlock.load(bytes);

            int blockSize = superBlock.BLOCK_SIZE;
            int blocksCount = superBlock.BLOCKS_COUNT;
            int freeBlocksCount = superBlock.FREE_BLOCKS_COUNT;

            Block.setBlockSize(blockSize);
            BitMap.setBlockSize(blockSize);

            //Load blocks bitmaps
            int blocksBitmapsCount = superBlock.BLOCKS_BITMAPS_COUNT;
            blocksBitMap = new BitMap[blocksBitmapsCount];
            for (int i = 0; i < blocksBitmapsCount; i++) {
                bytes = new byte[blockSize];
                file.read(bytes);
                blocksBitMap[i] = new BitMap();
                blocksBitMap[i].setBits(bytes);
            }

            //Load inodes bitmaps
            int inodesBitmapsCount = superBlock.INODES_BITMAPS_COUNT;
            inodesBitMap = new BitMap[inodesBitmapsCount];
            for (int i = 0; i < inodesBitmapsCount; i++) {
                bytes = new byte[blockSize];
                file.read(bytes);
                inodesBitMap[i] = new BitMap();
                inodesBitMap[i].setBits(bytes);
            }

            memory = blocksCount * blockSize;
            freeMemory = freeBlocksCount * blockSize;
            openedFiles = new HashMap<>();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Load data block
     *
     * @param number number of data block
     * @return data block
     */
    private Block loadDataBlock(int number) {
        try {
            int blockSize = superBlock.BLOCK_SIZE;
            int firstDataBlock = superBlock.FIRST_DATA_BLOCK;
            byte[] bytes = new byte[blockSize];

            file.seek(blockSize * (firstDataBlock + number));
            file.read(bytes);
            return new Block(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Write data block
     *
     * @param block data block to write
     * @param number number of data block
     */
    private void writeDataBlock(Block block, int number) {
        try {
            int blockSize = superBlock.BLOCK_SIZE;
            int firstDataBlock = superBlock.FIRST_DATA_BLOCK;

            file.seek(blockSize * (firstDataBlock + number));
            file.write(block.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the block
     *
     * @param number number of block
     * @return block
     */
    private Block loadBlock(int number) {
        try {
            byte[] bytes = new byte[superBlock.BLOCK_SIZE];

            file.seek(superBlock.BLOCK_SIZE * number);
            file.read(bytes);
            return new Block(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Write the block
     *
     * @param block block to write
     * @param number number of block
     */
    private void writeBlock(Block block, int number) {
        try {
            file.seek(superBlock.BLOCK_SIZE * number);
            file.write(block.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return info of the inode. Format:
     * File type: [filetype]
     * Size: [size]
     * Links: [count of links]
     * Used blocks: [count of used blocks]
     *
     * @param i inode number
     * @return info of the inode
     */
    public String getInodeInfo(int i) {
        String result = "";

        Inode inode = loadInode(i);

        result += "File type: " + inode.fileType + "\n";
        result += "\nSize: " + inode.size + "\n";
        result += "Links: " + inode.links;

        result += "\nUsed blocks: ";
        int count = 0;
        int length = inode.numberOfBlocks.length;
        while (length > count && inode.numberOfBlocks[count] != -1) {
            count++;
        }
        result += count;

        return result;
    }

    /**
     * Return list of files and its inode number in current directory
     * Format:
     * [filename] [inode number]
     *
     * @param curDir current directory
     * @return list of files and its inode
     */
    public String getListOfFiles(int curDir) {
        String result = "";
        Inode inode = loadInode(curDir);

        int length = inode.numberOfBlocks.length;
        for (int i = 0; i < length && inode.numberOfBlocks[i] != -1; i++) {
            int numberOfBlock = inode.numberOfBlocks[i];
            Block block = loadDataBlock(numberOfBlock);
            Link[] links = new Link[superBlock.LINK_SIZE];
            for (int j = 0; j < links.length; j++) {
                links[j] = new Link();
            }

            int sizeSector = superBlock.LINK_SIZE;

            int length2 = superBlock.BLOCK_SIZE / sizeSector;
            for (int j = 0; j < length2; j++) {
                int begin = j * sizeSector;
                int end = (j + 1) * sizeSector;
                byte[] bytes = block.getSector(begin, end);

                links[j].load(bytes);
                if (links[j].getInodeId() == -1) {
                    continue;
                }

                result += links[j].getFilename() + " ";
                result += links[j].getInodeId() + "\n";
            }
        }
        return result;
    }

    /**
     * Load the inode
     *
     * @param i inode number
     * @return inode
     */
    private Inode loadInode(int i) {
        int blockSize = superBlock.BLOCK_SIZE;
        int inodeSize = superBlock.INODE_SIZE;
        int blocksBitmapsCount = superBlock.BLOCKS_BITMAPS_COUNT;
        int inodesBitmapsCount = superBlock.INODES_BITMAPS_COUNT;
        int firstInodeBlock = 1 + blocksBitmapsCount + inodesBitmapsCount;

        int iPerBlock = blockSize/ inodeSize;
        int blockNumber = i / iPerBlock;
        int sectorNumber = i % iPerBlock;

        int begin = inodeSize * sectorNumber;
        int end = begin + inodeSize;
        Block block = loadBlock(blockNumber + firstInodeBlock);
        byte[] bytes = block.getSector(begin, end);

        Inode inode = new Inode();
        inode.load(bytes);
        return inode;
    }

    /**
     * Write the inode
     *
     * @param i inode number
     * @param inode inode
     */
    private void writeInode(int i, Inode inode) {
        int blockSize = superBlock.BLOCK_SIZE;
        int inodeSize = superBlock.INODE_SIZE;
        int blocksBitmapsCount = superBlock.BLOCKS_BITMAPS_COUNT;
        int inodesBitmapsCount = superBlock.INODES_BITMAPS_COUNT;
        int firstInodeBlock = 1 + blocksBitmapsCount + inodesBitmapsCount;

        int iPerBlock = blockSize / inodeSize;
        int blockNumber = i / iPerBlock;
        int sectorNumber = i % iPerBlock;

        int begin = inodeSize * sectorNumber;
        Block block = loadBlock(blockNumber + firstInodeBlock);
        block.writeSector(inode.toByte(), begin);
        writeBlock(block, blockNumber + firstInodeBlock);
    }

    /**
     * Create regular file
     *
     * @param curDir current directory
     * @param filename filename
     * @return result of creating file
     */
    public boolean createRegularFile(int curDir, String filename) {
        if (superBlock.FREE_BLOCKS_COUNT == 0) {
            return false;
        }

        if (superBlock.FREE_INODES_COUNT == 0) {
            return false;
        }

        int freeBlock = getFreeDataBlock();
        int freeInode = getFreeInode();

        Inode inode = new Inode();
        inode.size = superBlock.BLOCK_SIZE;
        inode.links = 1;
        inode.numberOfBlocks[0] = freeBlock;
        writeInode(freeInode, inode);

        createLink(curDir, freeInode, filename);

        superBlock.FREE_BLOCKS_COUNT--;
        superBlock.FREE_INODES_COUNT--;
        freeMemory--;

        Block b = new Block();
        b.writeSector(superBlock.toBytes(), 0);
        writeBlock(b, 0);
        return true;
    }

    /**
     * Open regular file. Add inode number and inode to hashmap opened files
     *
     * @param curDir current directory
     * @param filename filename
     * @return inode number or -1 if file does not exit
     */
    public int openRegularFile(int curDir, String filename) {
        int fd = searchInode(curDir, filename, false);
        Inode inode = loadInode(fd);

        if (inode.fileType != Inode.REGULAR) {
            return -1;
        }
        openedFiles.put(fd, inode);
        return fd;
    }

    /**
     * Close regular file. Delete inode from hashmap opened files
     *
     * @param fd file descriptor
     */
    public void closeRegularFile(int fd) {
        for (int i : openedFiles.keySet()) {
            if (i == fd) {
                openedFiles.remove(i);
            }
        }
    }

    /**
     * Read data of the file
     *
     * @param fd file descriptor
     * @param off offset
     * @param size size
     * @return read bytes or null if file has not opened
     */
    public byte[] readFile(int fd, int off, int size) {
        ByteBuffer bb = ByteBuffer.allocate(size);
        Inode inode = openedFiles.get(fd);

        if (inode == null || size + off > inode.size) {
            return null;
        }

        int blockSize = superBlock.BLOCK_SIZE;
        int begin = off / blockSize;
        int end = (int) Math.ceil((1. * (off + size)) / blockSize);
        int length = end - begin;
        int offset = off % blockSize;

        Block[] blocks = new Block[length];
        int toLoad = size;
        for (int i = 0; i < length; i++) {
            blocks[i] = loadDataBlock(inode.numberOfBlocks[begin + i]);

            if (toLoad + offset > blockSize) {
                bb.put(blocks[i].getSector(offset, blockSize));
                toLoad -= blockSize - offset;
            } else {
                bb.put(blocks[i].getSector(offset, toLoad + offset));
            }
            offset = 0;
        }


        return bb.array();
    }

    /**
     * Write data to the file
     *
     * @param fd file descriptor
     * @param off offset
     * @param size size
     * @param data data to write
     * @return result of writing to the fs
     */
    public boolean writeFile(int fd, int off, int size, byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        Inode inode = openedFiles.get(fd);

        if (inode == null) {
            return false;
        }

        while (size + off > inode.size + superBlock.BLOCK_SIZE) {
            if (!truncateUp(fd)) {
                return false;
            }
        }

        if (inode.size < size + off) {
            inode.size = size + off;
        }

        int blockSize = superBlock.BLOCK_SIZE;
        int begin = off / blockSize;
        int end = (int) Math.ceil((1. * (off + size)) / blockSize);
        int length = end - begin;
        int offset = off % blockSize;

        Block[] blocks = new Block[length];
        int toWrite = size;
        for (int i = 0; i < length; i++) {
            blocks[i] = loadDataBlock(inode.numberOfBlocks[begin + i]);
            byte[] bytes;

            if (toWrite + offset > blockSize) {
                bytes = new byte[blockSize - offset];
                toWrite -= blockSize - offset;
            } else {
                bytes = new byte[toWrite];
            }
            bb.get(bytes);
            blocks[i].writeSector(bytes, offset);
            writeDataBlock(blocks[i], inode.numberOfBlocks[begin + i]);
            offset = 0;
        }

        writeInode(fd, inode);
        return true;
    }

    /**
     * Create link to the file.
     *
     * @param curDir current directory
     * @param inode inode numbber
     * @param filename filename
     */
    private void createLink(int curDir, int inode, String filename) {
        Inode dir = loadInode(curDir);
        int count = 0;
        int length = dir.numberOfBlocks.length;
        while (count < length && dir.numberOfBlocks[count] != -1) {
            Block block = loadDataBlock(dir.numberOfBlocks[count]);
            int linkSize = superBlock.LINK_SIZE;
            Link[] links = new Link[linkSize];
            for (int j = 0; j < links.length; j++) {
                int begin = j * linkSize;
                int end = (j + 1) * linkSize;

                links[j] = new Link();
                links[j].load(block.getSector(begin, end));
            }

            for (int i = 0; i < links.length; i++) {
                if (links[i].getInodeId() == -1) {
                    links[i].set(inode, filename);
                    block.writeSector(links[i].toBytes(), i * linkSize);
                    writeDataBlock(block, dir.numberOfBlocks[count]);
                    return;
                }
            }
            count++;
        }

        // Create new block for link
        if (count < Helper.MAX_FILE_SIZE) {
            int blockSize = superBlock.BLOCK_SIZE;
            int linkSize = superBlock.LINK_SIZE;
            int freeBlockForLink = getFreeDataBlock();
            dir.numberOfBlocks[count] = freeBlockForLink;

            Block block1 = new Block();
            Link[] links2 = new Link[blockSize / linkSize];
            links2[0] = new Link(inode, filename);
            block1.writeSector(links2[0].toBytes(), 0);

            for (int i = 1; i < links2.length; i++) {
                links2[i] = new Link();
                block1.writeSector(links2[i].toBytes(), i * linkSize);
            }
            writeDataBlock(block1, freeBlockForLink);
            writeInode(curDir, dir);
        }
    }

    /**
     * Create link to the file.
     *
     * @param curDir current directory
     * @param filename1 filename exist file
     * @param filename2 filename link to exist file
     * @return result of creating link
     */
    public boolean createLink(int curDir, String filename1, String filename2) {
        int id = searchInode(curDir, filename1, false);

        if (id == -1) {
            return false;
        }

        Inode inode = loadInode(id);

        inode.links++;
        writeInode(id, inode);

        createLink(curDir, id, filename2);
        return true;
    }

    /**
     * Delete link from file. If file will not have links, delete him.
     *
     * @param curDir current directory
     * @param filename filename
     * @return result of deleting
     */
    public boolean deleteLink(int curDir, String filename) {
        int id = searchInode(curDir, filename, true);

        if (id == -1) {
            return false;
        }

        Inode inode = loadInode(id);

        inode.links--;
        if (inode.links == 0) {
            int length = inode.numberOfBlocks.length;
            for (int i = 0; i < length; i++) {
                if (inode.numberOfBlocks[i] == -1) {
                    break;
                }
                setToBlocksBitMap(inode.numberOfBlocks[i], 0);
            }
            inode = new Inode();
            setToInodesBitMap(id, 0);
        }
        writeInode(id, inode);

        return true;
    }

    /**
     * Search inode. Return inode number if it exist.
     *
     * @param curDir current directory
     * @param filename filename
     * @param delete true - delete link to the file
     * @return desired inode number
     */
    private int searchInode (int curDir, String filename, boolean delete) {
        int id = -1;
        Inode dir = loadInode(curDir);

        int length = dir.numberOfBlocks.length;
        mark:
        for (int i = 0; length > i && dir.numberOfBlocks[i] != -1; i++) {
            int numberOfBlock = dir.numberOfBlocks[i];
            Block block = loadDataBlock(numberOfBlock);
            Link[] links = new Link[superBlock.LINK_SIZE];
            for (int j = 0; j < links.length; j++) {
                links[j] = new Link();
            }

            int sizeSector = superBlock.LINK_SIZE;

            int length2 = superBlock.BLOCK_SIZE / sizeSector;
            for (int j = 0; j < length2; j++) {
                byte[] bytes =
                        block.getSector(j * sizeSector, (j + 1) * sizeSector);
                links[j].load(bytes);
                if (links[j].getInodeId() == -1) {
                    continue;
                }
                if (links[j].getFilename().equals(filename)) {
                    id = links[j].getInodeId();

                    if (delete) {
                        links[j].set(-1, "");
                        block.writeSector(links[j].toBytes(), j * sizeSector);
                        writeDataBlock(block, numberOfBlock);
                    }

                    break mark;
                }
            }
        }
        return id;
    }

    /**
     * Resize file.
     *
     * @param curDir current directory
     * @param filename filename
     * @param size resize to this size
     * @return result of resizing file
     */
    public boolean resizeRegularFile(int curDir, String filename, int size) {
        if (size < 1) {
            return false;
        }

        int id = searchInode(curDir, filename, false);
        if (id == -1) {
            return false;
        }

        Inode inode = loadInode(id);
        if (inode.fileType != Inode.REGULAR) {
            return false;
        }

        int fileSize = inode.size;
        if (size - inode.size > superBlock.FREE_BLOCKS_COUNT) {
            return false;
        }

        int count = 0;
        while (inode.numberOfBlocks[count] != -1) {
            count++;
        }
        count--;
        while (true) {
            if (fileSize > size) {
                setToBlocksBitMap(inode.numberOfBlocks[count], 0);
                inode.numberOfBlocks[count] = -1;
                superBlock.FREE_BLOCKS_COUNT++;
                inode.size -= superBlock.BLOCK_SIZE;
                count--;
            } else if (fileSize < size) {
                int freeDataBlock = getFreeDataBlock();
                setToBlocksBitMap(freeDataBlock, 1);

                inode.size += superBlock.BLOCK_SIZE;
                for (int i = 0; i < inode.numberOfBlocks.length; i++) {
                    if (inode.numberOfBlocks[i] == -1) {
                        inode.numberOfBlocks[i] = freeDataBlock;
                        count++;
                        fileSize += superBlock.BLOCK_SIZE;
                        break;
                    }
                }
            } else {
                break;
            }
        }
        inode.size = size;
        writeInode(id, inode);

        return true;
    }

    /**
     * Set bit in block bitmap
     *
     * @param position position to set
     * @param value value to set
     */
    private void setToBlocksBitMap(int position, int value) {
        int blockSize = superBlock.BLOCK_SIZE;
        int index = position / (blockSize * 8);
        int pos = position % (blockSize * 8);
        blocksBitMap[index].setBits(pos, (byte) value);
        Block b = new Block();
        b.writeSector(blocksBitMap[index].getBits(), 0);
        writeBlock(b, 1 + pos);
    }

    /**
     * Set bit in inodes bitmap
     *
     * @param position position to set
     * @param value value to set
     */
    private void setToInodesBitMap(int position, int value) {
        int blockSize = superBlock.BLOCK_SIZE;
        int index = position / (blockSize * 8);
        int pos = position % (blockSize * 8);
        inodesBitMap[index].setBits(pos, (byte) value);
        Block b = new Block();
        b.writeSector(inodesBitMap[index].getBits(), 0);
        writeBlock(b, 1 + superBlock.BLOCKS_BITMAPS_COUNT + pos);
    }

    /**
     * Search free data block and set "used" to it
     *
     * @return block number
     */
    private int getFreeDataBlock() {
        int blocksBitmapsCount = superBlock.BLOCKS_BITMAPS_COUNT;
        int blockSize = superBlock.BLOCK_SIZE;
        for (int i = 1; i <= blocksBitmapsCount; i++) {
            int length;
            if (i != blocksBitmapsCount) {
                length = blockSize;
            } else {
                length = blocksBitmapsCount * blockSize;
            }

            int freeBit = lookForFreeBitInBitMap(blocksBitMap[i - 1], length);
            if (freeBit != -1) {
                int freeDataBlock = (i - 1) * blockSize + freeBit;
                int index = i - 1;
                blocksBitMap[index].setBits(freeBit, (byte) 1);
                Block b = new Block();
                b.writeSector(blocksBitMap[index].getBits(), 0);
                writeBlock(b, 1 + index);
                return freeDataBlock;
            }
        }
        return -1;
    }

    /**
     * Search free inode and set "used" to it
     *
     * @return inode number
     */
    private int getFreeInode() {
        int inodesBitmapsCount = superBlock.INODES_BITMAPS_COUNT;
        int blockSize = superBlock.BLOCK_SIZE;
        for (int i = 1; i <= inodesBitmapsCount; i++) {
            int length;
            if (i != inodesBitmapsCount) {
                length = blockSize;
            } else {
                length = inodesBitmapsCount * blockSize;
            }

            int freeBit = lookForFreeBitInBitMap(inodesBitMap[i - 1], length);
            if (freeBit != -1) {
                int freeInode = (i - 1) * blockSize + freeBit;
                int index = i - 1;
                inodesBitMap[index].setBits(freeBit, (byte) 1);
                Block b = new Block();
                b.writeSector(inodesBitMap[index].getBits(), 0);
                writeBlock(b, 1 + superBlock.BLOCKS_BITMAPS_COUNT + index);
                return freeInode;
            }
        }
        return -1;
    }

    /**
     * Search free bit in bitmap.
     *
     * @param bitmap bitmap
     * @param length length of bitmap
     * @return founded number of bit or -1
     */
    private int lookForFreeBitInBitMap(BitMap bitmap, int length) {
        byte[] bits = bitmap.getBits();

        for (int i = 0; i < length; i++) {
            StringBuilder binaryString = new StringBuilder();
            binaryString.append(
                    String.format("%8s", Integer.toBinaryString(bits[i]))
                          .replace(' ', '0')
                               );
            for (int j = 0; j < 8; j++) {
                if (binaryString.charAt(7 - j) == '0') {
                    return i * 8 + j;
                }
            }
        }
        return -1;
    }

    /**
     * Resize file up to 1 block
     *
     * @param fd file descriptor
     * @return result of truncating up
     */
    private boolean truncateUp(int fd) {
        Inode inode = openedFiles.get(fd);

        if (inode == null) {
            return false;
        }

        int freeDataBlock = getFreeDataBlock();

        if (freeDataBlock == -1) {
            return false;
        }

        setToBlocksBitMap(freeDataBlock, 1);

        inode.size += superBlock.BLOCK_SIZE;
        for (int i = 0; i < inode.numberOfBlocks.length; i++) {
            if (inode.numberOfBlocks[i] == -1) {
                inode.numberOfBlocks[i] = freeDataBlock;
                break;
            }
        }
        writeInode(fd, inode);
        return true;
    }
}