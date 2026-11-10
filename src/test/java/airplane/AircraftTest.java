package airplane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import airplane.components.Engine;
import airplane.components.FuelSystem;
import airplane.components.LandingGear;
import airplane.components.NavigationSystem;
import airplane.exceptions.FlightException;
import airplane.models.FlightStatus;

public class AircraftTest {

    private Engine engineMock;
    private FuelSystem fuelSystemMock;
    private LandingGear landingGearMock;
    private NavigationSystem navigationSystemMock;
    private Aircraft aircraft;

    @BeforeEach
    public void setUp() {
        engineMock = mock(Engine.class);
        fuelSystemMock = mock(FuelSystem.class);
        landingGearMock = mock(LandingGear.class);
        navigationSystemMock = mock(NavigationSystem.class);
        aircraft = new Aircraft(engineMock, fuelSystemMock, landingGearMock, navigationSystemMock);
    }

    @Test
    public void testPrepareForTakeoffSuccess() {
        when(engineMock.isOperational()).thenReturn(true);
        when(fuelSystemMock.hasEnoughFuel()).thenReturn(true);
        when(navigationSystemMock.isRouteAvailable()).thenReturn(true);

        aircraft.prepareForTakeoff();

        assertEquals(FlightStatus.READY, aircraft.getStatus(), "Aircraft should be READY for takeoff.");
    }

    @Test
    public void testPrepareForTakeoffThrowsExceptionWhenRouteFails() {
        when(engineMock.isOperational()).thenReturn(true);
        when(fuelSystemMock.hasEnoughFuel()).thenReturn(true);
        
        when(navigationSystemMock.isRouteAvailable()).thenThrow(new RuntimeException("Navigation offline"));

        assertThrows(RuntimeException.class, () -> {
            aircraft.prepareForTakeoff();
        });
    }

    @Test
    public void testTakeOffDeploysLandingGearAndStartsEngine() {
        when(engineMock.isOperational()).thenReturn(true);
        when(fuelSystemMock.hasEnoughFuel()).thenReturn(true);
        when(navigationSystemMock.isRouteAvailable()).thenReturn(true);
        aircraft.prepareForTakeoff();

        aircraft.takeOff();

        verify(engineMock).start();
        verify(landingGearMock).retract();
        assertEquals(FlightStatus.IN_FLIGHT, aircraft.getStatus());
    }

    @Test
    public void testCruiseConsumesFuel() {
        when(engineMock.isOperational()).thenReturn(true);
        when(fuelSystemMock.hasEnoughFuel()).thenReturn(true);
        when(navigationSystemMock.isRouteAvailable()).thenReturn(true);
        aircraft.prepareForTakeoff();
        aircraft.takeOff();

        aircraft.cruise(50.0);

        verify(fuelSystemMock).consumeFuel(any(Double.class));
    }

    @Test
    public void testEmergencyLandBehaviors() {
        when(engineMock.isOperational()).thenReturn(true);
        when(fuelSystemMock.hasEnoughFuel()).thenReturn(true);
        when(navigationSystemMock.isRouteAvailable()).thenReturn(true);
        aircraft.prepareForTakeoff();
        aircraft.takeOff();

        doNothing().when(engineMock).stop();

        aircraft.emergencyLand();

        verify(landingGearMock, times(1)).deploy();
        verify(engineMock).stop();
        assertEquals(FlightStatus.LANDED, aircraft.getStatus());
    }

    @Test
    public void testResettingMocks() {
        when(engineMock.isOperational()).thenReturn(true);
        
        reset(engineMock);

        assertThrows(FlightException.class, () -> {
            aircraft.prepareForTakeoff();
        });
    }

   
}