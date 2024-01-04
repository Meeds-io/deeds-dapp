package io.meeds.dapp.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.deeds.elasticsearch.model.TrialContactInformation;
import io.meeds.deeds.service.TrialService;
import io.meeds.dapp.web.security.DeedAuthenticationProvider;

import jakarta.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/api/trials")
public class TrialController {

  @Autowired
  private TrialService trialService;

  @PostMapping(value="/contact", consumes =MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @RolesAllowed(DeedAuthenticationProvider.USER_ROLE_NAME)
  public TrialContactInformation saveTrial(@RequestParam(name = "firstname")
                                           String firstname,
                                           @RequestParam(name = "lastname")
                                           String lastname,
                                           @RequestParam(name = "position")
                                           String position,
                                           @RequestParam(name = "organization")
                                           String organization,
                                           @RequestParam(name = "email")
                                           String email) {
    if (email == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    return trialService.saveTrial(firstname, lastname, position, organization, email);
  }
   
}
