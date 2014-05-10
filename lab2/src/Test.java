public class Test {
    public static void main(String[] args) {
        RAM ram = new RAM(64);
        final OS os = new OS(ram);

        Thread threadOfOS = new Thread(new Runnable() {
            @Override
            public void run() {
                os.run();
            }
        });

        threadOfOS.start();

        os.addProgram(new Program(32));
        os.addProgram(new Program(64));
        os.runPrograms();
    }
}
