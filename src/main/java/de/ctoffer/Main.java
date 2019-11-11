package de.ctoffer;

import de.ctoffer.assistance.Assistant;

public class Main {
    public static void main(String[] args) {
        try(Assistant assistant = Assistant.getInstance()) {
            while(assistant.shouldServe()) {
                assistant.waitForOrder();
            }
        }
    }
}
