package com.careerbridge;

import com.careerbridge.server.ApiServer;

public class Main {
    public static void main(String[] args) throws Exception {
        String env = System.getenv("PORT");
        int port = args.length > 0 ? Integer.parseInt(args[0]) : (env != null ? Integer.parseInt(env) : 8080);
        ApiServer server = new ApiServer(port);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
        System.out.println("CareerBridge backend running at http://localhost:" + port);
        System.out.println("Health check: http://localhost:" + port + "/api/health");
    }
}
