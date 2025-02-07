public class MultiThreadedDeadlock {
    static volatile Runnable waitB;

    public static void main(String[] args) throws InterruptedException {
        Thread A = new Thread(() -> { waitB.run(); });
        Thread B = new Thread(() -> {
            try { 
                A.join(); 
            } catch (InterruptedException e) {
                System.err.println(e);
            };
        });
        waitB = () -> {
            try { 
                B.join(); 
            } catch (InterruptedException e) {
                System.err.println(e);
            };
        };
        A.start(); B.start();
        A.join(); B.join();
    }
}
