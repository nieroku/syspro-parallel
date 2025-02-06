public class FirstCase {
    public static void main(String[] args) {
        Thread threadB = new Thread () {
            @Override
            public void run() {
                throw new RuntimeException();
            }
        };
        threadB.start();
        try {
            threadB.join();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        System.out.println("Thread A: joined Thread B");
    }
}
