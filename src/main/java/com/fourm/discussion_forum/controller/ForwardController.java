package com.fourm.discussion_forum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to handle SPA (Single Page Application) routing.
 * Forwards all non-API and non-static resource routes to index.html
 * so that the frontend router can handle them.
 */
@Controller
public class ForwardController {

    @RequestMapping(value = "{path:[^\\.]*}")
    public String redirect() {
        // Forward to the index.html so React/frontend routing can take over
        return "forward:/index.html";
    }
}
