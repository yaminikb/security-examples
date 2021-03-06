/*
 * Copyright (c) 2015-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.secured;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Provider implements LoginModule {

    private static final Logger LOGGER = Logger.getLogger(Provider.class.getName());

    private Subject subject;
    private CallbackHandler callbackHandler;

    // not used in this simple LoginModule but can be useful when LoginModule are stacked
    private Map<String, ?> sharedState;
    private Map<String, ?> options;

    protected List<UserPrincipal> userPrincipals;
    protected List<RolePrincipal> rolePrincipals;

    @Override
    public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;

        this.userPrincipals = new ArrayList<>();
        this.rolePrincipals = new ArrayList<>();

        // for demo purpose
        System.out.println("=== Options ===");
        options.entrySet().stream()
                .forEach(System.out::println);

        System.out.println("=== Shared state ===");
        sharedState.entrySet().stream()
                .forEach(System.out::println);
    }

    @Override
    public boolean login() throws LoginException {
        final Callback[] callbacks = new Callback[] {
            new NameCallback("username"),
            new PasswordCallback("password", false)
        };
        try {
            callbackHandler.handle(callbacks);

        } catch (IOException | UnsupportedCallbackException e) {
            LOGGER.log(Level.SEVERE, "Can not authenticate user.", e);
            return false;
        }

        final String username = NameCallback.class.cast(callbacks[0]).getName();
        final char[] password = PasswordCallback.class.cast(callbacks[1]).getPassword();

        if (!"snoopy".equals(username) || !"woodst0ck".equals(new String(password))) {
            return false;
        }

        userPrincipals.add(new UserPrincipal("snoopy"));
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        // grab the roles
        rolePrincipals.add(new RolePrincipal("RedBaron"));
        rolePrincipals.add(new RolePrincipal("JoeCool"));
        rolePrincipals.add(new RolePrincipal("MansBestFriend"));

        this.subject.getPrincipals().addAll(userPrincipals);
        this.subject.getPrincipals().addAll(rolePrincipals);

        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        clear();
        return true;
    }

    private void clear() {
        if (rolePrincipals != null) {
            rolePrincipals.clear();
            rolePrincipals = null;
        }

        if (userPrincipals != null) {
            userPrincipals.clear();
            userPrincipals = null;
        }
    }

    @Override
    public boolean logout() throws LoginException {
        clear();
        return true;
    }
}
