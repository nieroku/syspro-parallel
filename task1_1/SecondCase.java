public class SecondCase {
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
        Thread threadC = new Thread () {
            @Override
            public void run() {
                try {
                    threadB.join();
                } catch (InterruptedException e) {
                    System.err.println(e);
                }
                System.out.println("Thread C: joined Thread B");
            }
        };
        threadC.start();
        try {
            threadC.join();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
}
