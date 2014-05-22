import java.util.Arrays;
import java.util.Scanner;

/**
 * OS
 *
 * @author Dmytro Troshchuk
 * @version 1.00 17.05.14
 */
public class OS {
    /** File system */
    FileSystem fileSystem;
    /** Hard drive */
    private final HardDrive hardDrive;
    /** Show fs is mounted */
    boolean fs;
    /** Current directory */
    int curDir;

    /**
     * Create hard drive
     */
    public OS() {
        hardDrive = new HardDrive();
    }

    /**
     * Run command line. Function:
     * mkfs - Create filesystem in hard drive
     * exit - Close command line
     * mount - Mount filesystem, saved in hard drive
     * umount - Umount filesystem , saved in hard drive
     * filestat fd - Output info about inode
     * ls - Output list of files with its fd
     * create filename - Create file with filename or output message if it
     * can't be created
     * open filename - Open file by filename.
     * close fd - Close file by fd.
     * read fd off size - Read some bytes in file by fd, offset and size
     * write fd off size - Write some bytes to file by fd, offset and size
     * link filename1 filename2 - Create link from filename1 to filename2
     * unlink filename - Delete link from filename
     * truncate filename size - Resize file by filename to "size"
     */
    public void run() {
        Scanner in = new Scanner(System.in);
        mark:
        while (true) {
            String command = in.nextLine();
            String[] commands = command.split(" ");
            switch (commands[0]) {
                case "mkfs":
                    createFileSystem();
                    break;
                case "exit":
                    break mark;
                case "mount":
                    mountFileSystem();
                    break;
                case "umount":
                    umountFileSystem();
                    break;
                case "filestat":
                    outputFileStat(commands[1]);
                    break;
                case "ls":
                    ls();
                    break;
                case "create":
                    if (commands.length > 1) {
                        create(commands[1]);
                    } else {
                        System.out.println(commands[0] + ": missing operand");
                    }
                    break;
                case "open":
                    if (commands.length > 1) {
                        open(commands[1]);
                    } else {
                        System.out.println(commands[0] + ": missing operand");
                    }
                    break;
                case "close":
                    if (commands.length > 1) {
                        close(commands[1]);
                    } else {
                        System.out.println(commands[0] + ": missing operand");
                    }
                    break;
                case "read":
                    if (commands.length > 3) {
                        read(commands[1], commands[2], commands[3]);
                    } else {
                        System.out.println(commands[0] + ": missing operands");
                    }
                    break;
                case "write":
                    if (commands.length > 3) {
                        write(commands[1], commands[2], commands[3]);
                    } else {
                        System.out.println(commands[0] + ": missing operands");
                    }
                    break;
                case "link":
                    if (commands.length > 2) {
                        link(commands[1], commands[2]);
                    } else {
                        System.out.println(commands[0] + ": missing operands");
                    }
                    break;
                case "unlink":
                    if (commands.length > 1) {
                        unlink(commands[1]);
                    } else {
                        System.out.println(commands[0] + ": missing operands");
                    }
                    break;
                case "truncate":
                    if (commands.length > 2) {
                        truncate(commands[1], commands[2]);
                    } else {
                        System.out.println(commands[0] + ": missing operands");
                    }
                    break;
                default:
                    System.out.println(commands[0] + ": command not found");
            }
        }
    }

    /** Create filesystem */
    public void createFileSystem() {
        String file = hardDrive.getFile();
        int memory = hardDrive.getMemory();
        fileSystem = new FileSystem(file, memory);
        System.out.println("Fs was created");
    }

    /** Mount filesystem */
    public void mountFileSystem() {
        String file = hardDrive.getFile();
        fileSystem = new FileSystem();
        fs = true;
        if (fileSystem.mountFileSystem(file)) {
            System.out.println("Fs was mounted");
        } else {
            System.out.println("Fs wasn't mounted");
        }
    }

    /** Umount filesystem */
    public void umountFileSystem() {
        fs = false;
        fileSystem = null;
    }

    /**
     * Output info about inode
     * Filesystem must be mounted else output message about it
     *
     * @param inode inode id must be numeral else output message about it
     */
    public void outputFileStat(String inode) {
        try {
            if (!fs) {
                System.out.println("File system wasn't mounted!");
                return;
            }
            int i = Integer.parseInt(inode);
            String info = fileSystem.getInodeInfo(i);
            System.out.println(info);
        } catch (Exception e) {
            System.out.println("'" + inode + "' is not numeral!");
        }
    }

    /**
     * Output all files and its inode id in current directory
     * Filesystem must be mounted else output message about it
     */
    public void ls() {
        if (!fs) {
            System.out.println("File system wasn't mounted!");
            return;
        }
        String list = fileSystem.getListOfFiles(curDir);
        System.out.println(list);
    }

