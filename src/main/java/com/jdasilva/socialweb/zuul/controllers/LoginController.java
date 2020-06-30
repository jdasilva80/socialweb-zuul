package com.jdasilva.socialweb.zuul.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

	@GetMapping({"/login2" })
	public String login() {

		return "login";
	}

}
