public class SingleThreadedDeadlock {
    public static void main(String[] args) throws InterruptedException {
        Thread.currentThread().join();
    }
}
