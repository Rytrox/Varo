package de.rytrox.varo.teams;

import de.rytrox.varo.game.GameTimeService;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.event.player.PlayerLoginEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameTimeServiceTest {

    private final LocalTime startTime = LocalTime.of(16, 0);
    private final LocalTime endTime = LocalTime.of(22, 0);

    @Mock
    GameTimeService service;

    @BeforeEach
    void init() throws IllegalAccessException {
        FieldUtils.writeField(service, "startTime", startTime, true);
        FieldUtils.writeField(service, "endTime", endTime, true);
        FieldUtils.writeField(service, "kickMessage", "Du wurdest gekickt", true);
    }

    @Test
    public void shouldCalculateCorrectOffsetWhenNowBeforeStart() {
        doCallRealMethod().when(service).getTimerOffsetEnd();

        LocalTime now = LocalTime.of(12, 0);
        try(MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalTime::now).thenAnswer((any) -> now);

            assertEquals(10 * 60 * 60 * 20L, service.getTimerOffsetEnd());
        }
    }

    @Test
    public void shouldCalculateCorrectOffsetWhenNowAfterEnd() {
        doCallRealMethod().when(service).getTimerOffsetEnd();

        LocalTime now = LocalTime.of(23, 0);
        try(MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalTime::now).thenAnswer((any) -> now);

            assertEquals(23 * 60 * 60 * 20L, service.getTimerOffsetEnd());
        }
    }

    @Test
    public void shouldCalculateCorrectOffsetWhenNowEqualsEnd() {
        doCallRealMethod().when(service).getTimerOffsetEnd();

        LocalTime now = LocalTime.of(22, 0);
        try(MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalTime::now).thenAnswer((any) -> now);

            assertEquals(0, service.getTimerOffsetEnd());
        }
    }

    @Test
    public void shouldKickPlayerWhenSystemTimeIsBeforeStart() {
        PlayerLoginEvent event = mock(PlayerLoginEvent.class);
        doCallRealMethod().when(service).onInvalidJoin(eq(event));

        LocalTime now = LocalTime.of(14, 0);
        try(MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalTime::now).thenAnswer((ans) -> now);
            doNothing().when(event).disallow(any(), anyString());

            service.onInvalidJoin(event);
            verify(event, times(1)).disallow(any(PlayerLoginEvent.Result.class), anyString());
        }
    }

    @Test
    public void shouldNotKickPlayerWhenSystemTimeIsInRange() {
        PlayerLoginEvent event = mock(PlayerLoginEvent.class);
        doCallRealMethod().when(service).onInvalidJoin(eq(event));

        LocalTime now = LocalTime.of(16, 0);
        try(MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalTime::now).thenAnswer((ans) -> now);

            service.onInvalidJoin(event);
            verify(event, times(0)).disallow(any(PlayerLoginEvent.Result.class), anyString());
        }
    }

    @Test
    public void shouldKickPlayerOnEndTime() {
        PlayerLoginEvent event = mock(PlayerLoginEvent.class);
        doCallRealMethod().when(service).onInvalidJoin(eq(event));

        LocalTime now = LocalTime.of(22, 0, 1);
        try(MockedStatic<LocalTime> mockedStatic = mockStatic(LocalTime.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(LocalTime::now).thenAnswer((ans) -> now);
            doNothing().when(event).disallow(any(), anyString());

            service.onInvalidJoin(event);
            verify(event, times(1)).disallow(any(PlayerLoginEvent.Result.class), anyString());
        }
    }
}
