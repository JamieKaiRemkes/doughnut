package com.odde.doughnut.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.odde.doughnut.controllers.dto.UserDTO;
import com.odde.doughnut.controllers.dto.UserTokenDTO;
import com.odde.doughnut.entities.User;
import com.odde.doughnut.entities.UserToken;
import com.odde.doughnut.entities.repositories.UserTokenRepository;
import com.odde.doughnut.exceptions.UnexpectedNoAccessRightException;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.testability.MakeMe;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RestUserControllerTest {
  @Autowired MakeMe makeMe;
  @Autowired UserTokenRepository userTokenRepository;
  UserModel userModel;
  RestUserController controller;

  @BeforeEach
  void setup() {
    userModel = makeMe.aUser().toModelPlease();
    controller = new RestUserController(makeMe.modelFactoryService, userModel, userTokenRepository);
  }

  @Test
  void createUserWhileSessionTimeout() {
    assertThrows(
        ResponseStatusException.class, () -> controller.createUser(null, userModel.getEntity()));
  }

  @Test
  void updateUserSuccessfully() throws UnexpectedNoAccessRightException {
    UserDTO dto = new UserDTO();
    dto.setName("new name");
    dto.setSpaceIntervals("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15");
    dto.setDailyAssimilationCount(12);
    User response = controller.updateUser(userModel.getEntity(), dto);
    assertThat(response.getName(), equalTo(dto.getName()));
    assertThat(response.getSpaceIntervals(), equalTo(dto.getSpaceIntervals()));
    assertThat(response.getDailyAssimilationCount(), equalTo(dto.getDailyAssimilationCount()));
  }

  @Test
  void updateOtherUserProfile() {
    UserDTO dto = new UserDTO();
    dto.setName("new name");
    User anotherUser = makeMe.aUser().please();
    assertThrows(
        UnexpectedNoAccessRightException.class, () -> controller.updateUser(anotherUser, dto));
  }

  @Test
  void createUserTokenSuccessfully() throws UnexpectedNoAccessRightException {
    UserTokenDTO tokenDTO = controller.createUserToken(userModel.getEntity());
    assertThat(tokenDTO, notNullValue());
    assertThat(
        tokenDTO.getToken(),
        matchesPattern("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    assertThat(tokenDTO.getCreatedAt(), notNullValue());
    assertThat(tokenDTO.getExpiresAt(), notNullValue());

    List<UserToken> savedTokens = userTokenRepository.findAllByUser(userModel.getEntity());
    assertThat(savedTokens.size(), equalTo(1));
    assertThat(savedTokens.get(0).getToken(), equalTo(tokenDTO.getToken()));
  }

  @Test
  void createTokenForOtherUserShouldFail() {
    User anotherUser = makeMe.aUser().please();
    assertThrows(
        UnexpectedNoAccessRightException.class, () -> controller.createUserToken(anotherUser));
  }

  @Test
  void deleteUserTokenSuccessfully() throws UnexpectedNoAccessRightException {
    // トークンを作成して保存
    String token = "test-token";
    UserToken userToken =
        new UserToken(
            userModel.getEntity(),
            token,
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now().plusYears(1));
    userTokenRepository.save(userToken);

    // トークンを削除
    controller.deleteUserToken(userModel.getEntity());

    // トークンが削除されたことを確認
    List<UserToken> savedTokens = userTokenRepository.findAllByUser(userModel.getEntity());
    assertThat(savedTokens.size(), equalTo(0));
  }

  @Test
  void deleteTokenForOtherUserShouldFail() {
    User anotherUser = makeMe.aUser().please();
    assertThrows(
        UnexpectedNoAccessRightException.class, () -> controller.deleteUserToken(anotherUser));
  }

  @Test
  void getUserTokensSuccessfully() throws UnexpectedNoAccessRightException {
    // トークンを作成して保存
    String token = "test-token";
    makeMe.theUser(userModel.getEntity()).withToken(token).please(true);

    List<UserTokenDTO> tokens = controller.getUserTokens(userModel.getEntity());

    assertThat(tokens, notNullValue());
    assertThat(tokens.size(), equalTo(1));
    assertThat(tokens.get(0).getToken(), equalTo(token));
  }

  @Test
  void getTokensForOtherUserShouldFail() {
    User anotherUser = makeMe.aUser().please();
    assertThrows(
        UnexpectedNoAccessRightException.class, () -> controller.getUserTokens(anotherUser));
  }

  @Test
  void tokenOperationsWithUnauthorizedUserShouldFail() {
    // Create controller with an unauthorized user model
    UserModel unauthorizedUserModel = makeMe.aUser().toModelPlease();
    RestUserController controllerWithUnauthorizedUser =
        new RestUserController(
            makeMe.modelFactoryService, unauthorizedUserModel, userTokenRepository);

    // Create a different user to test against
    User targetUser = makeMe.aUser().please();

    // All token operations should fail with authorization exception
    assertThrows(
        UnexpectedNoAccessRightException.class,
        () -> controllerWithUnauthorizedUser.createUserToken(targetUser));

    assertThrows(
        UnexpectedNoAccessRightException.class,
        () -> controllerWithUnauthorizedUser.deleteUserToken(targetUser));

    assertThrows(
        UnexpectedNoAccessRightException.class,
        () -> controllerWithUnauthorizedUser.getUserTokens(targetUser));
  }
}
