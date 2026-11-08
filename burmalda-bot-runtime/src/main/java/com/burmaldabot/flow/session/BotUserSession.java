package com.burmaldabot.flow.session;

import com.burmaldabot.flow.UserFlowState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bot_user_session")
@Getter
@Setter
@NoArgsConstructor
public class BotUserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long botId;

    @Column(nullable = false)
    private Long telegramUserId;

    private Long chatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserFlowState state = UserFlowState.MAIN_MENU;

    private String selectedResolution;
    private Integer selectedDuration;
    private Boolean isFreeTrial = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "bot_user_session_photos", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "file_id")
    private List<String> photoFileIds = new ArrayList<>();

    @Version
    private Long version;

    private Instant updatedAt;

    public BotUserSession(Long botId, Long telegramUserId, Long chatId) {
        this.botId = botId;
        this.telegramUserId = telegramUserId;
        this.chatId = chatId;
        this.state = UserFlowState.MAIN_MENU;
        this.isFreeTrial = false;
        this.updatedAt = Instant.now();
    }

    public void resetToMenu() {
        this.state = UserFlowState.MAIN_MENU;
        this.selectedResolution = null;
        this.selectedDuration = null;
        this.isFreeTrial = false;
        this.photoFileIds.clear();
        this.updatedAt = Instant.now();
    }

    public void startPaidGeneration() {
        this.state = UserFlowState.SELECTING_RESOLUTION;
        this.selectedResolution = null;
        this.selectedDuration = null;
        this.isFreeTrial = false;
        this.photoFileIds.clear();
        this.updatedAt = Instant.now();
    }

    public void startFreeTrial() {
        this.state = UserFlowState.COLLECTING_PHOTOS;
        this.selectedResolution = "480P";
        this.selectedDuration = 3;
        this.isFreeTrial = true;
        this.photoFileIds.clear();
        this.updatedAt = Instant.now();
    }

    public void addPhoto(String fileId) {
        if (this.photoFileIds.size() < 10) {
            this.photoFileIds.add(fileId);
            this.updatedAt = Instant.now();
        }
    }
}
