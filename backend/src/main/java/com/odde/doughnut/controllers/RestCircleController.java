package com.odde.doughnut.controllers;

import com.odde.doughnut.controllers.dto.CircleForUserView;
import com.odde.doughnut.controllers.dto.CircleJoiningByInvitation;
import com.odde.doughnut.controllers.dto.NoteCreationDTO;
import com.odde.doughnut.controllers.dto.RedirectToNoteResponse;
import com.odde.doughnut.entities.Circle;
import com.odde.doughnut.entities.Note;
import com.odde.doughnut.entities.User;
import com.odde.doughnut.exceptions.UnexpectedNoAccessRightException;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.models.CircleModel;
import com.odde.doughnut.models.JsonViewer;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.testability.TestabilitySettings;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/circles")
class RestCircleController {
  private final ModelFactoryService modelFactoryService;

  @Resource(name = "testabilitySettings")
  private final TestabilitySettings testabilitySettings;

  private UserModel currentUser;

  public RestCircleController(
      ModelFactoryService modelFactoryService,
      TestabilitySettings testabilitySettings,
      UserModel currentUser) {
    this.modelFactoryService = modelFactoryService;
    this.testabilitySettings = testabilitySettings;
    this.currentUser = currentUser;
  }

  @GetMapping("/{circle}")
  public CircleForUserView showCircle(
      @PathVariable("circle") @Schema(type = "integer") Circle circle)
      throws UnexpectedNoAccessRightException {
    currentUser.assertAuthorization(circle);
    JsonViewer jsonViewer = new JsonViewer();
    return jsonViewer.jsonCircleForUserView(circle);
  }

  @GetMapping("")
  public List<Circle> index() {
    currentUser.assertLoggedIn();
    return currentUser.getEntity().getCircles();
  }

  @PostMapping("")
  @Transactional
  public Circle createCircle(@Valid @RequestBody Circle circle) {
    CircleModel circleModel = modelFactoryService.toCircleModel(circle);
    circleModel.joinAndSave(currentUser.getEntity());
    return circle;
  }

  @PostMapping("/join")
  @Transactional
  public Circle joinCircle(@Valid @RequestBody CircleJoiningByInvitation circleJoiningByInvitation)
      throws BindException {
    CircleModel circleModel =
        modelFactoryService.findCircleByInvitationCode(
            circleJoiningByInvitation.getInvitationCode());
    if (circleModel == null) {
      BindingResult bindingResult =
          new BeanPropertyBindingResult(circleJoiningByInvitation, "circle");
      bindingResult.rejectValue("invitationCode", "error.error", "Does not match any circle");

      throw new BindException(bindingResult);
    }
    User user = currentUser.getEntity();
    if (user.inCircle(circleModel.getEntity())) {
      BindingResult bindingResult =
          new BeanPropertyBindingResult(circleJoiningByInvitation, "circle");
      bindingResult.rejectValue("invitationCode", "error.error", "You are already in this circle");
      throw new BindException(bindingResult);
    }
    circleModel.joinAndSave(user);
    return circleModel.getEntity();
  }

  @PostMapping({"/{circle}/notebooks"})
  @Transactional
  public RedirectToNoteResponse createNotebookInCircle(
      @PathVariable @Schema(type = "integer") Circle circle,
      @Valid @RequestBody NoteCreationDTO noteCreation)
      throws UnexpectedNoAccessRightException {
    currentUser.assertLoggedIn();
    currentUser.assertAuthorization(circle);
    Note note =
        circle
            .getOwnership()
            .createAndPersistNotebook(
                currentUser.getEntity(),
                testabilitySettings.getCurrentUTCTimestamp(),
                modelFactoryService,
                noteCreation.getNewTitle());
    return new RedirectToNoteResponse(note.getId());
  }
}
