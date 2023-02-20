package com.zavar.zavarlauncher;


import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.minecraft_authenticator.login.User;

import java.util.Optional;


public class AuthTest {
    public static void main(String[] args) throws AuthenticationException {
        // Build authenticator
        final Authenticator authenticator = Authenticator.ofMicrosoft("M.R3_BAY.ebffc781-09c3-1fa9-3d8f-0b8aa59809f6").
                customAzureApplication("fa3dee6a-27cd-4f30-8344-c438b3f5c037", "http://localhost:8000").shouldAuthenticate().build();
        try {
            // Run authentication
            authenticator.run();
        } catch (final Exception ex) {
            // Always check if result file is present when an exception is thrown
            final AuthenticationFile file = authenticator.getResultFile();
            if (file != null) {
                // Save authentication file
                //file.writeCompressed(outputStream);
            }

            // Show user error or rethrow
            throw ex;
        }

// Save authentication file
        final AuthenticationFile file = authenticator.getResultFile();
        //file.writeCompressed(outputStream);

// Get user
        final Optional<User> user = authenticator.getUser();
        System.out.println(user);
    }
}
