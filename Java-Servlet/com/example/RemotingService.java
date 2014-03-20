package com.example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class RemotingService {

    private HttpServletRequest httpServletRequest;
    private HttpSession httpSession;

    public RemotingService() {
    }

    public RemotingService(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
        this.httpSession = httpServletRequest.getSession();
    }

    public String reverseAndAppendStrings(String first, String second) {
        StringBuilder reverseFirst = new StringBuilder(first);
        StringBuilder reverseSecond = new StringBuilder(second);
        return reverseFirst.reverse().toString() + reverseSecond.reverse().toString();
    }
}
