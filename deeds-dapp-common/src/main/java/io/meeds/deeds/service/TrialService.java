package io.meeds.deeds.service;

import static io.meeds.deeds.constant.CommonConstants.TRIAL_CREATE_COMMAND_EVENT;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.meeds.deeds.constant.TrialStatus;
import io.meeds.deeds.elasticsearch.model.TrialContactInformation;
import io.meeds.deeds.storage.TrialRepository;

@Component
public class TrialService {

  @Autowired
  private TrialRepository trialRepository;

  @Autowired
  private ListenerService             listenerService;

  public TrialContactInformation saveTrial(String firstname, String lastname, String position, String organization, String email) {
    TrialContactInformation trial = new TrialContactInformation();
    trial.setFirstName(firstname);
    trial.setLastName(lastname);
    trial.setPosition(position);
    trial.setOrganization(organization);
    trial.setEmail(email);
    trial.setDate(LocalDateTime.now(ZoneOffset.UTC));
    trial.setStatus(TrialStatus.IN_PROGRESS);
    TrialContactInformation savedTrail = trialRepository.save(trial);

    listenerService.publishEvent(TRIAL_CREATE_COMMAND_EVENT, savedTrail);

    return savedTrail;
  }
    
}
