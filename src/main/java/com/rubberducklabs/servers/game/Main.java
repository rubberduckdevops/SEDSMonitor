package com.rubberducklabs.servers.game;

import com.rubberducklabs.servers.game.apiClient.SpaceEngineersRemoteClient;

public class Main {
    public static void main(String[] args) throws Exception {


        System.out.println("Hello, World!");
        SpaceEngineersRemoteClient client = new SpaceEngineersRemoteClient(
                "http://localhost:8080",
                "xxxx"
        );

        client.getServerStatus();

        client.pingServer();

    }
}