public class Test {
    public static void main(String[] args) {
        RAM ram = new RAM(128);
        final OS os = new OS(ram);

        Thread threadOfOS = new Thread(new Runnable() {
            @Override
            public void run() {
                os.run();
            }
        });

        threadOfOS.start();

        Process process1 = new Process(16);
        Process process2 = new Process(32);
        os.runProcess(process1);
        os.runProcess(process2);

        os.runProcess(new Process(32));
        os.runProcess(new Process(32));
        os.runProcess(new Process(32));
        os.runProcess(new Process(32));
    }
}
