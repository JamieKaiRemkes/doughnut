package com.odde.doughnut.services;

import com.odde.doughnut.controllers.dto.GlobalAiModelSettings;
import com.odde.doughnut.entities.GlobalSettings;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import java.sql.Timestamp;

public class GlobalSettingsService {

  public static final String DEFAULT_CHAT_MODEL = "gpt-3.5-turbo";
  private final ModelFactoryService modelFactoryService;

  public GlobalSettingsService(ModelFactoryService modelFactoryService) {
    this.modelFactoryService = modelFactoryService;
  }

  public GlobalSettingsKeyValue globalSettingQuestionGeneration() {
    return new GlobalSettingsKeyValue(
        "question_generation_model", DEFAULT_CHAT_MODEL, modelFactoryService);
  }

  public GlobalSettingsKeyValue globalSettingEvaluation() {
    return new GlobalSettingsKeyValue("evaluation_model", DEFAULT_CHAT_MODEL, modelFactoryService);
  }

  public GlobalSettingsKeyValue globalSettingOthers() {
    return new GlobalSettingsKeyValue("others_model", DEFAULT_CHAT_MODEL, modelFactoryService);
  }

  public GlobalSettingsKeyValue noteCompletionAssistantId() {
    return new GlobalSettingsKeyValue(
        "note_completion_assistant", "asst_mGLNAgjtMR60NUheP2QtXJ2D", modelFactoryService);
  }

  public static class GlobalSettingsKeyValue implements SettingAccessor {
    private final String keyName;
    private final String defaultValue;
    private final ModelFactoryService modelFactoryService;

    public GlobalSettingsKeyValue(
        String keyName, String defaultValue, ModelFactoryService modelFactoryService) {
      this.keyName = keyName;
      this.defaultValue = defaultValue;
      this.modelFactoryService = modelFactoryService;
    }

    @Override
    public String getValue() {
      return getGlobalSettings().getValue();
    }

    @Override
    public void setKeyValue(Timestamp currentUTCTimestamp, String value) {
      GlobalSettings settings = getGlobalSettings();
      settings.setValue(value);
      settings.setUpdatedAt(currentUTCTimestamp);
      modelFactoryService.save(settings);
    }

    public Timestamp getCreatedAt() {
      return getGlobalSettings().getUpdatedAt();
    }

    private GlobalSettings getGlobalSettings() {
      GlobalSettings currentQuestionGenerationModelVersion =
          modelFactoryService.globalSettingRepository.findByKeyName(keyName);
      if (currentQuestionGenerationModelVersion == null) {
        GlobalSettings globalSettings = new GlobalSettings();
        globalSettings.setKeyName(keyName);
        globalSettings.setValue(defaultValue);
        return globalSettings;
      }
      return currentQuestionGenerationModelVersion;
    }

    public String keyName() {
      return keyName;
    }

    public ModelFactoryService modelFactoryService() {
      return modelFactoryService;
    }
  }

  public GlobalAiModelSettings getCurrentModelVersions() {
    return new GlobalAiModelSettings(
        globalSettingQuestionGeneration().getValue(),
        globalSettingEvaluation().getValue(),
        globalSettingOthers().getValue());
  }

  public GlobalAiModelSettings setCurrentModelVersions(
      GlobalAiModelSettings models, Timestamp currentUTCTimestamp) {
    globalSettingQuestionGeneration()
        .setKeyValue(currentUTCTimestamp, models.getQuestionGenerationModel());
    globalSettingEvaluation().setKeyValue(currentUTCTimestamp, models.getEvaluationModel());
    globalSettingOthers().setKeyValue(currentUTCTimestamp, models.getOthersModel());
    return models;
  }
}
