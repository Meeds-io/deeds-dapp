package io.meeds.deeds.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.meeds.deeds.elasticsearch.model.TrialContactInformation;
import io.meeds.deeds.storage.TrialRepository;

@SpringBootTest(classes = {
  TrialService.class,
})
class TrialServiceTest {

  private static final String firstname    = "firstname";

  private static final String lastname     = "lastname";

  private static final String position     = "position";

  private static final String organization = "organization";

  private static final String email        = "email";

  @Autowired
  private TrialService        trialService;

  @MockBean
  private TrialRepository     trialRepository;
    
  @Test
  void testSaveTrialWhenNotExists() throws Exception {
    when(trialRepository.existsByEmail(email)).thenReturn(false);
    trialService.saveTrial(firstname, lastname, position, organization, email);
    when(trialRepository.existsByEmail(email)).thenReturn(true);
  }

  @Test
  void testGetNotFoundTrialByEmail() {
    TrialContactInformation trial = trialService.getTrialByEmail(email);
    assertNull(trial);
  }
}