    /**
     * Create regular file by filename
     * Filesystem must be mounted else output message about it
     * Output message if file can't be created
     *
     * @param filename filename
     */
    public void create(String filename) {
        if (!fs) {
            System.out.println("File system wasn't mounted!");
            return;
        }
        if (filename.length() > Helper.MAX_FILE_NAME) {
            System.out.println("Max file name is: " + Helper.MAX_FILE_NAME);
            return;
        }
        if (!fileSystem.createRegularFile(curDir, filename)) {
            System.out.println("File wasn't created!");
        }
    }

    /**
     * Open regular file by filename and return fd
     * Filesystem must be mounted else output message about it
     * Output message if file not founded
     *
     * @param filename filename
     */
    public void open(String filename) {
        if (!fs) {
            System.out.println("File system wasn't mounted!");
            return;
        }
        if (filename.length() > Helper.MAX_FILE_NAME) {
            System.out.println("Max file name is: " + Helper.MAX_FILE_NAME);
            return;
        }
        int fd = fileSystem.openRegularFile(curDir, filename);
        if (fd == -1) {
            System.out.println("File not founded!");
        } else {
            System.out.println("fd: " + fd);
        }
    }

    /**
     * Close regular file by fd
     * Filesystem must be mounted else output message about it
     *
     * @param fd fd must be numeral else output message about it
     */
    public void close(String fd) {
        try {
            if (!fs) {
                System.out.println("File system wasn't mounted!");
                return;
            }
            int i = Integer.parseInt(fd);
            fileSystem.closeRegularFile(i);
        } catch (Exception e) {
            System.out.println("'" + fd + "' is not numeral!");
        }
    }

    /**
     * Read regular file by fd. Begin with "off" and read "size" bytes
     * Filesystem must be mounted else output message about it
     * File must be opened else output message about it
     *
     * @param fd   fd must be numeral else output message about it
     * @param off  off must be numeral else output message about it
     * @param size size must be numeral else output message about it
     */
    public void read(String fd, String off, String size) {
        try {
            if (!fs) {
                System.out.println("File system wasn't mounted!");
                return;
            }
            int i1 = Integer.parseInt(fd);
            int i2 = Integer.parseInt(off);
            int i3 = Integer.parseInt(size);
            byte[] bytes = fileSystem.readFile(i1, i2, i3);
            if (bytes == null) {
                System.out.println("Couldn't read");
            } else {
                System.out.println(Arrays.toString(bytes));
            }

        } catch (Exception e) {
            System.out.println("Operands are not numeral!");
        }
    }

    /**
     * Write data to regular file by fd. Begin with "off" and write "size" bytes
     * Data is genereted randomly
     * Filesystem must be mounted else output message about it
     * File must be opened else output message about it
     *
     * @param fd   fd must be numeral else output message about it
     * @param off  off must be numeral else output message about it
     * @param size size must be numeral else output message about it
     */
    public void write(String fd, String off, String size) {
        try {
            if (!fs) {
                System.out.println("File system wasn't mounted!");
                return;
            }
            int i1 = Integer.parseInt(fd);
            int i2 = Integer.parseInt(off);
            int i3 = Integer.parseInt(size);
            byte[] data = new byte[i3];

            for (int i = 0; i < i3; i++) {
                data[i] = (byte) (Math.random() * Byte.MAX_VALUE);
            }
            System.out.println("Data to write: " + Arrays.toString(data));
            if (!fileSystem.writeFile(i1, i2, i3, data)) {
                System.out.println("Couldn't write");
            }


        } catch (Exception e) {
            System.out.println("Operands are not numeral!");
        }
    }

    /**
     * Create link filename2 to file by filename1
     * Filesystem must be mounted else output message about it
     * File must be opened else output message about it
     *
     * @param filename1 filename
     * @param filename2 link name
     */
    public void link(String filename1, String filename2) {
        if (!fs) {
            System.out.println("File system wasn't mounted!");
            return;
        }
        fileSystem.createLink(curDir, filename1, filename2);
    }

    /**
     * Unlink link from file by filename
     * Filesystem must be mounted else output message about it
     * File must be opened else output message about it
     *
     * @param filename filename
     */
    public void unlink(String filename) {
        if (!fs) {
            System.out.println("File system wasn't mounted!");
            return;
        }
        fileSystem.deleteLink(curDir, filename);
    }

    /**
     * Resize file by filename.
     *
     * @param filename filename
     * @param size     size must be numeral else output message about it
     */
    public void truncate(String filename, String size) {
        try {
            if (!fs) {
                System.out.println("File system wasn't mounted!");
                return;
            }
            int i = Integer.parseInt(size);
            fileSystem.resizeRegularFile(curDir, filename, i);

        } catch (Exception e) {
            System.out.println("'" + size + "' is not numeral!");
        }
    }
}
