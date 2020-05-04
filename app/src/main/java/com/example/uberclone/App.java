package com.example.uberclone;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)

                .applicationId("mu5HbshV7sbsL1JuwxOXwJo2e4jLqfU7CXqRuYL2")

                .clientKey("xciuU8zrxokJB4853VA9cou87fS92mNpEwbnRWuG")

                .server("https://parseapi.back4app.com/")
                .build()

        );
    }
}
