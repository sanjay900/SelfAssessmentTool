package sat;

import sat.webserver.WebServer;

public class TestApp {
    public static void main(String[] args) {
        new TestApp();
    }
    public TestApp() {
        new WebServer().startServer();
    }
}
