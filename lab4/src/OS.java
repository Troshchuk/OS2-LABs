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
    /** Root directory */
    int root;
    /** Current directory */
    int curDir;

    /**
     * Create hard drive
     */
    public OS() {
        hardDrive = new HardDrive();
        root = 0;
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
     * mkdir name - Create directory with name or output message if it
     * can't be created
     * rmdir name - Remove directory with name or output message if it
     * can't be removed
     * cd name - Change current directory to directory with "name"
     * pwd - Output current directory
     * symlink filename1 filename2 - Create symlink from filename1 to filename2
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
                        link(commands[2], commands[1]);
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
                case "mkdir":
                    if (commands.length > 1) {
                        mkdir(commands[1]);
                    } else {
                        System.out.println(commands[0] + ": missing operands");
                    }
                    break;
                case "rmdir":
                    if (commands.length > 1) {
                        rmdir(commands[1]);
                    } else {
                        System.out.println(commands[0] + ": missing operands");
                    }
                    break;
                case "cd":
                    if (commands.length > 1) {
                        cd(commands[1]);
                    } else {
                        System.out.println(commands[0] + ": missing operands");
                    }
                    break;
                case "pwd":
                    System.out.println("Current directory: " + curDir);
                    break;
                case "symlink":
                    if (commands.length > 2) {
                        symlink(commands[2], commands[1]);
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
        int dir = curDir;
        if (filename.contains("/")) {
            dir = searchDirectory(filename, true);
            if (dir != -1) {
                filename = filename.substring(filename.lastIndexOf("/") + 1);
            } else {
                System.out.println(filename + ": No such file or directory");
                return;
            }
        }

        if (filename.length() > Helper.MAX_FILE_NAME) {
            System.out.println("Max file name is: " + Helper.MAX_FILE_NAME);
            return;
        }


        if (!fileSystem.createRegularFile(dir, filename)) {
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

        int dir = curDir;
        if (filename.contains("/")) {
            dir = searchDirectory(filename, true);
            if (dir != -1) {
                filename = filename.substring(filename.lastIndexOf("/") + 1);
            } else {
                System.out.println(filename + ": No such file or directory");
                return;
            }
        }

        int fd = fileSystem.openRegularFile(dir, filename);
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
     * Create link filename2 to file with filename1
     * Filesystem must be mounted else output message about it
     * File must be opened else output message about it
     */
    public void link(String target, String linkName) {
        if (!fs) {
            System.out.println("File system wasn't mounted!");
            return;
        }

        fileSystem.createLink(curDir, linkName, target);
    }

    /**
     * Unlink link from file by filename
     * Filesystem must be mounted else output message about it
     * File must be opened else output message about it
     *
     * @param linkName filename
     */
    public void unlink(String linkName) {
        if (!fs) {
            System.out.println("File system wasn't mounted!");
            return;
        }
        int dir = curDir;
        if (linkName.contains("/")) {
            dir = searchDirectory(linkName, true);
            if (dir != -1) {
                linkName = linkName.substring(linkName.lastIndexOf("/") + 1);
            } else {
                System.out.println(linkName + ": No such file or directory");
                return;
            }
        }

        fileSystem.deleteLink(dir, linkName);
    }

    /**
     * Resize file by filename.
     * Filesystem must be mounted else output message about it
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

            int dir = curDir;
            if (filename.contains("/")) {
                dir = searchDirectory(filename, true);
                if (dir != -1) {
                    filename =
                            filename.substring(filename.lastIndexOf("/") + 1);
                } else {
                    System.out
                            .println(filename + ": No such file or directory");
                    return;
                }
            }

            fileSystem.resizeRegularFile(dir, filename, i);

        } catch (Exception e) {
            System.out.println("'" + size + "' is not numeral!");
        }
    }

    /**
     * Create a directory
     * Filesystem must be mounted else output message about it
     *
     * @param name name of directory
     */
    public void mkdir(String name) {
        if (!fs) {
            System.out.println("File system wasn't mounted");
            return;
        }

        int dir = curDir;

        if (name.contains("/")) {
            dir = searchDirectory(name, true);
            if (dir != -1) {
                name = name.substring(name.lastIndexOf("/") + 1);
            } else {
                System.out.println(name + ": No such file or directory");
                return;
            }
        }

        if (!fileSystem.createDirectory(dir, name)) {
            System.out.println("Directory wasn't created");
        }
    }

    /**
     * Remove a directory
     * Filesystem must be mounted else output message about it
     *
     * @param name name of directory
     */
    public void rmdir(String name) {
        if (!fs) {
            System.out.println("File system wasn't mounted");
            return;
        }

        int dir = curDir;

        if (name.contains("/")) {
            dir = searchDirectory(name, true);
            if (dir != -1) {
                name = name.substring(name.lastIndexOf("/") + 1);
            } else {
                System.out.println(name + ": No such file or directory");
                return;
            }
        }

        if (!fileSystem.removeDirectory(dir, name)) {
            System.out.println(name + ": directory can't be removed");
        }
    }

    /**
     * Change current directory
     * Filesystem must be mounted else output message about it
     *
     * @param name name of directory
     */
    public void cd(String name) {
        if (!fs) {
            System.out.println("File system wasn't mounted");
            return;
        }

        int dir = searchDirectory(name, false);

        if (dir < 0) {
            System.out.println("Directory can't be changed");
        } else {
            curDir = dir;
        }
    }

    /**
     * Create symlink filename2 to file with target
     * Filesystem must be mounted else output message about it
     *
     * @param name   name of symlink
     * @param target target filename
     */
    public void symlink(String name, String target) {
        if (!fs) {
            System.out.println("File system wasn't mounted");
            return;
        }

        if (!fileSystem.createSymlink(curDir, target, name)) {
            System.out.println("Symlink can't be created");
        }
    }

    private int searchDirectory(String path, boolean isFile) {
        int dir = curDir;

        if (path.charAt(0) == '/') {
            dir = root;
            path = path.substring(1);
        }

        if (isFile) {
            int lastIndexOf = path.lastIndexOf("/");
            path = path.substring(0, lastIndexOf);
        }

        if (path.charAt(path.length() - 1) == '/') {
            path = path.substring(0, path.length() - 1);
        }

        String[] directories = path.split("/");

        for (String str : directories) {
            dir = fileSystem.searchDirectory(dir, str);
            if (dir == -1) {
                return -1;
            }
        }

        return dir;
    }
}
